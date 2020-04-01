package serialPort;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Scanner;

import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.UnsupportedCommOperationException;
import serialPort.*;

public class ReceiveMessage {
	public static int getValidLength(byte[] bytes){
	    int i = 0;
	    if (null == bytes || 0 == bytes.length)
	        return i ;
	    for (; i < bytes.length; i++) {
	        if (bytes[i] == '\0')
	            break;
	    }
	    return i + 1;
	}
	public static void main(String[] args) throws IOException, UnsupportedCommOperationException, PortInUseException, NoSuchPortException {
		SerialTool serialTool = null;
		String configFile="E:\\University\\Study\\ThreeDown\\计算机网络\\协议实验\\lab2\\2.cfg";
		InputStream is = new FileInputStream(configFile);
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		String portName = "";
		int baudRate, dataBits, Parity, stopBits;
		String[] config=new String[10];
		config[0] = reader.readLine();
		portName = config[0] .split(" ")[1];
		config[1] = reader.readLine();
		String Baud = config[1] .split(" ")[1];
		baudRate = Integer.parseInt(Baud);
		config[2]  = reader.readLine();
		String Data = config[2] .split(" ")[1];
		dataBits = Integer.parseInt(Data);
		config[3]  = reader.readLine();
		String Par = config[3] .split(" ")[1];
		Parity = Integer.parseInt(Par);
		config[4]  = reader.readLine();
		String Stop = config[4] .split(" ")[1];
		stopBits = Integer.parseInt(Stop);
		is.close();
		serialTool=serialTool.getSerialTool();
		SerialPort Port = serialTool.openPort(portName);
		Port.setSerialPortParams(baudRate, dataBits, stopBits, Parity);
		System.out.println("Current Config File:"+configFile);
		System.out.println("SerialPort: "+Port.getName());
		System.out.println("BaudRate: "+Port.getBaudRate());
		System.out.println("DataBits: "+Port.getDataBits());
		System.out.println("Parity: "+Port.getParity());
		System.out.println("stopBits: "+Port.getStopBits());
		Scanner input = new Scanner(System.in);
		System.out.println("Begin connecting……");
		boolean flag=true;
		while(flag) {
			byte[] hexmsg = serialTool.readFromPort(Port);
//			System.out.println(hexmsg);
			while(getValidLength(hexmsg)>0)
			{
				String msg=new String(hexmsg,"utf-8");
				System.out.println("Received message:"+msg);
				if(msg.equals("exit")) {
					flag=false;break;
				}
				hexmsg = serialTool.readFromPort(Port);
			}
		}
		serialTool.closePort(Port);
		System.out.println("The port has been closed!Goodbye!");
	}
}
