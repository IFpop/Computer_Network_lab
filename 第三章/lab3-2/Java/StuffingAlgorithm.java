package chapter3.lab2;

public class StuffingAlgorithm 
{
	
	public static final char ESCAPE = 27;
	
	/**
	 * 
	 * @param str The input 01 sequence.
	 * @param flag The flag which is added to the head and tail of string.
	 * @return Return the string after stuffing.
	 */
	public static String bitStuffing(String str, String flag) 
	{
		String resultString = new String();
		
		// Add the first flag
		resultString += flag;
		
		int len = str.length();
		int oneCount = 0;
		for (int i = 0; i < len; i++)
		{
			if (str.charAt(i) == '0')
			{
				oneCount = 0;
			}
			else
			{
				oneCount++;
				if (oneCount == 5)
				{
					oneCount = 0;
					resultString += '0';
				}
			}
			resultString += str.charAt(i);
		}
		
		// Add the last flag
		resultString += flag;
		
		return resultString;
	}
	
	/**
	 * 
	 * @param str
	 * @param flag
	 * @return If error occurs, then return an empty string. Else return the string after destuffing
	 */
	public static  String bitDeStuffing(String str, String flag)
	{
		String resultString = new String();
		boolean error = false;
		int strLen = str.length();
		int flagLen = flag.length();
		int i = 0;
		for (; i < flagLen; i++)
		{
			if (str.charAt(i) != flag.charAt(i))
			{
				error = true;
				break;
			}
		}
		if (i != flagLen) // strLen < flagLen
		{
			error = true;
		}
		if (!error)
		{
			int oneCount = 0;
			for (; i < strLen - flagLen; i++)
			{
				resultString += str.charAt(i);
				if (str.charAt(i) == '0')
				{
					oneCount = 0;
				}
				else
				{
					oneCount++;
					if (oneCount == 5)
					{
						if (str.charAt(i + 1) != '0')
						{
							error = true;
							break;
						}
						i++;
						oneCount = 0;
					}
				}
			}
			
			// The last flag
			for (int j = 0; j < flagLen; i++, j++)
			{
				if (str.charAt(i) != flag.charAt(j))
				{
					error = true;
					break;
				}
			}
		}
		
		if (error)
		{
			System.out.println("Fatal error when bit destuffing");
			return new String();
		}
		else
		{
			return resultString;
		}
	}
	
	/**
	 * 
	 * @param str The input sequence in hexadecimal.
	 * @param flag The flag which is added to the head and tail of string.
	 * @return Return the string after stuffing.
	 */
	public static String byteStuffing(String str, char flag) 
	{
		String resultString = new String();
		
		// Add the first flag
		resultString += flag;
		
		int len = str.length();
		for (int i = 0; i < len; i++)
		{
			char currentChar = str.charAt(i);
			if (currentChar == flag || currentChar == ESCAPE)
			{
				// Add another ESCAPE byte
				resultString += (ESCAPE);
			}
			resultString += currentChar;
		}
		
		// Add the last flag
		resultString += flag;
		
		return resultString;
	}
	
	public static String byteDeStuffing(String str, char flag) 
	{
		String resultString = new String();
		boolean error = false;
		int i = 0;
		
		if (str.charAt(i++) != flag)
		{
			error = true;
		}
		int strLen = str.length();
		for (; i < strLen - 1 && !error; i++)
		{
			if (str.charAt(i) == ESCAPE)
			{
				i++;
				if (str.charAt(i) != ESCAPE && str.charAt(i) != flag)
				{
					error = true;
				}
			}
			resultString += str.charAt(i);
		}
		if (str.charAt(i) != flag)
		{
			error = true;
		}
		
		if (error)
		{
			System.out.println("Fatal error when byte destuffing");
			return new String();
		}
		else
		{
			return resultString;
		}
	}
}
