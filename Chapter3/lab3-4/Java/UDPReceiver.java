package chapter3.lab4;

import java.io.IOException;
import java.net.*;

public class UDPReceiver extends Thread
{
	UDPReceiver(User user)
	{
		_user = user;
	}
	
	public void run()
	{
		while (!_user.hasEnded())
		{
			byte[] buffer = new byte[Frame.FRAME_SIZE];
	        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
	        try
			{
	        	_user.getSocket().receive(packet);	
			}
	        catch (IOException e)
    		{
    			e.printStackTrace();
    		}
	        _user.receiveDataFromPhysicalLayer(packet.getData());
		}
	}
	
	User _user;
}
