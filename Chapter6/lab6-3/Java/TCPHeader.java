package network.chapter6.lab3;

import java.util.BitSet;

import network.chapter4.lab2.Util;

public class TCPHeader 
{
	public final int SRC_PORT_BEGIN = 0,  SRC_PORT_END = 16,
			DST_PORT_BEGIN = SRC_PORT_END, DST_PORT_END = DST_PORT_BEGIN + 16,
			SEQUENCE_BEGIN = DST_PORT_END, SEQUENCE_END = SEQUENCE_BEGIN + 32,
			ACKNOWLEDGE_BEGIN = SEQUENCE_END, ACKNOWLEDGE_END = ACKNOWLEDGE_BEGIN + 32,
			OFFSET_BEGIN = ACKNOWLEDGE_END, OFFSET_END = OFFSET_BEGIN + 4,
			RESERVE_BEGIN = OFFSET_END, RESERVE_END = RESERVE_BEGIN + 6,
			FLAG_BEGIN = RESERVE_END, FLAG_END = FLAG_BEGIN + 6,
			WINDOW_BEGIN = FLAG_END, WINDOW_END = WINDOW_BEGIN + 16,
			CHECKSUM_BEGIN = WINDOW_END, CHECKSUM_END = CHECKSUM_BEGIN + 16,	
			POINTER_BEGIN = CHECKSUM_END, POINTER_END = POINTER_BEGIN + 16;
		
		public TCPHeader(String dataInHex)
		{
			_header = BitSet.valueOf(Util.hexToByteBitSet(dataInHex));	
		}
		public TCPHeader(byte[] byteArr)
		{
			_header = BitSet.valueOf(Util.reverse(byteArr));
		}
		
		private void resetScanner()
		{
			scanIndex = 159;
		}
		
		private long scan(int startIndex, int endIndex)
		{
			long num = 0;
			for (int i = startIndex; i < endIndex; i++)
			{
				if (159 - i < 0)
				{
					num <<= 1;
				}
				else
				{
					num = (num << 1) + (_header.get(159 - i) ? 1 : 0);
				}
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
		
		public void output()
		{
			resetScanner();
			long srcPort = scanLong(16);
			long dstPort = scanLong(16);
			long seq = scanLong(32);
			long ack = scanLong(32);
			long offset = scanLong(4);
			scanLong(6);
			boolean URG = scanLong(1) > 0 ? true : false;
			boolean ACK = scanLong(1) > 0 ? true : false;
			boolean PSH = scanLong(1) > 0 ? true : false;
			boolean RST = scanLong(1) > 0 ? true : false;
			boolean SYN = scanLong(1) > 0 ? true : false;
			boolean FIN = scanLong(1) > 0 ? true : false;
			long window = scanLong(16);
			long checkSum = scanLong(16);
			long urgentPointer = scanLong(16);
			
			System.out.println("Src Port: " + srcPort);
			System.out.println("Dst Port: " + dstPort);
			System.out.println("Sequence Number: " + seq);
			System.out.println("Acknowledge Number: " + ack);
			System.out.println("Offset: " + offset);
			System.out.println("URG: " + URG);
			System.out.println("ACK: " + ACK);
			System.out.println("PSH: " + PSH);
			System.out.println("RST: " + RST);
			System.out.println("SYN: " + SYN);
			System.out.println("FIN: " + FIN);
			System.out.println("Window: " + window);
			System.out.println("Checksum: " + checkSum);
			System.out.println("Urgent Pointer: " + urgentPointer);
		}

		
		private byte[] getPseudoHeader(byte[] srcIP, byte[] dstIP, int totalLength)
		{
			byte[] res = new byte[12];
			int tot = 0;
			for (int i = 0; i < srcIP.length; i++, tot++)
			{
				res[tot] = srcIP[i];
			}
			for (int i = 0; i < dstIP.length; i++, tot++)
			{
				res[tot] = dstIP[i];
			}
			res[tot++] = 0;
			res[tot++] = 6;
			res[tot++] = (byte)(totalLength >> 8);
			res[tot++] = (byte)(totalLength % 256);
			
			System.out.println("Pseudo Header: ");
			System.out.println("Src IP: " + Util.toIPAddress(srcIP));
			System.out.println("Dst IP: " + Util.toIPAddress(dstIP));
			System.out.println("Protocol: 6(TCP)");
			System.out.println("TotalLength: " + totalLength);
			
			return res;
		}
		
		public int calChecksum(byte[] srcIP, byte[] destIP, byte[] data)
		{
			byte[] pseudoHeader = getPseudoHeader(srcIP, destIP, data.length+ 20);
			int res = 0;
			for (int i = 0; i < 10; i++)
			{
				int tmp = (int)scan(i * 16, i * 16 + 16);
				res += tmp;
			}
			for (int i = 0; i + 1 < pseudoHeader.length; i += 2)
			{
				res += Util.byteToUShort(pseudoHeader[i], pseudoHeader[i + 1]);
			}
			for (int i = 0; i + 1 < data.length; i += 2)
			{
				res += Util.byteToUShort(data[i], data[i + 1]);
			}
			if (data.length % 2 == 1)
			{
				res += Util.byteToUShort(data[data.length - 1], (byte)0);
			}
			res = (res >> 16) + (res & 0xffff);
			res += res >> 16;
			res &= 0xffff;

			return (int)(~((short)res)) & 0xffff;
		}
		
			
		private BitSet _header;
		private int scanIndex;
}
