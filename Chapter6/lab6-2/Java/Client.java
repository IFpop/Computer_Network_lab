package network.chapter6.lab2;

import java.io.*;
import java.net.*;
import java.util.*;

public class Client 
{
	public static void main(String args[])
	{
		if (args.length != 3)
		{
			System.out.println("argument count must be 3");
		}
		String host = args[0];
		int port = Integer.parseInt(args[1]);
		String msg = args[2];
		
		try
		{
			Socket socket = new Socket(host, port);
			DataInputStream input = new DataInputStream(socket.getInputStream());
			DataOutputStream output = new DataOutputStream(socket.getOutputStream());
			output.writeUTF(msg);
			String received = input.readUTF();
			System.out.println("Client Send: " + msg);
			System.out.println("Client Received: " + received);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
}
