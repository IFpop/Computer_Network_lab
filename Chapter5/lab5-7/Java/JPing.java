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
	 * ��֯ICMP���ķ��ͣ��������߳̽��ձ���
	 * 
	 * @param ip
	 */
	public void ping(String ip) {
		try {
			usedIP=ip;
			System.out.println("����Ping "+ip+" ����32�ֽڵ����ݣ�");
			jpcap = JpcapCaptor.openDevice(devices[1], 200, false, 20);
			sender = jpcap.getJpcapSenderInstance();
			// ��������ֻ����ICMP����
			jpcap.setFilter("icmp", true);
			icmpPacket = new ICMPPacket();
			// ���ͻ���������
			icmpPacket.type = ICMPPacket.ICMP_ECHO;

			icmpPacket.setIPv4Parameter(0, false, false, false, 0, false, false, false, 0, 1010101, 100,
					IPPacket.IPPROTO_ICMP, devices[0].addresses[1].address, InetAddress.getByName(ip));

			// icmp���ݱ������Ͱ������ݣ����⣩
			icmpPacket.data = "abcdefghijklmnopqrstuvwxyzabcdef".getBytes();
			EthernetPacket ethernetPacket = new EthernetPacket();
			ethernetPacket.frametype = EthernetPacket.ETHERTYPE_IP;
			ethernetPacket.src_mac = devices[0].mac_address;

			// �㲥��ַ
			ethernetPacket.dst_mac = new byte[] { (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff,
					(byte) 0xff };
			icmpPacket.datalink = ethernetPacket;
			listResult.add("Pinging " + icmpPacket.dst_ip + " with " + icmpPacket.data.length + " bytes of data: ");
			
			// �������ݱ�
			startCapThread(jpcap);

			// �������ݱ�
			for (int i = 0; i < 4; i++) {
				icmpPacket.sec = 0;
				// icmpPacket.usec = System.currentTimeMillis();
				icmpPacket.usec = new GregorianCalendar().getTimeInMillis();// ��¼����ʱ��
				icmpPacket.seq = (short) (1000 + i);
				icmpPacket.id = (short) (999 + i);
				sender.sendPacket(icmpPacket);
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * ����ICMP����
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
//				System.out.println("���յ���icmp���ݱ�------��" + rp);
				numOfData++;
				// ��ȷ���յ�ICMP���ݰ�
				if (rp != null) {
					// ���㷢������յ�ʱ���
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
					// ICMP���ݱ���Ϣ
					System.out.println("����" + rp.src_ip.getHostAddress()+"�Ļظ�" + ": �ֽ�= " + rp.data.length
								+ " ʱ��" + tmpStr + "TTL = " + rp.hop_limit);
					// ICMP���ݱ�������Ϣ�����
					listResult.add("Reply from " + rp.src_ip.getHostAddress() + ": bytes = " + rp.data.length + " time "
							+ tmpStr + "TTL = " + rp.hop_limit);

				} else {
					numOfFail++;
					System.out.println("request timeout");
				}
			}
			System.out.println(usedIP+"��Pingͳ����Ϣ��");
			System.out.println("    ���ݰ����ѷ���="+numOfData+"���ѽ���="+numOfSuc+"����ʧ="+numOfFail+"��"+numOfFail*1.0/numOfData+"%��ʧ)��");//			System.out.println("");
			System.out.println("�����г̵Ĺ���ʱ�䣨�Ժ���Ϊ��λ����");
			System.out.println("    ���="+minTime+"ms���="+maxTime+"ms��ƽ��="+time/numOfData+"ms");
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/**
	 * ����ICMP����
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
	 * �ж��Ƿ�����ȷ��IP��ַ
	 */
	public static boolean isboolIp(String ipAddress) {
		String ip = "([1-9]|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])(\\.(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])){3}";
		Pattern pattern = Pattern.compile(ip);
		Matcher matcher = pattern.matcher(ipAddress);
		return matcher.matches();
	}

	/**
	 * ���
	 * 
	 * ping һ�Σ�ping xxxx
	 * 
	 * ping ���޴Σ�ping -t xxxx
	 */
	public static void main(String[] args) {

		JPing jPing = new JPing();
		Scanner scanner = new Scanner(System.in);
//		System.out.println("������ָ�");
		time=0;
		String command = scanner.nextLine();
		String[] str = command.split(" ");
		if ("-t".equals(str[1])) {
			if (isboolIp(str[2])) {
				while (true) {
					jPing.ping(str[2]);
				}
			} else {
				System.out.println("ip��������");
			}
		} else if (isboolIp(str[1])) {
			jPing.ping(str[1]);
		} else {
			System.out.println("��������");
		}
	}
}

