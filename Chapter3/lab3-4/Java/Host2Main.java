package network.chapter3.lab4;

public class Host2Main 
{
	private static User _user;
	
	public static void main(String args[])
	{
		_user = new User(false, "lab3-4_Host2.properties", "host2");
	}
}
