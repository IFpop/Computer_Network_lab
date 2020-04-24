package network.chapter4.lab2;

public class LearningListQueryResult 
{
	public enum Type
	{
		FLOODING,
		FORWARDING,
		REFUSE
	}
	
	Type _type;
	int _deviceId;
	
	LearningListQueryResult(Type type, int deviceId)
	{
		_type = type;
		_deviceId = deviceId;
	}
	
	public Type getType()
	{
		return _type;
	}
	
	public int getDevice()
	{
		assert(_type == Type.FORWARDING);
		return _deviceId;
	}
	
	
}
