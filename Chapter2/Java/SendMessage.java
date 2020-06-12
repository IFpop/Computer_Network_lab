package serialPort;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
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

public class SendMessage {
	public static void main(String[] args) throws IOException, UnsupportedCommOperationException, PortInUseException, NoSuchPortException {
		SerialTool serialTool = null;
		serialTool = serialTool.getSerialTool();
		String configFile="E:\\University\\Study\\ThreeDown\\计算机网络\\协议实验\\lab2\\1.cfg";
		InputStream is = new FileInputStream(configFile);
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		String portName = "";
		int baudRate, dataBits, Parity, stopBits;
		//Get the config from the file
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
		SerialPort Port = serialTool.openPort(portName);
		Port.setSerialPortParams(baudRate, dataBits, stopBits, Parity);
		System.out.println("Current Config File:"+configFile);
		// print the cnfig
		System.out.println("SerialPort: "+Port.getName());
		System.out.println("BaudRate: "+Port.getBaudRate());
		System.out.println("DataBits: "+Port.getDataBits());
		System.out.println("Parity: "+Port.getParity());
		System.out.println("stopBits: "+Port.getStopBits());
		Scanner input = new Scanner(System.in);
		System.out.println("Begin connecting……");
		while (true) {
			System.out.print("Please input the message to send:");
			String msg = input.nextLine();
			if (msg.length() > 0) {
				try {
					byte[] test = msg.getBytes("utf-8");
					serialTool.sendToPort(Port, test);
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
//				System.out.println(msg+(msg.equals("exit")));
				if(msg.equals("exit")) break;
			}
		}
		serialTool.closePort(Port);
		input.close();
		System.out.println("The port has been closed!Goodbye!");
	}
}
