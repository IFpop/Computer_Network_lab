package network.chapter5.lab4;
import java.util.*;
import network.chapter4.lab2.*;

public class IPHeader 
{
	public final int VERSION_BEGIN = 0,  VERSION_END = 4,
		HEADER_LEN_BEGIN = VERSION_END, HEADER_LEN_END = HEADER_LEN_BEGIN + 4,
		SERVICE_TYPE_BEGIN = HEADER_LEN_END, SERVICE_TYPE_END = SERVICE_TYPE_BEGIN + 8,
		TOTAL_LENGTH_BEGIN = SERVICE_TYPE_END, TOTAL_LENGTH_END = TOTAL_LENGTH_BEGIN + 16,
		ID_BEGIN = TOTAL_LENGTH_END, ID_END = ID_BEGIN + 16,
		FLAG_BEGIN = ID_END, FLAG_END = FLAG_BEGIN + 3,
		FRAG_OFFSET_BEGIN = FLAG_END, FRAG_OFFSET_END = FRAG_OFFSET_BEGIN + 13,
		TTL_BEGIN = FRAG_OFFSET_END, TTL_END = TTL_BEGIN + 8,
		PROTOCOL_BEGIN = TTL_END, PROTOCOL_END = PROTOCOL_BEGIN + 8,
		CHECKSUM_BEGIN = PROTOCOL_END, CHECKSUM_END = CHECKSUM_BEGIN + 16,	
		SRC_ADD_BEGIN = CHECKSUM_END, SRC_ADD_END = SRC_ADD_BEGIN + 32,
		DST_ADD_BEGIN = SRC_ADD_END, DST_ADD_END = DST_ADD_BEGIN + 32;
	
	public IPHeader(String dataInHex)
	{
		_header = BitSet.valueOf(Util.hexToByteBitSet(dataInHex));
	}
	public IPHeader(byte[] byteArr)
	{
		_header = BitSet.valueOf(Util.reverse(byteArr));
	}
	
	private void resetScanner()
	{
		scanIndex = _header.length();
	}
	
	private long scan(int startIndex, int endIndex)
	{
		long num = 0;
		for (int i = startIndex; i < endIndex; i++)
		{
			num = (num << 1) + (_header.get(159 - i) ? 1 : 0);
		}
		return num;
	}
	private void setRange(int startIndex, int endIndex, int value)
	{
		for (int i = 0, j = endIndex - 1; j >= startIndex; i++, j--)
		{
			if (((value >> i) & 1) > 0)
			{
				_header.set(159 - j);
			}
			else
			{
				_header.clear(159 - j);
			}
		}
	}

	private long scanLong(int len)
	{
		long num = 0;
		for (int i = 0; i < len; i++)
		{
			//System.out.println(scanIndex - i);
			num = (num << 1) + (_header.get(scanIndex - i) ? 1 : 0);
		}
		scanIndex -= len;
		//System.out.println("");
		return num;
	}
	public int getTTL()
	{
		return (int)scan(TTL_BEGIN, TTL_END);
	}
	public void setTTL(int ttl)
	{
		setRange(TTL_BEGIN, TTL_END, ttl);
	}
	public int getSrcIP()
	{
		return (int)scan(SRC_ADD_BEGIN, SRC_ADD_END);
	}
	public int getDstIP()
	{
		return (int)scan(DST_ADD_BEGIN, DST_ADD_END);
	}
	public void setSrcIP(int ip)
	{
		setRange(SRC_ADD_BEGIN, SRC_ADD_END, ip);
	}
	private void clearChecksum()
	{
		setRange(CHECKSUM_BEGIN, CHECKSUM_END, 0);
	}
	// return new checksum
	public int recalChecksum()
	{
		//System.out.println("checksum before " + scan(CHECKSUM_BEGIN, CHECKSUM_END));
		clearChecksum();
		//System.out.println("checksum clear " + scan(CHECKSUM_BEGIN, CHECKSUM_END));
		int checksum = calChecksum();
		setRange(CHECKSUM_BEGIN, CHECKSUM_END, checksum);
		//System.out.println("checksum after " + checksum + " , " + scan(CHECKSUM_BEGIN, CHECKSUM_END));
		int reCheck = calChecksum();
		if (reCheck != 0)
		{
			System.out.println("checksum error! checksum = " + reCheck);
		}
		return checksum;
	}
	
	public void output()
	{
		resetScanner();
		long version = scanLong(4);
		long headerLen = scanLong(4);
		long service = scanLong(8);
		long totalLen = scanLong(16);
		long id = scanLong(16);
		long MF = scanLong(1);
		long DF = scanLong(1);
		scanLong(1);
		long segOffset = scanLong(13);
		long ttl = scanLong(8);
		long protocol = scanLong(8);
		long checkSum = scanLong(16);
		long srcIP = scanLong(32);
		long destIP = scanLong(32);
		
		System.out.print("Version: " + version);
		if (version == 4)
		{
			System.out.println("(IPV4)");
		}
		else if (version == 6)
		{
			System.out.println("(IPV6)");
		}
		else
		{
			System.out.println("");
		}
		System.out.println("Header Length: " + headerLen);
		System.out.println("Differentiated Service: " + service);
		System.out.println("Total Length: " + totalLen);
		System.out.println("Identification: : " + id);
		System.out.println("MF: " + MF);
		System.out.println("DF: " + DF);
		System.out.println("Segment Offset: " + segOffset);
		System.out.println("TTL: " + ttl);
		System.out.println("Protocol: " + protocol);
		System.out.println("Checksum: " + checkSum);
		System.out.println("Source IP: " + Util.toIPAddress((int)srcIP));
		System.out.println("Destination IP: " + Util.toIPAddress((int)destIP));
	}
	
	public int calChecksum()
	{
		int res = 0;
		for (int i = 0; i < 10; i++)
		{
			int tmp = (int)scan(i * 16, i * 16 + 16);
			res += tmp;
		}
		res = (res >> 16) + (res & 0xffff);
		res += res >> 16;
		res &= 0xffff;

		return (int)(~((short)res)) & 0xffff;
	}
	
		
	private BitSet _header;
	private int scanIndex;
}
