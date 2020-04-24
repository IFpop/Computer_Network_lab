package network.chapter4.lab2;

public class Util 
{
	public static String macToString(byte[] arr)
	{
		String res = new String();
		for (int i = 0; i < arr.length; i++) 
		{
			if (i < arr.length) 
			{
				res += (Integer.toHexString(arr[i] & 0xff) + ":");
			} 
			else 
			{
				res += (Integer.toHexString(arr[i] & 0xff));
			}
		}
		return res;
	}
	
	private Util()
	{
		
	}
}
