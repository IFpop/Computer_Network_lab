package chapter3.lab2;

import java.util.*;
import java.io.*;

public class BitStuffing 
{
	private static String inputString;
	private static String flag;
	
	private static void getInput(String fileName) throws IOException
	{
		Properties pro = new Properties();
		pro.load(new FileInputStream(fileName));
		inputString =  pro.getProperty("InfoString");
		flag = pro.getProperty("FlagString");
	}
	
	public static void main(String args[]) throws IOException
	{
		if (args.length <= 1)
		{
			getInput("lab3-2_bit.cfg");
		}
		else
		{
			getInput(args[1]);
		}
		
		System.out.println("Start Flag: " + flag);
		System.out.println("Data: " + inputString);
		System.out.println("End Flag: " + flag);
		
		String result = StuffingAlgorithm.bitStuffing(inputString, flag);
		
		System.out.println("Frame: " + result);
		
		String data = StuffingAlgorithm.bitDeStuffing(result, flag);
		
		System.out.println("Data after destuffing: " + data);
	}
}
