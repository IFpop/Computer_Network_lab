package Chapter6.lab5;

import java.io.*;
import java.net.*;

public class Server {

  private ServerSocket serverSocket;
  public Server(int port) throws Exception{
      serverSocket = new ServerSocket(port);
  }
  public void start() throws Exception{
    while (true) {
        Socket socket = serverSocket.accept();
        (new Request(socket)).start();
    }
  }
  private class Request extends Thread {

    private Socket socket;
    private ObjectInputStream in;
    private ObjectOutputStream out;
    public Request(Socket socket) throws IOException {
      this.socket = socket;
        in = new ObjectInputStream(socket.getInputStream());
        out = new ObjectOutputStream(socket.getOutputStream());
    }

    @Override
    public void run() {
      try {
        Object obj = in.readObject();
        if (obj instanceof Packet) {
          Packet packet = (Packet)obj;
          System.out.println("recv from " + socket.getInetAddress().getHostAddress() + ":" + socket.getPort() + ", seq = " + packet.getSeq());

          out.writeObject(packet);
          out.flush();
        }

        socket.close();
      } catch (IOException e) {
        e.printStackTrace();
      } catch (ClassNotFoundException e) {
        e.printStackTrace();
      }
      System.out.flush();
    }
  }

  public static void main(String[] args) throws Exception{
      int port = Integer.parseInt(args[0]);
      Server server = new Server(port);
      server.start();
  }
}
