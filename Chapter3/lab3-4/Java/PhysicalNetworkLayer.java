package network.chapter3.lab4;

public interface PhysicalNetworkLayer 
{
	void toPhysicalLayer(FrameInterface frame);
	FrameInterface fromPhysicalLayer();
	
	String fromNetworkLayer();
	void toNetworkLayer(String data);
}
