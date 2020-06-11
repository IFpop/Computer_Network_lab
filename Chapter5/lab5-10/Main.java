package network.chapter5.lab10;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.Scanner;

import jpcap.JpcapCaptor;
import jpcap.NetworkInterface;
import network.chapter4.lab2.Util;

public class Main 
{
	private static String proFilename = "lab5-10.properties";
	private static String raFilename = "lab5-10RA.properties";
	private static String rbFilename = "lab5-10RB.properties";
	
	
	public static void main(String[] args) throws Exception
	{
		Properties pro = new Properties();
		pro.load(new FileInputStream(proFilename));
		String raMac = pro.getProperty("RAMAC");
		String rbMac = pro.getProperty("RBMAC");
		
		RoutingTable raTable = new RoutingTable(raFilename);
		RoutingTable rbTable = new RoutingTable(rbFilename);
		
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
			JpcapCaptor capRA = JpcapCaptor.openDevice(devices[device1], 10000, true, 50);
			capRA.setFilter("ip", true);
			PortListener raListener = new PortListener(
					raTable,
					Util.stringToMac(raMac, "-"),
					device1,
					device2,
					devices,
					capRA);
			
			JpcapCaptor capRB = JpcapCaptor.openDevice(devices[device2], 10000, true, 50);
			capRB.setFilter("ip", true);
			PortListener rbListener = new PortListener(
					rbTable,
					Util.stringToMac(rbMac, "-"),
					device2,
					4,
					devices,
					capRB);
			
			raListener.start();
			rbListener.start();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
}
