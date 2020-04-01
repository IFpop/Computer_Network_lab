package chapter3.lab4;

import java.util.*;

import chapter3.lab4.FrameInterface.FrameKind;

public class GoBackN implements Protocol
{	
	GoBackN(boolean isSender, String name, PhysicalNetworkLayer manager, int timeoutInMs)
	{
		_hostName = name;
		_timer = new Timer();
		_timeoutInMs = timeoutInMs;
		_manager = manager;
		
		_networkLayerAble = false;
		_timeoutEvent = false;
		_frameArrivalCnt = 0;
		_checkSumErrorEvent = false;
		
		_packets = new byte[GoBackN.MAX_SEQUENCE_COUNT][Frame.PACKET_SIZE];
		_frameToSend = null;
		_sendOver = false;
		_receiveOver = false;
		
		_timerTasks = new TimerTask[GoBackN.MAX_SEQUENCE_COUNT];
		
		if (isSender)
		{
			_startToSendMsg = true;
		}
		else
		{
			_startToSendMsg = false;
		}
		
		System.out.println(_hostName + "'s protocol has initialized");
	}
	
	private static boolean between(int a, int b, int c)
	{
		return ((a <= b) && (b < c)) || ((c < a) && (a <= b)) || ((b < c) && (c < a));
	}
	
	public void runProtocol()
	{
		int receivedCnt = 0;
		int nextFrameToSend = 0;
		int ackExpected = 0;
		int frameExpected = 0;
		int bufferSize = 0;
		Protocol.EventType crtEvent;
		
		System.out.println(_hostName + " start to run protocol ");
		
		enableNetworkLayer();
		boolean end = false;
		while (!end)
		{
			while((crtEvent = waitForEvent()) == EventType.NULL)
			{
				try
				{
					Thread.sleep(500);
				}
				catch (InterruptedException e)
				{
					e.printStackTrace();
				}
			}
			
			System.out.println(_hostName + " receive a event: ");
			
			switch (crtEvent)
			{
			case FRAME_ARRIVAL:
				System.out.println("Frame Arrival");
				_startToSendMsg = true;
				FrameInterface receivedFrame = _manager.fromPhysicalLayer();
				if (receivedFrame.getkind() == FrameInterface.FrameKind.TERMINATION)
				{
					_receiveOver = true;
					if (_sendOver)
					{
						end = true;
					}
				}
				else
				{
					if (receivedFrame.getSequenceNumber() == frameExpected)
					{
						receivedCnt++;
						System.out.println(_hostName + " recevied frame " + receivedCnt);
						frameExpected = (frameExpected + 1) % MAX_SEQUENCE_COUNT;
					}
					while (between(ackExpected, receivedFrame.getAckNumber(), nextFrameToSend))
					{
						bufferSize--;
						stopTimer(ackExpected);
						ackExpected = (ackExpected + 1) % MAX_SEQUENCE_COUNT;
					}
				}
				break;
			case CKSUM_ERR:
				System.out.println("Check sum Error");
				break;
			case TIMEOUT:
				System.out.println("Time is outr");
				nextFrameToSend = ackExpected;
				for (int i = 0; i < bufferSize; i++, nextFrameToSend = (nextFrameToSend + 1) % MAX_SEQUENCE_COUNT)
				{
					sendData(nextFrameToSend, frameExpected);
				}
				break;
			case NETWORK_LAYER_READY:
				String receivedStr = _manager.fromNetworkLayer();
				System.out.println("Network layer is ready: " + receivedStr);
				if (receivedStr == null)
				{
					_sendOver = true;
					sendTerminatingFrame();
					
					if(_receiveOver)
					{
						 end = true;
					}
				}
				else
				{
					byte[] receivedBytes = receivedStr.getBytes();
					int len = receivedBytes.length;
					for (int i = 0; i < Frame.PACKET_SIZE; i++)
					{
						if (i < len)
						{
							_packets[nextFrameToSend][i] = receivedBytes[i];
						}
						else
						{
							_packets[nextFrameToSend][i] = 0;
						}
					}
					
					bufferSize++;
					sendData(nextFrameToSend, frameExpected);
					nextFrameToSend = (nextFrameToSend + 1) % MAX_SEQUENCE_COUNT;
				}
				
				break;
			default:
				System.out.println("Error");
				break;
			}
			
			if (bufferSize < MAX_SEQUENCE_COUNT)
			{
				enableNetworkLayer();
			}
			else
			{
				disableNetworkLayer();
			}
		}
	}
	
	private EventType waitForEvent()
	{
		//System.out.println("Frame arrival cnt: " + _frameArrivalCnt);
		if (_timeoutEvent)
		{
			_timeoutEvent = false;
			return EventType.TIMEOUT;
		}
		else if (_frameArrivalCnt > 0)
		{
			_frameArrivalCnt--;
			return EventType.FRAME_ARRIVAL;
		}
		else if (_checkSumErrorEvent)
		{
			_checkSumErrorEvent = false;
			return EventType.CKSUM_ERR;
		}
		else
		{	
			if (_networkLayerAble && _startToSendMsg && !_sendOver)
			{
				return EventType.NETWORK_LAYER_READY;
			}
			else
			{
				return EventType.NULL;
			}
		}
	}
	
	public void receiveDataFromPhysicalLayer()
	{
		_frameArrivalCnt++;
	}
	
	public void checkSumError()
	{
		_checkSumErrorEvent = true;
	}
	
	private void sendTerminatingFrame()
	{
		_frameToSend = new Frame(_hostName);
		_frameToSend.setProperties(
				FrameKind.TERMINATION, 
				0, 
				0);
		_manager.toPhysicalLayer(_frameToSend);
	}
	
	private void sendData(int frameIndex, int frameExpected)
	{
		_frameToSend = new Frame(_hostName);
		_frameToSend.fromNetworkLayer(_packets[frameIndex]);
		_frameToSend.setProperties(
				FrameKind.DATA, 
				frameIndex, 
				(frameExpected + MAX_SEQUENCE_COUNT - 1) % MAX_SEQUENCE_COUNT);
		startTimer(frameIndex);
		_manager.toPhysicalLayer(_frameToSend);
	}

	public void timeIsUp(int index)
	{
		if (!timerIndexInRange(index))
		{
			return;
		}
		_timeoutEvent = true;
	}
	
	private void startTimer(int index)
	{
		if (!timerIndexInRange(index))
		{
			return;
		}
		_timerTasks[index] = new SendTimerTask(this, index);
		_timer.schedule(_timerTasks[index], _timeoutInMs);
	}
	private void stopTimer(int index)
	{
		if (!timerIndexInRange(index))
		{
			return;
		}
		_timerTasks[index].cancel();
	}
	
	private boolean timerIndexInRange(int index)
	{
		if (!(index >= 0 && index < MAX_SEQUENCE_COUNT))
		{
			System.out.println("Timer index error");
			return false;
		}
		return true;
	}
	
	private void enableNetworkLayer()
	{
		_networkLayerAble = true;
	}
	private void disableNetworkLayer()
	{
		_networkLayerAble = false;
	}
	
	private String _hostName;
	
	private FrameInterface _frameToSend;
	private boolean _timeoutEvent;
	private int _frameArrivalCnt;
	private boolean _checkSumErrorEvent;
	
	private boolean _networkLayerAble;
	private Timer _timer;
	private TimerTask[] _timerTasks;
	private int _timeoutInMs;
	
	private PhysicalNetworkLayer _manager;
	private boolean _sendOver;
	private boolean _receiveOver;
	
	private byte[][] _packets;
	private boolean _startToSendMsg;
}