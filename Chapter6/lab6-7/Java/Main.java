package network.chapter6.lab7;

import java.util.*;
import java.io.*;
import java.math.*;

public class Main 
{
	static String proFilename = "lab6-7.properties";
	
	public static void main(String args[]) throws IOException
	{
		Properties pro = new Properties();
		pro.load(new FileInputStream(proFilename));
		
		String rttArr[] = pro.getProperty("RTT").split(",");
		int rtts[] = new int[rttArr.length];
		for (int i = 0; i < rtts.length; i++)
		{
			rtts[i] = Integer.parseInt(rttArr[i]);
		}
		
		float alpha = Float.parseFloat(pro.getProperty("Alpha"));
		float beta = Float.parseFloat(pro.getProperty("Beta"));
		
		// initialization
		int rtt = rtts[0];
		float srtt = rtt;
		float rttvar = rtt / 2.0f;
		float rto = 0.0f;
		System.out.println("Initial RTT: " + rtt);
		for (int i = 1; i < rtts.length; i++)
		{
			rtt = rtts[i];
			srtt = alpha * srtt + (1 - alpha) * rtt; 
			rttvar = beta * rttvar + (1 - beta) * Math.abs(srtt - rtt);
			rto = rtt + 4 * rttvar;
			System.out.println("Round " + i + ":  RTT = " + rtt + " SRTT = " + srtt + " RTTVAR = " + rttvar + " RTO = " + rto);
		}
	}
}
