package network.chapter4.lab2;

import network.chapter4.lab2.*;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
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
	public static void main(String[] args) 
	{
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
		System.out.print(">>�������һ��������豸��: ");
		int device1 = sc.nextInt();
		System.out.print(">>������ڶ���������豸��: ");
		int device2 = sc.nextInt();
		
		Bridge bridge = new Bridge();
		try
		{
			JpcapCaptor cap1 = JpcapCaptor.openDevice(devices[device1], 10000, true, 50);
			JpcapCaptor cap2 = JpcapCaptor.openDevice(devices[device2], 10000, true, 50);
			AdapterThread thread1 = new AdapterThread(new Adapter(devices[device1].mac_address, device1, devices[device2].mac_address, devices, bridge), cap1);
			AdapterThread thread2 = new AdapterThread(new Adapter(devices[device2].mac_address, device2, devices[device1].mac_address, devices, bridge), cap2);
			thread1.start();
			thread2.start();
			try
			{
				thread1.join();
				thread2.join();
			}
			catch (InterruptedException e)
			{
				e.printStackTrace();
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		
	}
}
