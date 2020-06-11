package network.chapter6.lab6;

import java.util.*;
import java.io.*;

public class Main 
{
	public static String filename = "lab6-6.properties";
	
	public static void main(String args[]) throws Exception
	{
		Properties pro = new Properties();
		pro.load(new FileInputStream(filename));
		int mss = Integer.parseInt(pro.getProperty("MSS"));
		int threshold = Integer.parseInt(pro.getProperty("Threshold"));		
		int triack = Integer.parseInt(pro.getProperty("TriACKRound"));
		int timeout =Integer.parseInt(pro.getProperty("TimeoutRound"));
		int end =Integer.parseInt(pro.getProperty("EndRound"));
		
		Receiver receiver = new Receiver();
		Sender sender = new Sender(mss, threshold, triack, timeout, end, receiver);
		sender.start();
	}
}
