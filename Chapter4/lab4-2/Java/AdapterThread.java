package network.chapter4.lab2;

import java.lang.*;
import network.chapter4.lab2.*;
import jpcap.packet.Packet;
import jpcap.packet.DatalinkPacket;
import jpcap.packet.EthernetPacket;
import jpcap.packet.ARPPacket;
import jpcap.packet.IPPacket;
import jpcap.packet.TCPPacket;
import jpcap.packet.UDPPacket;

import jpcap.JpcapCaptor;
import jpcap.NetworkInterface;
import jpcap.PacketReceiver;

public class AdapterThread extends Thread
{
	Adapter _adapter;
	JpcapCaptor _captor;
	public AdapterThread(Adapter adapter, JpcapCaptor captor)
	{
		_adapter = adapter;
		_captor = captor;
	}
	
	public void run()
	{
		_captor.loopPacket(-1, _adapter);
	}
}
