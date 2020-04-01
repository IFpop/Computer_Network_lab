package network.chapter3.lab4;

import network.chapter3.lab1.*;
import java.math.*;
import java.util.BitSet;

public class Frame implements FrameInterface
{
	public static final int POLYNOMINAL = 0x1021;
	public static final int PACKET_SIZE = 8;
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
	}
	public byte[] toNetworkLayer()
	{
		return _packet;
	}
	
	/*public static int CRC16_CCITT(byte[] buffer) {
        int wCRCin = 0x0000;
        int wCPoly = 0x8408;
        for (byte b : buffer) {
            wCRCin ^= ((int) b & 0x00ff);
            for (int j = 0; j < 8; j++) {
                if ((wCRCin & 0x0001) != 0) {
                    wCRCin >>= 1;
                    wCRCin ^= wCPoly;
                } else {
                    wCRCin >>= 1;
                }
            }
        }
        return wCRCin ^= 0x0000;
    }*/
	
	private static String byteArrToBinary(byte[] data)
	{
		String result = new String();
		for (int i = 0; i < data.length; i++)
		{
			result += Integer.toBinaryString(data[i]);
		}
		return result;
	}
	
	public static String byteToBinaryString(byte[] data)
	{
		String binaryString = new String();
		for (int i = 0; i < data.length; i++)
		{
			binaryString += Integer.toBinaryString(data[i]);
		}
		return binaryString;
	}
	
	public static boolean validCheckSum(byte[] data)
	{
		byte[] tmp = new byte[data.length - 2];
		for (int i = 0; i < data.length - 2; i++)
		{
			tmp[i] = data[i];
		}
		String binaryString = byteToBinaryString(tmp);
		int result = Integer.parseInt(CRC_CCITT.getCRC16CCITT(binaryString, POLYNOMINAL, 0x0000), 2);
		if ((byte)(result >> 8) == data[data.length - 2] && 
				(byte)(result % (1 << 8)) == data[data.length - 1])
		{
			return true;
		}
		else
		{
			return false;
		}
	}
	
	public static byte[] addCheckSum(byte[] data)
	{
		//int result = CRC16_CCITT(data);
		String binaryString = byteToBinaryString(data);
		int result = Integer.parseInt(CRC_CCITT.getCRC16CCITT(binaryString, POLYNOMINAL, 0x0000), 2);
		byte[] crcCode = new byte[2];
		crcCode[0] = (byte)(result >> 8);
		crcCode[1] = (byte)(result % (1 << 8));
		byte[] tmp = new byte[data.length + crcCode.length];
		int index = 0;
		for (int i = 0; i < data.length; i++)
		{
			tmp[index++] = data[i];
		}
		for (int i = 0; i < crcCode.length; i++)
		{
			tmp[index++] = crcCode[i];
		}
		return tmp;
	}
	
	public void fromPhysicalLayer(byte[] data)
	{
		if (!validCheckSum(data))
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
		//System.out.println(_hostName + " get data from physical layer: " + new String(_packet));
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
		byte[] dataByte = data.getBytes();
		dataByte = addCheckSum(dataByte);
		//System.out.println(_hostName + " send data to physical layer : " + new String(_packet));
		return dataByte;
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
