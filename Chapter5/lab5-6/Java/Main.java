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
			System.out.println("��������Ϣ��");
			return;
		}
		for (int i = 0; i < devices.length; i++) {
			NetworkInterface ni = devices[i];
			System.out.println("---- " + "�豸: " + i + " ----");
			System.out.println("����: " + ni.name);
			System.out.println("����: " + ni.description);
			System.out.println("������·����: " + ni.datalink_name);
			System.out.println("������·����: " + ni.datalink_description);
			System.out.println("Loopback�豸: " + ni.loopback);
			System.out.println("MAC��ַ: " + Util.macToString(ni.mac_address));
			System.out.println("");
		}
		Scanner sc = new Scanner(System.in);
		
		System.out.println("-------------------------------------------");
		System.out.print(">>�������ڲ��豸��: ");
		int device1 = sc.nextInt();
		System.out.print(">>�������ⲿ�豸��: ");
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
