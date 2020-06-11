package network.chapter5.lab6;

import network.chapter4.lab2.Util;
import network.chapter5.lab4.IPHeader;
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

public class Adapter implements PacketReceiver
{
	private NatTable _natTable;
	private byte[] _dstMac;
	private int _fromDevice;
	private int _toDevice;
	
	private NetworkInterface[] _deviceList;
	public JpcapCaptor jpcap;
	
	public Adapter(NatTable natTable, byte[] dstMac, int fromDevice, int toDevice, NetworkInterface[] deviceList, JpcapCaptor jCaptor)
	{
		_natTable = natTable;
		_dstMac = dstMac;
		_fromDevice = fromDevice;
		_toDevice = toDevice;
		_deviceList = deviceList;
		jpcap = jCaptor;
	}
	
	@Override
	public void receivePacket(Packet packet) 
	{
		if (!(packet instanceof IPPacket))
		{
			System.out.println("Error! The packet must be a IP packet due to the filter");
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
		
		int tcpSrcPort = Util.byteToUint((byte)(ipPacket.header[34] << 8)) + Util.byteToUint((byte)(ipPacket.header[35])); 
		int tcpDstPort = Util.byteToUint((byte)(ipPacket.header[36] << 8)) + Util.byteToUint((byte)(ipPacket.header[37])); 
		int srcIP = ipHeader.getSrcIP();
		int dstIP = ipHeader.getDstIP();
		
		System.out.println("----------------------------------");
		System.out.println("Source IP: " + Util.toIPAddress(srcIP) + " Port: " + tcpSrcPort);
		System.out.println("Destination IP: " + Util.toIPAddress(dstIP) + " Port: " + tcpDstPort);
		
		int newSrcIP = _natTable.changeIP(srcIP);
		ipHeader.setSrcIP(newSrcIP);
		newSrcIP = ipHeader.getSrcIP();
		int newChecksum = ipHeader.recalChecksum();
		
		System.out.println("New source IP: " + Util.toIPAddress(newSrcIP) + " Port: " + tcpSrcPort);
		System.out.println("New IP checksum: " + newChecksum);
		
		_natTable.output();
		
		// modify mac
		for (int i = 0; i < 6; i++)
		{
			packet.header[i] = _dstMac[i];
		}
		// Modify ip
		for (int i = 26, j = 3; i < 30; i++, j--)
		{
			packet.header[i] = (byte)((newSrcIP >> j * 8) & 0xff);
		}
		// Modify checksum
		for (int i = 24, j = 1; i < 26; i++, j--)
		{
			packet.header[i] = (byte)((newChecksum >> j * 8) & 0xff);
		}
		
		System.out.println("MAC header: " + Util.bytesToSHex(packet.header));
		System.out.println("MAC data: " + Util.bytesToSHex(packet.data));
		
		try
		{
			JpcapCaptor cap = JpcapCaptor.openDevice(_deviceList[_toDevice], 10000, true, 50);
			JpcapSender sender = cap.getJpcapSenderInstance();
			sender.sendPacket(packet);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
	}
}
