package Chapter6.lab1;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.DatagramPacket;
import java.net.InetAddress;
public class UDPServer {
        public static void main(String[] args) throws IOException {

            int receiveport=Integer.parseInt(args[0]);
            DatagramSocket socket = new DatagramSocket(receiveport);
            byte[] data = new byte[1024];
            DatagramPacket packet = new DatagramPacket(data, data.length);
            System.out.println("The server is on......");
            socket.receive(packet);
            String info = new String(data, 0, packet.getLength());
            System.out.println("The message get from the client:" + info);

            InetAddress address = packet.getAddress();
            int port = packet.getPort();
            byte[] data_r = info.toUpperCase().getBytes();
            DatagramPacket packet_r = new DatagramPacket(data_r, data_r.length, address, port);
            socket.send(packet_r);
            socket.close();
        }
    }
