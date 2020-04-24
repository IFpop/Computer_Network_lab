package Lab4.Capture;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.Scanner;

import jpcap.*;
import jpcap.packet.ARPPacket;
import jpcap.packet.DatalinkPacket;
import jpcap.packet.EthernetPacket;
import jpcap.packet.ICMPPacket;
import jpcap.packet.IPPacket;
import jpcap.packet.Packet;
import jpcap.packet.TCPPacket;
import jpcap.packet.UDPPacket;
public class PacketCapture implements PacketReceiver{
	public static String GetMac(byte[] bt)
	{
		String mac=new String();
		int len=0;
		for(byte b:bt)
		{
			len++;
			if(len<bt.length)
			{
				mac+=Integer.toHexString(b&0xff)+":";
			}
			else {
				mac+=Integer.toHexString(b&0xff);
			}
		}
		return mac;
	}
	public static void main(String[] argc)
	{
		
		NetworkInterface[] devices = JpcapCaptor.getDeviceList();
		if(devices.length==0)
		{
			System.out.println("未检测到网络适配器信息！");
			return;
		}
		System.out.println("检测到的网卡如下！");
		int num=0;
		for (NetworkInterface n : devices) {
			System.out.println("------------ 设备ID:"+num+"------------------");
			System.out.println("设备名称"+n.name);
			System.out.println("设备描述"+n.description);
			int len=0;
			String mac=GetMac(n.mac_address);
			System.out.println("MAC地址为："+mac);
			num++;
		}
		System.out.println("-------------------------------------------");
		while(true)
		{
			Scanner scanner=new Scanner(System.in);
			System.out.print("请输入要捕获的网卡设备编号(若要退出请输入-1):");
			int Id=scanner.nextInt();
			if(Id==-1) break;
			//Listen to the device
//			
			/**
			 * NetworkInterderface :要打开的网络接口。
			 * int snaplen :一次捕获数据包的最大byte数。
			 * boolean prommics :是否采用混淆模式
			 * int to_ms：捕获的数据包的超时设置（数量级为毫秒）。
			 */
			try {
				JpcapCaptor jpcap=JpcapCaptor.openDevice(devices[Id], 65536, true, 50);
				jpcap.loopPacket(20, new PacketCapture());//是receivePacket的回调函数
			}catch (IOException e) {
				// TODO: handle exception
				e.printStackTrace();
			}
		}
	}

	@Override
	public void receivePacket(Packet packet) {
		// TODO Auto-generated method stub
//		System.out.println(packet);
//		下面进行分类讨论
		System.out.println("----------------------------------------------");
		System.out.println("实际捕获的包长度: " + packet.caplen);
		System.out.println("真正的包长度: " + packet.len);
		System.out.println("");
		//数据链路层
		DatalinkPacket DataLink=packet.datalink;
		if(DataLink instanceof EthernetPacket)
		{
			System.out.println("MAC子层结构及各字段的值:");
			EthernetPacket Ep=(EthernetPacket)DataLink;
			//获取mac地址
			System.out.println("源MAC地址为"+GetMac(Ep.src_mac));
			System.out.println("目的MAC地址为"+GetMac(Ep.dst_mac));
			System.out.println("以太类型为"+Integer.toHexString(Ep.frametype & 0xffff));
			System.out.println("");
		}
		else 
		{
			System.out.println("此帧为其他类型的数据链路帧");
			System.out.println("帧内容为"+packet);
			System.out.println("");
		}
		if(packet instanceof ARPPacket)
		{
			System.out.println("网络层报文类型:ARP");
			ARPPacket ap = (ARPPacket) packet;
			System.out.println("硬件类型: " + ap.hardtype);
			System.out.println("协议类型: " + ap.prototype);
			System.out.println("硬件地址长度: " + ap.hlen);
			System.out.println("协议地址长度: " + ap.plen);
			System.out.println("操作码: " + ap.operation);
			System.out.println("发送者MAC地址: " + ap.getSenderHardwareAddress());
			System.out.println("发送者IP地址: " + ap.getSenderProtocolAddress());
			System.out.println("目标MAC地址: " + ap.getTargetHardwareAddress());
			System.out.println("目标IP地址: " + ap.getTargetProtocolAddress());
			System.out.println("");
			
		}
		if(packet instanceof IPPacket)
		{
			
			IPPacket ip=(IPPacket)packet;
			System.out.println("网络层报文类型:IP");
			if(ip.version==4)//ipv4版本号为4
			{
				System.out.println("服务类型: " + ip.rsv_tos);
				System.out.println("优先级: " + ip.priority);
				System.out.println("包长度: " + ip.length);
				System.out.println("标志: " + ip.ident);
				System.out.println("偏移量: " + ip.offset);
				System.out.println("TTL: " + ip.hop_limit);
				System.out.println("协议: " + ip.protocol);
				System.out.println("源IP地址: " + ip.src_ip.getHostAddress());
				System.out.println("目的IP地址: " + ip.dst_ip.getHostAddress());
				System.out.println("");
			}
			if(ip instanceof UDPPacket)
			{
				System.out.println("运输层报文类型:UDP");
				UDPPacket up = (UDPPacket) ip;
				System.out.println("源端口: " + up.src_port);
				System.out.println("目标端口: " + up.dst_port);
				System.out.println("长度: " + up.length);
				System.out.println("");
			}
			if(ip instanceof TCPPacket)
			{
				System.out.println("运输层报文类型:TCP");
				TCPPacket tp = (TCPPacket) ip;
				System.out.println("源端口: " + tp.src_port);
				System.out.println("目的端口: " + tp.dst_port);
				System.out.println("序号: " + tp.sequence);
				System.out.println("确认号: " + tp.ack_num);
				System.out.println("URG:" + tp.urg + "/n");
				System.out.println("ACK:" + tp.ack + "/n");
				System.out.println("PSH:" + tp.psh + "/n");
				System.out.println("RST:" + tp.rst + "/n");
				System.out.println("SYN:" + tp.syn + "/n");
				System.out.println("FIN:" + tp.fin + "/n");
				System.out.println("窗口大小: " + tp.window);
				System.out.println("紧急指针: " + tp.urgent_pointer);
				System.out.println("数据:"+tp.data.toString());
				System.out.println("");
				if (tp.src_port == 80 || tp.dst_port == 80) {
					System.out.println("应用层报文类型: HTTP");
					byte[] data = tp.data;
					if (data.length == 0) {
						System.out.println("此为不带数据的应答报文");
					} else {
						StringBuffer Str = new StringBuffer();
						if (tp.src_port == 80) {
							String str = null;
							try {
								String str1 = new String(data, "UTF-8");
								if (str1.contains("HTTP/1.1")) {
									str = str1;
								} else {
									String str2 = new String(data, "GB2312");
									if (str2.contains("HTTP/1.1")) {
										str = str2;
									} else {
										String str3 = new String(data, "GBK");
										if (str3.contains("HTTP/1.1")) {
											str = str3;
										} else {
											str = new String(data, "Unicode");
										}
									}
								}
								Str.append(str + "\n");
							} catch (UnsupportedEncodingException e) {
								e.printStackTrace();
							}
						}
						if (tp.dst_port == 80) {
							try {
								String str = new String(data, "ASCII");
								Str.append(str);
							} catch (Exception e) {

							}
						}
						System.out.println(Str);
					}
				}
				
			}
			
		}
		if(packet instanceof ICMPPacket)
		{
			System.out.println("网络层报文类型:ICMP");
		}
	}
}
