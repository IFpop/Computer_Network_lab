package network.chapter5.lab8;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.URL;
import java.util.*;

import jpcap.packet.Packet;
import jpcap.packet.DatalinkPacket;
import jpcap.packet.EthernetPacket;
import jpcap.packet.ICMPPacket;
import jpcap.packet.ARPPacket;
import jpcap.packet.IPPacket;
import jpcap.packet.TCPPacket;
import jpcap.packet.UDPPacket;

import jpcap.JpcapCaptor;
import jpcap.JpcapSender;
import jpcap.NetworkInterface;
import jpcap.NetworkInterfaceAddress;
import jpcap.PacketReceiver;

public class JavaTracert 
{
	private NetworkInterface[] devices;
	private TracertLog tracertLog[];
	private long secArray[];
	private long usecArray[];
	public static final int PACKET_CNT = 3;
	private int packetTTL;
	private int currentPacketIndex;
	private String dstIP;
	
	JavaTracert()
	{
		devices = JpcapCaptor.getDeviceList();
		currentPacketIndex = 0;
	}
	

	public void tracert(String ip, int maxTTL, int deviceId) 
	{
		dstIP = ip;
		
		System.out.println("最多通过 " + maxTTL + " 个跃点追踪");
		System.out.println("到 " + ip + " 的路由:");
		
		try 
		{
			JpcapCaptor jpcap = JpcapCaptor.openDevice(devices[deviceId], 200, false, 20);
			JpcapSender sender = jpcap.getJpcapSenderInstance();
			jpcap.setFilter("icmp", true);
			
			ICMPPacket[] packets = new ICMPPacket[maxTTL];
			secArray = new long[maxTTL * PACKET_CNT];
			usecArray = new long[maxTTL * PACKET_CNT];
			tracertLog = new TracertLog[maxTTL];
			
			InetAddress myAddress = null;
			for(NetworkInterfaceAddress addr:devices[deviceId].addresses)
			{
				if(addr.address instanceof Inet4Address){
					myAddress = addr.address;
                    break;
               }
			}
			System.out.println("My IP is " + myAddress);
	               
			
			
			for (int i = 0; i < maxTTL; i++)
			{
				packetTTL = i;
				packets[i] = new ICMPPacket();
				
				ICMPPacket icmpPacket = packets[i];
				icmpPacket.type = ICMPPacket.ICMP_ECHO;
								
				icmpPacket.setIPv4Parameter(0, false, false, false, 0, false, false, false, 0, 0, i + 1,
						IPPacket.IPPROTO_ICMP, myAddress, InetAddress.getByName(ip));

				icmpPacket.data = new byte[64];
				EthernetPacket ethernetPacket = new EthernetPacket();
				ethernetPacket.frametype = EthernetPacket.ETHERTYPE_IP;
				ethernetPacket.src_mac = devices[deviceId].mac_address;

//				ethernetPacket.dst_mac = new byte[] { (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff,
//						(byte) 0xff };
				
				
				// 这个mac地址需要自己在其它地方抓包并且保存下来
				ethernetPacket.dst_mac = new byte[] { (byte) 0xb4, (byte) 0x1d, (byte) 0x2b, (byte) 0xf9, (byte) 0x8a,
						(byte) 0xc8 };
				
				icmpPacket.datalink = ethernetPacket;
				
				boolean out = false;
				for (int packetCnt = 0; packetCnt < PACKET_CNT; packetCnt++, currentPacketIndex++)
				{
					icmpPacket.sec = 0;
					icmpPacket.usec = new GregorianCalendar().getTimeInMillis() * 1000;
					icmpPacket.seq = (short)(i * PACKET_CNT + packetCnt);
					icmpPacket.id = (short)(icmpPacket.seq);
					
					usecArray[icmpPacket.seq] = icmpPacket.usec;
					secArray[icmpPacket.seq] = icmpPacket.sec;
					
					sender.sendPacket(icmpPacket);
					boolean received = false;
					
					while (new GregorianCalendar().getTimeInMillis() * 1000 - icmpPacket.usec < 1000000)
					{
						Packet packet = jpcap.getPacket();
						if (packet instanceof ICMPPacket)
						{
							if (getIcmpPacket(packet))
							{
								out = true;
							}
							received = true;
							break;
						}
					}
					if (!received)
					{
						getIcmpPacket(null);
					}
				}
				if (out)
				{
					break;
				}
			}
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}
	}

	// return true if the find dest ip
	public boolean getIcmpPacket(Packet packet) 
	{
		long timeInMs = 0;
		if (packet == null)
		{
			if (tracertLog[packetTTL] == null)
			{
				tracertLog[packetTTL] = new TracertLog(packetTTL, "超时");
			}
			if (tracertLog[packetTTL].addLog(-1))
			{
				tracertLog[packetTTL].output();
			}
			return false;
		}
		else
		{
			//System.out.println("catch");
			ICMPPacket rp = (ICMPPacket)packet;
			
			long sendSec = secArray[currentPacketIndex];
			long sendUsec = usecArray[currentPacketIndex];
			timeInMs = (rp.sec * 1000 + rp.usec / 1000 - sendSec * 1000 - sendUsec / 1000);
			if (tracertLog[packetTTL] == null)
			{
				tracertLog[packetTTL] = new TracertLog(packetTTL, rp.src_ip.getHostAddress());
			}
			if (tracertLog[packetTTL].addLog(timeInMs))
			{
				tracertLog[packetTTL].output();
			}
			if (rp.src_ip.getHostAddress().equals(dstIP) )
			{
				return true;
			}
			else
			{
				return false;
			}
		}
	}

}
