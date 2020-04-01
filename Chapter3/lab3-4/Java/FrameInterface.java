package network.chapter3.lab4;

public interface FrameInterface
{	
	public enum FrameKind
	{
		DATA, ACK, NCK, TERMINATION
	}
	
	FrameKind getkind();
	int getSequenceNumber();
	int getAckNumber();
	
	void setProperties(FrameKind kind, int sequenceNumber, int ackNumber);
	
	void fromNetworkLayer(byte[] data);
	byte[] toNetworkLayer();
	
	void fromPhysicalLayer(byte[] data);
	byte[] toPhysicalLayer();
	
	boolean isError();
}
