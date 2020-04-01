package network.chapter3.lab4;

import java.util.*;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.*;

public class User implements PhysicalNetworkLayer
{
	User(boolean isSender, String propertyFileName, String name)
	{
		_isSender = isSender;
		_frameToSendCounter = 0;
		_currentDataIndex = 0;
		_end = false;
		_protocol = new GoBackN(isSender, name, this, 3000);
		_name = name;
		_frameQueue = new LinkedList<FrameInterface>();
		Properties pro = new Properties();
		try
		{
			pro.load(new FileInputStream(propertyFileName));
			_myPort =  Integer.parseInt(pro.getProperty("receivePort"));
			_portToSend =  Integer.parseInt(pro.getProperty("sendPort"));
			_filterError = Integer.parseInt(pro.getProperty("FilterError"));
			_filterLost = Integer.parseInt(pro.getProperty("FilterLost"));
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
		try
		{
			_udpReceiver.join();
		}
		catch (InterruptedException e)
		{
			e.printStackTrace();
		}
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
		
		FrameInterface frame = new Frame(_name);
		frame.fromPhysicalLayer(data);
		if (frame.isError())
		{
			if (!_isSender)
			{
				System.out.println(_name + " dectect a checksum error, sequence number = " + frame.getSequenceNumber());
			}
			_protocol.checkSumError();
		}
		else
		{
			_frameQueue.offer(frame);
			_protocol.receiveDataFromPhysicalLayer();
		}
		
	}
	
	private boolean skipFrame()
	{
		if (_frameToSendCounter % _filterLost == 0)
		{
			System.out.println("Cause a frame lost event");
			return true;
		}
		return false;
	}
	
	private void filterData(byte[] data)
	{
		if (_frameToSendCounter % _filterError == _filterError / 2)
		{
			data[0] ^= 0b00100000;
			System.out.println("Cause a frame error event");
		}
	}
	
	public void toPhysicalLayer(FrameInterface frame)
	{
		_frameToSendCounter++;
		if (skipFrame())
		{
			return;
		}
		byte[] data = frame.toPhysicalLayer();
		filterData(data);
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
	private int _frameToSendCounter;
	private int _filterError;
	private int _filterLost;
	private boolean _isSender;
	
	private DatagramSocket _socket;
	private Protocol _protocol;
	private String _name;
	private boolean _end;
	private Queue<FrameInterface> _frameQueue; 
	
	private int _currentDataIndex;
	private static final byte[] TEST_DATA = "1abcdefghijklmnopqrstuvwxyz2abcdefghijklmnopqrstuvwxyz3abcdefghijklmnopqrstuvwxyz".getBytes();
}
