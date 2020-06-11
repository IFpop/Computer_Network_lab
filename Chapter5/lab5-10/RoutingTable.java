package network.chapter5.lab10;
import network.chapter4.lab2.*;
import java.io.File;
import java.io.IOException;
import java.util.*;

public class RoutingTable 
{
	public RoutingTable(String filename)
	{
		try
		{
			Scanner in = new Scanner(new File(filename));
			_table = new ArrayList<TableItem>();
			while (in.hasNext())
			{
				String destIPStr = in.next();
				String nextIPStr = in.next();
				String linkType = in.next();
				String[] destIPAndSubnet = destIPStr.split("/");
				_table.add(new TableItem(
						Util.ipToInt(destIPAndSubnet[0]), 
						Integer.parseInt(destIPAndSubnet[1]),
						nextIPStr.charAt(0) == '-' ? null : Util.ipToInt(nextIPStr),
						linkType));
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	public Integer getNextIP(int destIP)
	{
		for (TableItem item : _table)
		{
			if (item.inDestIP(destIP))
			{
				Integer tmp = item.getNextIP();
				return tmp == null ? 0 : tmp;
			}
		}
		return null;
	}
	public String toString()
	{
		String res = new String();
		for (TableItem item : _table)
		{
			res += item.toString() + "\n";
		}
		return res;
	}
	private class TableItem
	{
		private int _destIP;
		private int _subnetCnt;
		private Integer _nextIP;
		private String _linkType;
		public TableItem(int destIP, int subnetCnt, Integer nextIP, String linkType)
		{
			_destIP = destIP;
			_subnetCnt = subnetCnt;
			_nextIP = nextIP;
			_linkType = linkType;
		}
		public boolean inDestIP(int num)
		{
			if ((num < 0 && _destIP > 0) || (num > 0 && _destIP < 0))
			{
				return false;
			}
			if (num < 0)
			{
				return (-num ^ -_destIP) < (1 << (32 - _subnetCnt));
			}
			else
			{
				return (num ^ _destIP) < (1 << (32 - _subnetCnt));
			}
		}
		public String toString()
		{
			String nextIPStr = _nextIP == null ? "-" : Util.toIPAddress(_nextIP);
			return Util.toIPAddress(_destIP) + "/" + _subnetCnt + " " + nextIPStr
					 + " " + _linkType;
		}
		public Integer getNextIP()
		{
			return _nextIP;
		}
	}
	private ArrayList<TableItem> _table;
	
}
