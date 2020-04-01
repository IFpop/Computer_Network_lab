package chapter3.lab4;

import java.util.*;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.*;

public class User implements PhysicalNetworkLayer
{
	User(boolean isSender, String propertyFileName, String name)
	{
		_currentDataIndex = 0;
		_end = false;
		_protocol = new GoBackN(isSender, name, this, 1000);
		_name = name;
		_frameQueue = new LinkedList<FrameInterface>();
		Properties pro = new Properties();
		try
		{
			pro.load(new FileInputStream(propertyFileName));
			_myPort =  Integer.parseInt(pro.getProperty("receivePort"));
			_portToSend =  Integer.parseInt(pro.getProperty("sendPort"));
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		
		try 
		{
			_myAddress = InetAddress.getLocalHost();
		}
		catch (UnknownHostException e) {
			e.printStackTrace();
		}
		
		try
		{
			startCommunication();
			
		}
		catch (SocketException e)
		{
			e.printStackTrace();
		}
		
		// Test code
		/*try
		{
			if (isSender)
			{
				sendTestMsg();
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		
		try
		{
			_udpReceiver.join();
		}
		catch (InterruptedException e)
		{
			e.printStackTrace();
		}*/ 
		
		terminateCommunication();
	}
	
	private void startCommunication() throws SocketException
	{
		connect();
		_udpReceiver = new UDPReceiver(this);
		_udpReceiver.start();
		_protocol.runProtocol();
	}
	
	private void terminateCommunication()
	{
		_end = true;
		System.out.println(_name + " has exited.");
	}
	
	private void connect() throws SocketException
	{
		_socket = new DatagramSocket(_portToSend);
	}
	
	// Call by other functions
	private void sendDataToPhysicalLayer(byte[] data) throws IOException
	{
		DatagramPacket datagramPacket = new DatagramPacket(data, data.length, _myAddress, _myPort);
		_socket.send(datagramPacket);
	}
	
	// Call by another thread
	public void receiveDataFromPhysicalLayer(byte[] data)
	{
		// Test code
		//System.out.println("received data:" + new String(data));
		
		FrameInterface frame = new Frame(_name);
		frame.fromPhysicalLayer(data);
		if (frame.isError())
		{
			_protocol.checkSumError();
		}
		else
		{
			_frameQueue.offer(frame);
			_protocol.receiveDataFromPhysicalLayer();
		}
		
	}
	
	public void toPhysicalLayer(FrameInterface frame)
	{
		byte[] data = frame.toPhysicalLayer();
		try
		{
			sendDataToPhysicalLayer(data);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	public FrameInterface fromPhysicalLayer()
	{
		return _frameQueue.poll();
	}
	
	public String fromNetworkLayer()
	{
		if (_currentDataIndex == TEST_DATA.length)
		{
			return null;
		}
		String result = new String();
		for (int i = 0; i < Frame.PACKET_SIZE && _currentDataIndex < TEST_DATA.length; i++, _currentDataIndex++)
		{
			result += (char)TEST_DATA[_currentDataIndex];
		}
		return result;
	}
	public void toNetworkLayer(String data)
	{
		
	}
	
	// Test Code
	/*
	private void sendTestMsg() throws IOException
	{
		byte[] hello = "Hello World".getBytes();
		DatagramPacket datagramPacket = new DatagramPacket(hello, hello.length , _myAddress, _myPort);
		_socket.send(datagramPacket);
	}*/
	
	public DatagramSocket getSocket()
	{
		return _socket;
	}
	
	public boolean hasEnded()
	{
		return _end;
	}
	
	UDPReceiver _udpReceiver;
	private InetAddress _myAddress;
	private int _myPort;
	private int _portToSend;
	private DatagramSocket _socket;
	private Protocol _protocol;
	private String _name;
	private boolean _end;
	private Queue<FrameInterface> _frameQueue; 
	
	private int _currentDataIndex;
	private static final byte[] TEST_DATA = "abcdefghijklmnopqrstuvwxyz2abcdefghijklmnopqrstuvwxyz3abcdefghijklmnopqrstuvwxyz".getBytes();
	//private static final byte[] TEST_DATA = "abcdefghijklmnopqrstuvwxyz1234567890abcdefghijklmnopqrstuvwxyz1234567890abcdefghijklmnopqrstuvwxyz1234567890abcdefghijklmnopqrstuvwxyz1234567890abcdefghijklmnopqrstuvwxyz1234567890abcdefghijklmnopqrstuvwxyz1234567890".getBytes();
}
