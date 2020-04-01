package serialPort;
import java.util.ArrayList;

import serialPort.*;
public class MainFunction {
	public static void main(String[] args)
	{
		SerialTool serialTool = null;
		ArrayList<String> PortName=serialTool.findPort();
		System.out.println(PortName);
	}
}
