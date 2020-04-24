package network.chapter4.lab2;

import java.util.*;

import java.util.Arrays;

public class Bridge  
{
	// Singleton
	
	public Bridge()
	{
		lock = new Object();
		learningList = new ArrayList<Bridge.LearningListItem>();
	}
	
	// Data
	
	private class LearningListItem
	{
		public static final int MAX_CNT = 3;
		public byte[] address;
		public int deviceId;
		
		private int _counter;
		public LearningListItem(byte[] _address, int _deviceId)
		{
			address = _address.clone();
			deviceId = _deviceId;
			_counter = 1;
		}
		public boolean isOutdated()
		{
			if (++_counter == MAX_CNT)
			{
				System.out.println("学习表已清空");
				System.out.println(" ");
				return true;
			}
			else
			{
				return false;
			}
		}
	}
	
	private ArrayList<Bridge.LearningListItem> learningList;
	private Object lock;
	
	// Functions
	
	public LearningListQueryResult query(int srcDevice, byte[] srcMac, byte[] destMac)
	{
		LearningListQueryResult result;
		synchronized (lock) 
		 {
			LearningListItem srcItem = null;
			LearningListItem destItem = null;
			int listSize = learningList.size(); // To visit the last item
			for (LearningListItem i : learningList)
			{
				if (Arrays.equals(i.address, srcMac))
				{
					srcItem = i;
				}
				if (Arrays.equals(i.address, destMac))
				{
					destItem = i;
				}
			}
			for (int i = listSize - 1; i >= 0; i--)
			{
				if (learningList.get(i).isOutdated())
				{
					learningList.remove(i);
					listSize--;
				}		
			}
			if (srcItem == null)
			{
				learningList.add(new LearningListItem(srcMac, srcDevice));
				srcItem = learningList.get(listSize);
			}
			
			if (srcItem == destItem || Arrays.equals(srcMac, destMac))
			{
				result = new LearningListQueryResult(LearningListQueryResult.Type.REFUSE, 0);
			}
			else if (destItem == null)
			{
				result = new LearningListQueryResult(LearningListQueryResult.Type.FLOODING, 0);
			}
			else
			{
				result = new LearningListQueryResult(LearningListQueryResult.Type.FORWARDING, destItem.deviceId);
			}
		 }
		return result;
	}
}

