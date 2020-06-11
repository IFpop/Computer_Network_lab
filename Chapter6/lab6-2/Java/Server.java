package network.chapter6.lab2;

import java.io.*;
import java.net.*;
import java.util.*;


public class Server 
{
	public static final int PORT = 8888;
	
	public static void main(String args[])
	{
		try
		{
			ServerSocket serverSocket = new ServerSocket(PORT);
			Socket socket = serverSocket.accept();
			DataInputStream input = new DataInputStream(socket.getInputStream());
			DataOutputStream output = new DataOutputStream(socket.getOutputStream());
			String received = input.readUTF();
			String send = received.toUpperCase();
			output.writeUTF(send);
			System.out.println("Server Received: " + received);
			System.out.println("Server Send: " + send);
			
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		
	}
}
