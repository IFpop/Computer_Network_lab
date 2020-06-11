package network.chapter5.lab10;

import network.chapter4.lab2.Util;
import network.chapter5.lab4.IPHeader;
import network.chapter5.lab6.NatTable;

import java.util.Arrays;
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
import jpcap.JpcapSender;
import jpcap.NetworkInterface;
import jpcap.NetworkInterfaceAddress;
import jpcap.PacketReceiver;

public class PortListener implements PacketReceiver, Runnable
{
	private RoutingTable _routingTable;
	private int _fromDevice;
	private int _toDevice;
	private byte[] _dstMac;
	
	private NetworkInterface[] _deviceList;
	public JpcapCaptor jpcap;
	
	public PortListener(RoutingTable routingTable, byte[] dstMac, int fromDevice, int toDevice, NetworkInterface[] deviceList, JpcapCaptor jCaptor)
	{
		_routingTable = routingTable;
		_dstMac = dstMac;
		_fromDevice = fromDevice;
		_toDevice = toDevice;
		_deviceList = deviceList;
		jpcap = jCaptor;
	}
	
	public void run()
	{
		jpcap.loopPacket(-1, this);
	}
	
	public void start()
	{
		Thread t = new Thread(this, "thread" + _fromDevice);
		t.start();
	}
	
	@Override
	public void receivePacket(Packet packet) 
	{
		if (!(packet instanceof IPPacket))
		{
			return;
		}
		
		IPPacket ipPacket = (IPPacket)packet;
		//System.out.println("Length: " + ipPacket.length);
		//System.out.println(Util.bytesToSHex(ipPacket.header));
		
		// Copy the header to an array from the header of packet
		byte[] ipHeaderArr = new byte[20];
		for (int i = 14, j = 0; j < 20; i++, j++)
		{
			ipHeaderArr[j] = ipPacket.header[i];
		}
		
		// Use byte array to construct an IP header
		IPHeader ipHeader = new IPHeader(ipHeaderArr);
		//ipHeader.output();
		
		//int tcpSrcPort = Util.byteToUint((byte)(ipPacket.header[34] << 8)) + Util.byteToUint((byte)(ipPacket.header[35])); 
		//int tcpDstPort = Util.byteToUint((byte)(ipPacket.header[36] << 8)) + Util.byteToUint((byte)(ipPacket.header[37])); 
		int srcIP = ipHeader.getSrcIP();
		int dstIP = ipHeader.getDstIP();
		int ttl = ipHeader.getTTL();
		
		if (_routingTable.getNextIP(dstIP) == null)
		{
			return;
		}
		
		System.out.println("----------------------------------");
		System.out.println("Device " + _fromDevice + " receive a ip packet");
		System.out.println("Destination IP: " + Util.toIPAddress(dstIP));
		System.out.println("TTL: " + ttl);
		System.out.println("-----Routing Table----");
		System.out.println(_routingTable.toString());
		
		int nextIP = _routingTable.getNextIP(dstIP);
		if (nextIP == 0)
		{
			System.out.println("Next router: Direct");
		}
		else
		{
			System.out.println("Next router: " + Util.toIPAddress(nextIP));
		}
		
		ttl--;
		ipHeader.setTTL(ttl);
		int newChecksum = ipHeader.recalChecksum();
		
		System.out.println("New IP checksum: " + newChecksum);
				
		// modify mac
		for (int i = 0; i < 6; i++)
		{
			ipPacket.header[i] = _dstMac[i];
		}
			
		// Modify checksum
		for (int i = 24, j = 1; i < 26; i++, j--)
		{
			ipPacket.header[i] = (byte)((newChecksum >> j * 8) & 0xff);
		}
		
		try
		{
			JpcapCaptor cap = JpcapCaptor.openDevice(_deviceList[_toDevice], 10000, true, 50);
			JpcapSender sender = cap.getJpcapSenderInstance();
			
			// Modify ttl
			ipPacket.hop_limit = (short)ttl;
			
			sender.sendPacket(ipPacket);
			//cap.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
	}
}
