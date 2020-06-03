package Chapter5.Lab7;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jpcap.JpcapCaptor;
import jpcap.JpcapSender;
import jpcap.NetworkInterface;
import jpcap.packet.EthernetPacket;
import jpcap.packet.ICMPPacket;
import jpcap.packet.IPPacket;

import static java.lang.Integer.min;
import static sun.swing.MenuItemLayoutHelper.max;

public class JPing {

	private static int time;
	private NetworkInterface[] devices = JpcapCaptor.getDeviceList();
	private JpcapSender sender;
	private JpcapCaptor jpcap;
	private ICMPPacket icmpPacket;
	private List<String> listResult = new ArrayList<String>();
	private static String usedIP;
//	private int time=0;
	/**
	 * 组织ICMP报文发送，并开启线程接收报文
	 * 
	 * @param ip
	 */
	public void ping(String ip) {
		try {
			usedIP=ip;
			System.out.println("正在Ping "+ip+" 具有32字节的数据：");
			jpcap = JpcapCaptor.openDevice(devices[1], 200, false, 20);
			sender = jpcap.getJpcapSenderInstance();
			// 拦截器，只接受ICMP报文
			jpcap.setFilter("icmp", true);
			icmpPacket = new ICMPPacket();
			// 发送回显请求报文
			icmpPacket.type = ICMPPacket.ICMP_ECHO;

			icmpPacket.setIPv4Parameter(0, false, false, false, 0, false, false, false, 0, 1010101, 100,
					IPPacket.IPPROTO_ICMP, devices[0].addresses[1].address, InetAddress.getByName(ip));

			// icmp数据报（发送包）数据（任意）
			icmpPacket.data = "abcdefghijklmnopqrstuvwxyzabcdef".getBytes();
			EthernetPacket ethernetPacket = new EthernetPacket();
			ethernetPacket.frametype = EthernetPacket.ETHERTYPE_IP;
			ethernetPacket.src_mac = devices[0].mac_address;

			// 广播地址
			ethernetPacket.dst_mac = new byte[] { (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff,
					(byte) 0xff };
			icmpPacket.datalink = ethernetPacket;
			listResult.add("Pinging " + icmpPacket.dst_ip + " with " + icmpPacket.data.length + " bytes of data: ");
			
			// 接收数据报
			startCapThread(jpcap);

			// 发送数据报
			for (int i = 0; i < 4; i++) {
				icmpPacket.sec = 0;
				// icmpPacket.usec = System.currentTimeMillis();
				icmpPacket.usec = new GregorianCalendar().getTimeInMillis();// 记录发送时间
				icmpPacket.seq = (short) (1000 + i);
				icmpPacket.id = (short) (999 + i);
				sender.sendPacket(icmpPacket);
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 接收ICMP报文
	 * 
	 * @param
	 */
	public void getIcmpPacket(JpcapCaptor jpcapCaptor) {
		try {
			long tmp = 0;
			int numOfSuc=0,numOfFail=0,numOfData=0;
			int maxTime=0,minTime=9999;
			String tmpStr = null;
			ICMPPacket rp;
			while ((rp = (ICMPPacket) jpcapCaptor.getPacket()) != null) {
//				System.out.println("接收到的icmp数据报------》" + rp);
				numOfData++;
				// 正确接收到ICMP数据包
				if (rp != null) {
					// 计算发送与接收的时间差
					tmp = (rp.sec * 1000 + rp.usec / 1000 - icmpPacket.sec * 1000 - icmpPacket.usec);
					if (tmp <= 0) {
						maxTime=max(maxTime,(int)tmp);
						if(tmp==0)
						minTime=min(minTime,(int)tmp);
						tmpStr = " < 1 ms ";
//						time+=tmp;
					} else {
						tmpStr = "= " + tmp + " ms ";
						time+=tmp;
					}
					numOfSuc++;
					// ICMP数据报信息
					System.out.println("来自" + rp.src_ip.getHostAddress()+"的回复" + ": 字节= " + rp.data.length
								+ " 时间" + tmpStr + "TTL = " + rp.hop_limit);
					// ICMP数据报接收信息结果集
					listResult.add("Reply from " + rp.src_ip.getHostAddress() + ": bytes = " + rp.data.length + " time "
							+ tmpStr + "TTL = " + rp.hop_limit);

				} else {
					numOfFail++;
					System.out.println("request timeout");
				}
			}
			System.out.println(usedIP+"的Ping统计信息：");
			System.out.println("    数据包：已发送="+numOfData+"，已接收="+numOfSuc+"，丢失="+numOfFail+"（"+numOfFail*1.0/numOfData+"%丢失)，");//			System.out.println("");
			System.out.println("往返行程的估计时间（以毫秒为单位）：");
			System.out.println("    最短="+minTime+"ms，最长="+maxTime+"ms，平均="+time/numOfData+"ms");
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/**
	 * 接收ICMP报文
	 * 
	 * @param jpcap
	 */
	public void startCapThread(final JpcapCaptor jpcap) {

		Runnable runner = new Runnable() {
			public void run() {
				getIcmpPacket(jpcap);
			}
		};
		new Thread(runner).start();
	}

	/**
	 * 判断是否是正确的IP地址
	 */
	public static boolean isboolIp(String ipAddress) {
		String ip = "([1-9]|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])(\\.(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])){3}";
		Pattern pattern = Pattern.compile(ip);
		Matcher matcher = pattern.matcher(ipAddress);
		return matcher.matches();
	}

	/**
	 * 命令：
	 * 
	 * ping 一次：ping xxxx
	 * 
	 * ping 无限次：ping -t xxxx
	 */
	public static void main(String[] args) {

		JPing jPing = new JPing();
		Scanner scanner = new Scanner(System.in);
//		System.out.println("请输入指令：");
		time=0;
		String command = scanner.nextLine();
		String[] str = command.split(" ");
		if ("-t".equals(str[1])) {
			if (isboolIp(str[2])) {
				while (true) {
					jPing.ping(str[2]);
				}
			} else {
				System.out.println("ip输入有误");
			}
		} else if (isboolIp(str[1])) {
			jPing.ping(str[1]);
		} else {
			System.out.println("输入有误");
		}
	}
}

