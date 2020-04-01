package network.chapter3.lab1;

import java.io.*;
import java.lang.reflect.Array;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Properties;
import network.chapter3.lab1.*;

public class Main {
	static String configPath = "E:\\GoToCode\\Java\\WorkSpace\\ComputerNetworkLab3\\src\\network\\chapter3\\lab1\\config_lab1.properties";
	static String infoString1 = "";
	static String infoString2 = "";
	static String genXString = "";

	public static void main(String[] args) {
		Properties pps = new Properties();
		try {
			InputStream in = new BufferedInputStream(new FileInputStream(configPath));
			pps.load(in);
			infoString1 = pps.getProperty("InfoString1");
			infoString2 = pps.getProperty("InfoString2");
			genXString = pps.getProperty("GenXString");
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("待发送的数据信息二进制比特串:"+infoString1);
		System.out.println("CRC生成多项式对应的二进制比特串:"+genXString);
		System.out.println();
		CRC_CCITT CRC_Test=new CRC_CCITT();
		String CRC_Code1=CRC_CCITT.getCRC16CCITT(infoString1, 0x1021, 0x0000);
		System.out.println("计算得到的CRC校验码:"+CRC_Code1);
		
		System.out.println("带校验和的发送帧"+infoString1+CRC_Code1);
		System.out.println();
//		String getCrcCode=CRC_CCITT.getCRC16CCITT(infoString1+CRC_Code, 0x1021, 0x0000);
		String CRC_Code2=CRC_CCITT.getCRC16CCITT(infoString2, 0x1021, 0x0000);
		System.out.println("接收到的数据信息二进制比特串:"+infoString2);
		System.out.println("计算得到的CRC校验码"+CRC_Code2);
		String CheckString=infoString1+CRC_Code1;
		String Remainder=CRC_CCITT.getCRC16CCITT(CheckString, 0x1021, 0x0000);
		System.out.print("接受码校验余数为："+Remainder);
		if(Integer.valueOf(Remainder)==0) System.out.print("(无错)");
		else System.out.print("(错误)");
	}
}
