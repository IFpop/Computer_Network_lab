package chapter3.lab4;

public class Host1Main 
{
	private static User _user;
	
	public static void main(String args[])
	{
		_user = new User(true, "lab3-4_Host1.cfg", "host1");
	}
}
