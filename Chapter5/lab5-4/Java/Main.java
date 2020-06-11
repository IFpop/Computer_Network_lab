package network.chapter5.lab4;
import java.io.FileInputStream;
import java.util.*;
public class Main 
{
	private static String filename = "lab5-4.properties";
	public static void main(String args[]) throws Exception
	{
		Properties pro = new Properties();
		pro.load(new FileInputStream(filename));
		String headerStr = pro.getProperty("IPHeader");
		
		System.out.println("IPHeader: " + headerStr);
		IPHeader ipHeader = new IPHeader(headerStr);
		ipHeader.output();
		
		int cksum = ipHeader.calChecksum();
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
