package network.chapter6.lab6;

public class Sender 
{
	private int _mss;
	private int _threshold;
	private int _triAckRound;
	private int _timeoutRound;
	private int _endRound;
	private Receiver _receiver;
	private byte[] arr;
	
	Sender(int mss, int threshold, int triAckRound, int timeoutRound, int endRound, Receiver receiver)
	{
		_mss = mss;
		_threshold = threshold;
		_triAckRound = triAckRound;
		_timeoutRound = timeoutRound;
		_endRound = endRound;
		_receiver = receiver;
		arr = new byte[mss];
		for (int i = 0; i < mss; i++)
		{
			arr[i] = 'a';
		}
	}
	
	void start()
	{
		System.out.println("MSS: " + _mss);
		System.out.println("Initial threshold: " + _threshold);
		int cwnd = 1;
		
		for (int round = 1; round <= _endRound; round++)
		{
			System.out.println("---------------------------");
			System.out.println("Round: " + round);
			System.out.println("cwnd: " + cwnd);
			System.out.println("threshold: " + _threshold);
			int counter = 0;
			for (int j = 0; j < cwnd; j++)
			{
				_receiver.receive(arr);
				counter += arr.length;
			}
			System.out.println("Send data: a(repeat " + counter + " times)");
			
			if (round == _triAckRound)
			{
				cwnd /= 2;
				_threshold = cwnd;
			}
			else if (round == _timeoutRound)
			{
				_threshold = cwnd / 2;
				cwnd = 1;
			}
			else
			{
				if (cwnd < _threshold)
				{
					cwnd *= 2;
				}
				else
				{
					cwnd++;
				}
			}
			
		}
	}
}
