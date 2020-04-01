package chapter3.lab4;

import java.util.TimerTask;

public class SendTimerTask extends TimerTask
{
	SendTimerTask(GoBackN protocol, int index)
	{
		_protocol = protocol;
		_index = index;
	}
	public void run()
	{
		_protocol.timeIsUp(_index);
	}
	private GoBackN _protocol;
	private int _index;
}