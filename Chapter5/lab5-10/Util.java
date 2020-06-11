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
	public static byte[] stringToMac(String str, String seperator)
	{
		byte[] res;
		String[] segs = str.split(seperator);
		res = new byte[segs.length];
		for (int i = 0; i < segs.length; i++)
		{
			res[i] = Byte.parseByte(segs[i], 16);
		}
		return res;
	}
	
	public static byte[] hexToByte(String input)
	{
		int strLen = input.length();
		byte[] result = new byte[strLen / 2];
		for (int i = 0; i + 1 < strLen; i += 2)
		{
			result[i / 2] = (byte)Integer.parseInt(input.substring(i, i + 2), 16);
		}
		return result;
	}
	
	public static String bytesToSHex(byte[] arr)
	{
		String res = new String();
		for (int i = 0; i < arr.length;  i++)
		{
			res += Integer.toHexString(arr[i] & 0xff);
			res += " ";
		}
		return res;
	}
	
	public static byte[] hexToByteBitSet(String input)
	{
		int strLen = input.length();
		byte[] result = new byte[strLen / 2];
		for (int i = strLen - 2, j = 0; i >= 0; i -= 2, j++)
		{
			result[j] = (byte)Integer.parseInt(input.substring(i, i + 2), 16);
		}
		return result;
	}
	
	public static int byteToUint(byte num)
	{
		return (int)num & 0xff;
	}
	public static int byteToUShort(byte num1, byte num2)
	{
		return (byteToUint(num1) << 8) + byteToUint(num2);
	}
	
	public static String toIPAddress(int num)
	{
		String res = new String();
		res += byteToUint((byte)(num >> 24)) + ".";
		res += byteToUint((byte)((num >> 16) & 0xff)) + ".";
		res += byteToUint((byte)((num >> 8) & 0xff)) + ".";
		res += byteToUint((byte)((num) & 0xff));
		return res;
	}
	
	public static String toIPAddress(byte nums[])
	{
		String res = new String();
		res += byteToUint(nums[0]) + ".";
		res += byteToUint(nums[1]) + ".";
		res += byteToUint(nums[2]) + ".";
		res += byteToUint(nums[3]);
		return res;
	}
	
	public static int ipToInt(String str)
	{
		String[] segs = str.split("\\.");
		int res = 0;
		for (int i = 0; i < segs.length; i++)
		{
			res = (res << 8) + Integer.parseInt(segs[i]);
		}
		return res;
	}
	
	public static byte[] ipToBytes(String str)
	{
		String[] segs = str.split("\\.");
		byte[] res = new byte[4];
		for (int i = 0; i < segs.length; i++)
		{
			res[i] = (byte)Integer.parseInt(segs[i]);
		}
		return res;
	}
	
	public static byte[] reverse(byte[] arr)
	{
		byte[] newArr = new byte[arr.length];
		for (int i = 0; i < arr.length; i++)
		{
			newArr[i] = arr[arr.length - i - 1];
		}
		return newArr;
	}
	
	private Util()
	{
		
	}
}
