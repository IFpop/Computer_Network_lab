package network.chapter5.lab6;

import network.chapter4.lab2.Util;
import java.util.*;
import java.util.Map.*;

public class NatTable 
{
	public NatTable()
	{
		_itemMap = new HashMap<>();
	}
	
	public void output()
	{
		System.out.println("NAT Table:");
		System.out.println("Src IP           Dst IP");
		for (Entry<Integer, Integer> entry : _itemMap.entrySet())
		{
			System.out.println(Util.toIPAddress(entry.getKey()) + "   " + Util.toIPAddress(entry.getValue()));
		}
		System.out.println("");
	}
	
	public void addItem(int innerIP, int outerIP)
	{
		_itemMap.put(innerIP, outerIP);
	}
	
	public int changeIP(int ip)
	{
		if (_itemMap.get(ip) != null)
		{
			return _itemMap.get(ip);
		}
		else
		{
			return ip;
		}
	}
	
	private Map<Integer, Integer> _itemMap;
}
