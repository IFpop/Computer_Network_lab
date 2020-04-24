package network.chapter4.lab2;
import java.util.Arrays;

import network.chapter4.lab2.*;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.net.InetAddress;
import java.util.GregorianCalendar;
import java.util.Scanner;

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
import jpcap.PacketReceiver;

public class CommunicationMain 
{
	public static void main(String[] args) throws IOException
	{
		String add = "192.168.1.3";
		
		NetworkInterface[] devices = JpcapCaptor.getDeviceList();
		JpcapCaptor jpcap = JpcapCaptor.openDevice(devices[0], 10000, false, 20);
		JpcapSender sender = jpcap.getJpcapSenderInstance();
		
		ICMPPacket icmpPacket = new ICMPPacket();
		icmpPacket.type = ICMPPacket.ICMP_ECHO;

		icmpPacket.setIPv4Parameter(0, false, false, false, 0, false, false, false, 0, 1010101, 100,
				IPPacket.IPPROTO_ICMP, devices[0].addresses[1].address, InetAddress.getByName(add));
		icmpPacket.data = "abc".getBytes();
		EthernetPacket ethernetPacket = new EthernetPacket();
		ethernetPacket.frametype = EthernetPacket.ETHERTYPE_IP;
		ethernetPacket.src_mac = devices[0].mac_address;
		ethernetPacket.dst_mac = devices[5].mac_address;
		icmpPacket.datalink = ethernetPacket;
		System.out.println("src: " + Util.macToString(devices[0].mac_address) + ". dst: " + Util.macToString(devices[5].mac_address));
		for (int i = 0; i < 4; i++) {
			icmpPacket.sec = 0;
			icmpPacket.usec = new GregorianCalendar().getTimeInMillis();// 记录发送时间
			icmpPacket.seq = (short) (1000 + i);
			icmpPacket.id = (short) (999 + i);
			sender.sendPacket(icmpPacket);
		}
	}
}
