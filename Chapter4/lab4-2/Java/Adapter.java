package network.chapter4.lab2;

import network.chapter4.lab2.*;
import java.util.Arrays;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Scanner;

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

public class Adapter implements PacketReceiver
{
	private byte[] _receiveFrom;
	private byte[] _sendTo;
	private int _receiveDevice;
	private NetworkInterface[] _deviceList;
	private Bridge _bridge;
	
	public Adapter(byte[] receiveFrom, int receiveDevice, byte[] sendTo, NetworkInterface[] deviceList, Bridge bridge)
	{
		_receiveFrom = receiveFrom;
		_receiveDevice = receiveDevice;
		_sendTo = sendTo;
		_deviceList = deviceList;
		_bridge = bridge;
	}
	
	@Override
	public void receivePacket(Packet packet) 
	{
		DatalinkPacket dp = packet.datalink;
		if (dp instanceof EthernetPacket) 
		{
			EthernetPacket ep = (EthernetPacket) dp;
			if (Arrays.equals(ep.dst_mac, _receiveFrom)|| Arrays.equals(ep.dst_mac, _sendTo))
			{
				System.out.println("�˿�  " + _receiveDevice + "���յ�һ��packet");
				System.out.println("ԴMAC��ַ: " + Util.macToString(ep.src_mac));
				System.out.println("Ŀ��MAC��ַ: " + Util.macToString(ep.dst_mac));
				System.out.println("");
				
				LearningListQueryResult queryResult = _bridge.query(_receiveDevice, _receiveFrom, _sendTo);
				switch (queryResult.getType())
				{
				case REFUSE:
					System.out.println("Դ�˿ں�Ŀ�Ķ˿���ͬ��MAC��ַ��ͬ��������Ϣ");
					break;
				case FLOODING:
					System.out.println("Ŀ�Ķ˿�δ֪�����з���");
					break;
				case FORWARDING:
					System.out.println("Ŀ�Ķ˿���֪: " + queryResult.getDevice() + "������ת����");
					break;
				default:
					System.out.println("Error!!!!!!!!!");
					break;
				}
			}
			
		}

	}
}
