package chapter3.lab2;

import java.util.*;
import java.io.*;

public class ByteStuffing 
{
	private static String inputString;
	private static char flag;
	
	private static String hexToByte(String input)
	{
		String result = new String();
		int strLen = input.length();
		for (int i = 0; i + 1 < strLen; i += 2)
		{
			result += (char)Integer.parseInt(input.substring(i, i + 2), 16);
		}
		
		return result;
	}
	
	private static String byteToHex(String input)
	{
		String result = new String();
		int strLen = input.length();
		for (int i = 0; i < strLen; i++)
		{
			result += Integer.toHexString(input.charAt(i));
		}
		return result.toUpperCase();
	}
	
	private static void getInput(String fileName) throws IOException
	{
		Properties pro = new Properties();
		pro.load(new FileInputStream(fileName));
		inputString =  pro.getProperty("InfoString");
		flag = hexToByte(pro.getProperty("FlagString")).charAt(0);
	}
	
	public static void main(String args[]) throws IOException
	{
		if (args.length <= 1)
		{
			getInput("lab3-2_byte.cfg");
		}
		else
		{
			getInput(args[1]);
		}
		
		
		String inputStringInByte = hexToByte(inputString);
				
		System.out.println("Start Flag: " + Integer.toHexString(flag).toUpperCase());
		System.out.println("Data: " + inputString);
		System.out.println("End Flag: " + Integer.toHexString(flag).toUpperCase());
		
		String result = StuffingAlgorithm.byteStuffing(inputStringInByte, flag);
		String resultInHex = byteToHex(result);
		System.out.println("Frame: " + resultInHex);
		
		String data = StuffingAlgorithm.byteDeStuffing(result, flag);
		String dataInHex = byteToHex(data);
		
		System.out.println("Data after destuffing: " + dataInHex);
	}
}
