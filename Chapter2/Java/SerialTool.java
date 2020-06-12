package serialPort;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.TooManyListenersException;

import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.SerialPortEventListener;
import gnu.io.UnsupportedCommOperationException;

public class SerialTool {
	private static SerialTool serialTool = null;
	//��ʼ��
	public static SerialTool getSerialTool() {
		if (serialTool == null) {
			serialTool = new SerialTool();
		}
		return serialTool;
	}
	//�������п��ö˿�
	public static final ArrayList<String> findPort() {
		Enumeration<CommPortIdentifier> portList = CommPortIdentifier.getPortIdentifiers();
		ArrayList<String> portNameList = new ArrayList<>();
		while (portList.hasMoreElements()) {
			String portName = portList.nextElement().getName();
			portNameList.add(portName);
		}
		return portNameList;
	}
     //�򿪴���
	 public static final SerialPort openPort(String portName)  {
	        try {
	            CommPortIdentifier portIdentifier = CommPortIdentifier.getPortIdentifier(portName);
	            CommPort commPort = portIdentifier.open(portName, 2000);
	            //�ж��ǲ��Ǵ���
	            if (commPort instanceof SerialPort) { 
	                SerialPort serialPort = (SerialPort) commPort;
	                return serialPort;
	            }        
	        } catch (NoSuchPortException e1) {
	        	System.out.println(e1);
	        } catch (PortInUseException e2) {
	        	System.out.println(e2);
	        }
			return null;
	    }
	// �رմ���
	public static void closePort(SerialPort serialPort) {
		if (serialPort != null) {
			serialPort.close();
			serialPort = null;
		}
	}
	//�����ڷ�������
	 public static void sendToPort(SerialPort serialPort, byte[] order)  {
	        OutputStream out = null;
	        try {
	            
	            out = serialPort.getOutputStream();
	            out.write(order);
	            out.flush();
	        } catch (IOException e) {
	        	System.out.println(e);
	        } finally {
	            try {
	                if (out != null) {
	                    out.close();
	                    out = null;
	                }                
	            } catch (IOException e) {
	            	System.out.println(e);
	            }
	        }
	        
	    }

	//�Ӵ��ڶ�ȡ���ݵĺ���
	 public static byte[] readFromPort(SerialPort serialPort) {

	        InputStream in = null;
	        byte[] bytes = null;
	        try {
	            in = serialPort.getInputStream();
	            int bufflenth = in.available();      
	            while (bufflenth != 0) {                             
	                bytes = new byte[bufflenth]; 
	                in.read(bytes);
	                bufflenth = in.available();
	            } 
	        } catch (IOException e) {
	        	System.out.println(e);
	        } finally {
	            try {
	                if (in != null) {
	                    in.close();
	                    in = null;
	                }
	            } catch(IOException e) {
	            	System.out.println(e);
	            }
	        }
	        return bytes;
	    }
}