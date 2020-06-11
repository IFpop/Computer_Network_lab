package network.chapter5.lab8;

import java.util.Scanner;

public class Main 
{

	public static void main(String[] args) 
	{
		//System.out.println(args[0]);
		if (args.length != 1)
		{
			System.out.println("The parameter count must be 1! Please pass the ip address as the first parameter");
			return;
		}
		JavaTracert tracert = new JavaTracert();
		tracert.tracert(args[0], 30, 5);
	}
}
