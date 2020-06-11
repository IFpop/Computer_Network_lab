package network.chapter5.lab8;

public class TracertLog 
{
	private String _ip;
	private String[] _timeLog;
	private int _index;
	private int _ttl;
	TracertLog(int ttl, String ip)
	{
		_timeLog = new String[JavaTracert.PACKET_CNT];
		_index = 0;
		_ttl = ttl;
		_ip = ip;
		for (int i = 0; i < JavaTracert.PACKET_CNT; i++)
		{
			_timeLog[i] = " * ";
		}
	}
	
	// return true if the log count equals max packet count(default: 3)
	public boolean addLog(long timeInms)
	{
		if(timeInms >= 0)
		{
			if (_index >= JavaTracert.PACKET_CNT)
			{
				return true;
			}
			
			if (timeInms <= 0)
			{
				_timeLog[_index] = "<= 1ms";
			}
			else
			{
				_timeLog[_index] = timeInms + "ms";
			}
		}
		_index++;
		return _index == JavaTracert.PACKET_CNT;
	}
	
	public void output()
	{
		System.out.print((_ttl + 1) + " ");
		for (int i = 0; i < JavaTracert.PACKET_CNT; i++)
		{
			System.out.print(_timeLog[i] + " ");
		}
		System.out.println(_ip);
	}
}
