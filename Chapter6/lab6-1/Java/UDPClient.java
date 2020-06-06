package Chapter6.lab1;
import java.io.IOException;
import java.net.*;
public class UDPClient {
    public static void main(String[] args) throws IOException {
        //Send
        String DestIp = args[0];
        int port = Integer.parseInt(args[1]);
        InetAddress address = InetAddress.getByName(DestIp);
        System.out.println("Information send to server:" + args[2]);
        byte[] data = args[2].getBytes();
        DatagramPacket packet = new DatagramPacket(data, data.length, address, port);
        DatagramSocket socket = new DatagramSocket();
        socket.send(packet);
        //Receive
        byte[] data2 = new byte[1024];
        DatagramPacket packet2 = new DatagramPacket(data2, data2.length);
        socket.receive(packet2);
        String reply = new String(data2, 0, packet2.getLength());
        System.out.println("After the server transform the message:" + reply);
        socket.close();
    }
}
