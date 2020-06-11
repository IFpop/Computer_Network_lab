package network.chapter5.lab6;

import network.chapter4.lab2.Util;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Properties;
import java.util.Scanner;

import jpcap.packet.Packet;
import jpcap.packet.DatalinkPacket;
import jpcap.packet.EthernetPacket;
import jpcap.packet.ARPPacket;
import jpcap.packet.IPPacket;
import jpcap.packet.TCPPacket;
import jpcap.packet.UDPPacket;

import jpcap.JpcapCaptor;
import jpcap.NetworkInterface;
import jpcap.PacketReceiver;

public class Main 
{
	private static String innerIP;
	private static String outerIP;
	private static String dstMac;
	private static String filename = "lab5-6.properties";
	
	private static void getInput() throws IOException
	{
		Properties pro = new Properties();
		pro.load(new FileInputStream(filename));
		innerIP = pro.getProperty("InsideIP");
		outerIP = pro.getProperty("OutsideIP");
		dstMac = pro.getProperty("DestMAC");
	}
	
	public static void main(String[] args) throws Exception
	{
		getInput();
		NatTable natTable = new NatTable();
		natTable.addItem(Util.ipToInt(innerIP), Util.ipToInt(outerIP));
		
		NetworkInterface[] devices = JpcapCaptor.getDeviceList();

		if (devices.length == 0) {
			System.out.println("无网卡信息！");
			return;
		}
		for (int i = 0; i < devices.length; i++) {
			NetworkInterface ni = devices[i];
			System.out.println("---- " + "设备: " + i + " ----");
			System.out.println("名称: " + ni.name);
			System.out.println("描述: " + ni.description);
			System.out.println("数据链路名称: " + ni.datalink_name);
			System.out.println("数据链路描述: " + ni.datalink_description);
			System.out.println("Loopback设备: " + ni.loopback);
			System.out.println("MAC地址: " + Util.macToString(ni.mac_address));
			System.out.println("");
		}
		Scanner sc = new Scanner(System.in);
		
		System.out.println("-------------------------------------------");
		System.out.print(">>请输入内部设备号: ");
		int device1 = sc.nextInt();
		System.out.print(">>请输入外部设备号: ");
		int device2 = sc.nextInt();
		
		try
		{
			JpcapCaptor cap = JpcapCaptor.openDevice(devices[device1], 10000, true, 50);
			cap.setFilter("ip", true);
			Adapter adapter = new Adapter(
					natTable,
					Util.stringToMac(dstMac, "-"),
					device1,
					device2,
					devices,
					cap);
			cap.loopPacket(-1, adapter);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
}
