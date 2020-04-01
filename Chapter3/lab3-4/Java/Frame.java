package chapter3.lab4;

import chapter3.lab1.CRC_CCITT;
import java.math.*;

public class Frame implements FrameInterface
{
	public static final int PACKET_SIZE = 16;
	public static final int POLYNOMINAL = 0x1021;
	public static final int FRAME_SIZE = PACKET_SIZE + 5;
	
	private FrameKind _kind;
	private int _sequenceNumber;
	private int _ackNumber;
	private byte[] _packet;
	
	private String _hostName;
	
	Frame(String name)
	{
		_hostName = name;
		_packet = new byte[PACKET_SIZE];
	}
	
	public FrameKind getkind()
	{
		return _kind;
	}
	public int getSequenceNumber()
	{
		return _sequenceNumber;
	}
	public int getAckNumber()
	{
		return _ackNumber;
	}
	
	public void setProperties(FrameKind kind, int sequenceNumber, int ackNumber)
	{
		_kind = kind;
		_sequenceNumber = sequenceNumber;
		_ackNumber = ackNumber;
	}
	
	public void fromNetworkLayer(byte[] inputPacket)
	{
		_packet = inputPacket.clone();
		System.out.println(_hostName + " get packet from network layer");
	}
	public byte[] toNetworkLayer()
	{
		System.out.println(_hostName + " send packet to network layer");
		return _packet;
	}
	
	private boolean validAndDeleteCheckSum(String str)
	{
		/*BigInteger number = new BigInteger(str, 2);
		if (number.mod(BigInteger.valueOf(POLYNOMINAL)).equals(BigInteger.ZERO))
		{
			int len = str.length();
			str = str.substring(0, len - 16);
			return true;
		}
		else
		{
			return false;
		}*/
		return true;
	}
	
	private void addCheckSum(String str)
	{
		/*String crcCode = CRC_CCITT.getCRC16CCITT(str, POLYNOMINAL, 0x0000);
		str += crcCode;*/
	}
	
	public void fromPhysicalLayer(byte[] data)
	{
		if (!validAndDeleteCheckSum(data.toString()))
		{
			_packet = null;
			return;
		}
		_kind = intToKind(data[0]);
		_sequenceNumber = data[1];
		_ackNumber = data[2];
		_packet = new byte[PACKET_SIZE];
		for (int i = 0; i < PACKET_SIZE; i++)
		{
			_packet[i] = data[i + 3];
		}
		System.out.println(_hostName + " get data from physical layer: " + new String(_packet));
	}
	
	public boolean isError()
	{
		return _packet == null;
	}
	
	public byte[] toPhysicalLayer()
	{
		String data =  new String();
		data += (char)kindToInt(_kind);
		data += (char)_sequenceNumber;
		data += (char)_ackNumber;
		data += new String(_packet);
		addCheckSum(data);
		System.out.println(_hostName + " send data to physical layer : " + new String(_packet));
		return data.getBytes();
	}
	
	private static int kindToInt(FrameKind kind)
	{
		switch (kind)
		{
		case DATA:
			return 1;
		case ACK:
			return 2;
		case NCK:
			return 3;
		case TERMINATION:
			return 4;
		default :
			return 0;
		}
	}
	
	private static FrameKind intToKind(int kind)
	{
		switch (kind)
		{
		case 1:
			return FrameKind.DATA;
		case 2:
			return FrameKind.ACK;
		case 3:
			return FrameKind.NCK;
		case 4:
			return FrameKind.TERMINATION;
		default :
			System.out.println("Error when convert int to frame kind");
			return FrameKind.DATA;
		}
	}
}
