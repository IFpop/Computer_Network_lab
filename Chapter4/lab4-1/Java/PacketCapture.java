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
				mac+=Integer.toHexString(b&0xff)+"-";
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
			System.out.println("No device found！");
			return;
		}
		System.out.println("Following are the devices found：");
		int num=0;
		for (NetworkInterface n : devices) {
			System.out.println("------------ Device ID:"+num+"------------------");
			System.out.println("Device Name:"+n.name);
			System.out.println("Device Description:"+n.description);
			int len=0;
			String mac=GetMac(n.mac_address);
			System.out.println("Device MAC Address："+mac);
			num++;
		}
		System.out.println("-------------------------------------------");
		while(true)
		{
			Scanner scanner=new Scanner(System.in);
			System.out.print("Please input the ID of device to capture(input -1 to exit):");
			int Id=scanner.nextInt();
			if(Id==-1) break;
			int NumOfDev=devices.length;
			if(Id>=NumOfDev||Id<0)
			{
				System.out.println("No such device found!Please input again.");
				continue;
			}
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
				jpcap.loopPacket(1000, new PacketCapture());//是receivePacket的回调函数
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
		System.out.println("Actual Capture Length: " + packet.caplen);
		System.out.println("Real Packet Length: " + packet.len);
		System.out.println("");
		//数据链路层
		DatalinkPacket DataLink=packet.datalink;
		if(DataLink instanceof EthernetPacket)
		{
			System.out.println("MAC address and other values:");
			EthernetPacket Ep=(EthernetPacket)DataLink;
			//获取mac地址
			System.out.println("src MAC address:"+GetMac(Ep.src_mac));
			System.out.println("des MAC address"+GetMac(Ep.dst_mac));
			System.out.println("Ether type："+Integer.toHexString(Ep.frametype & 0xffff));
			System.out.println("");
		}
		else 
		{
			System.out.println("Other Data Frame");
			System.out.println("Content of the frame:"+packet);
			System.out.println("");
		}
		if(packet instanceof ARPPacket)
		{
			System.out.println("Network layer message type:ARP");
			ARPPacket ap = (ARPPacket) packet;
			System.out.println("Hardware type: " + ap.hardtype);
			System.out.println("Protocol type: " + ap.prototype);
			System.out.println("Hardware address length: " + ap.hlen);
			System.out.println("Protocol address length: " + ap.plen);
			System.out.println("Opcode: " + ap.operation);
			System.out.println("src MAC address: " + ap.getSenderHardwareAddress());
			System.out.println("des MAC address: " + ap.getTargetHardwareAddress());
			System.out.println("src IP address: " + ap.getSenderProtocolAddress());
			System.out.println("des IP address: " + ap.getTargetProtocolAddress());
			System.out.println("");
			
		}
		if(packet instanceof IPPacket)
		{
			
			IPPacket ip=(IPPacket)packet;
			System.out.println("Network layer message type:IP");
			if(ip.version==4)//ipv4版本号为4
			{
				System.out.println("Type of service: " + ip.rsv_tos);
				System.out.println("Priority: " + ip.priority);
				System.out.println("Length: " + ip.length);
				System.out.println("Identification: " + ip.ident);
				System.out.println("Offset: " + ip.offset);
				System.out.println("TTL: " + ip.hop_limit);
				System.out.println("Protocol: " + ip.protocol);
				System.out.println("src IP address: " + ip.src_ip.getHostAddress());
				System.out.println("des IP address: " + ip.dst_ip.getHostAddress());
				System.out.println("");
			}
			if(ip instanceof UDPPacket)
			{
				System.out.println("Transport layer message type:UDP");
				UDPPacket up = (UDPPacket) ip;
				System.out.println("src port: " + up.src_port);
				System.out.println("des port: " + up.dst_port);
				System.out.println("Length: " + up.length);
				System.out.println("");
			}
			if(ip instanceof TCPPacket)
			{
				System.out.println("Transport layer message type:TCP");
				TCPPacket tp = (TCPPacket) ip;
				System.out.println("src port: " + tp.src_port);
				System.out.println("des port: " + tp.dst_port);
				System.out.println("ID: " + tp.sequence);
				System.out.println("ACK ID: " + tp.ack_num);
				System.out.println("URG:" + tp.urg + "/n");
				System.out.println("ACK:" + tp.ack + "/n");
				System.out.println("PSH:" + tp.psh + "/n");
				System.out.println("RST:" + tp.rst + "/n");
				System.out.println("SYN:" + tp.syn + "/n");
				System.out.println("FIN:" + tp.fin + "/n");
				System.out.println("Window Size: " + tp.window);
				System.out.println("Emergency pointer: " + tp.urgent_pointer);
				System.out.println("");
				if (tp.src_port == 80 || tp.dst_port == 80) {
					System.out.println("Application layer message type: HTTP");
				}
			}
		}
		if(packet instanceof ICMPPacket)
		{
			System.out.println("Network layer message type:ICMP");
			ICMPPacket icmp=(ICMPPacket)packet;
			System.out.println("Type:"+icmp.type);
			System.out.println("Code:"+icmp.code);
			System.out.println("Checksum:"+icmp.checksum);
			System.out.println("Identifier:"+icmp.ident);
			System.out.println("Seq Num:"+icmp.seq);
		}
	}
}
