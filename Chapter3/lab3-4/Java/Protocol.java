package network.chapter3.lab4;

public interface Protocol
{
	public enum EventType
	{
		NULL,
		FRAME_ARRIVAL,
		CKSUM_ERR,
		TIMEOUT,
		NETWORK_LAYER_READY
	}
	
	int MAX_SEQUENCE_COUNT = 8;
	
	void runProtocol();
	void receiveDataFromPhysicalLayer();
	void checkSumError();
	void timeIsUp(int index);
}
