package network.chapter6.lab3;

import java.io.FileInputStream;
import java.util.*;

import network.chapter4.lab2.Util;
public class Main 
{
	private static String filename = "lab6-3.properties";
	public static void main(String args[]) throws Exception
	{
		Properties pro = new Properties();
		pro.load(new FileInputStream(filename));
		
		String tcpData = pro.getProperty("tcpsegment");
		int len = tcpData.length();
		String header = tcpData.substring(0, 40);
		byte[] data = Util.hexToByte(tcpData.substring(40, len));
		byte[] srcIP = Util.ipToBytes(pro.getProperty("sourceip"));
		byte[] dstIP = Util.ipToBytes(pro.getProperty("destip"));
		
		TCPHeader tcpHeader = new TCPHeader(header);
		tcpHeader.output();
		System.out.println("");
		
		int cksum = tcpHeader.calChecksum(srcIP, dstIP, data);
		System.out.println("Checksum by calculating: " + cksum);
		if (cksum == 0)
		{
			System.out.println("Checksum is 0, correct");
		}
		else
		{
			System.out.println("Checksum is incorrect");
		}
		
	}
}
