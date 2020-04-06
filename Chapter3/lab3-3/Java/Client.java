package network.chapter3.lab3;

import java.io.*;
import java.math.BigInteger;
import java.net.ServerSocket;
import java.net.Socket;
import network.chapter3.lab1.CRC_CCITT;

import javax.jws.soap.SOAPBinding.Use;

public class Client extends Thread {
    private static final String GetInfo = null;
	private static ServerSocket Server;
    private static int ServerPort = 8888;
    private int ExpectedFrameId;
    private byte[] Buffer=new byte[4];
    private String SavePath;
    private byte[] FileInfo=new byte[0];
    private Frame InfoFrame;
    private boolean EndOfFile = false;
    private int AckId=1;
    private String FileName = "test.txt";
    Socket UsedSocket;
    // ObjectInputStream in;
    BufferedReader in;

    public Client(int UDPPort, String SavePath) {
        this.SavePath = SavePath;
        ServerPort = UDPPort;
        try {
            Server = new ServerSocket(ServerPort);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            // e.printStackTrace();
            System.out.println("Client: Socket启动出错!");
            System.out.println("IOException :" + e.toString());
        }
        ExpectedFrameId = 1;
        EndOfFile = false;
    }

    public void run() {

        System.out.println("Client:客户端启动！\n");
        while (!EndOfFile) {
        	UsedSocket = null;
            try {
            	 
                 synchronized (Server) {
                	 UsedSocket = Server.accept();
//                     System.out.println("Client:连接成功！");
                 }
                // 设置超时，方便获取帧丢失的状态
                UsedSocket.setSoTimeout(15000);
                in = new BufferedReader(new InputStreamReader(UsedSocket.getInputStream()));
//                DataInputStream dos=new DataInputStream(UsedSocket.getInputStream());
                
                String GetInfo = in.readLine();
//                in.close();
//                String GenInfo=dos.readUTF();
//                System.out.println("Client:"+GetInfo);
//                in.close();
//                in = null;
                // 将字符串分为信息字节流和CRC代码两部分
                String TrueInfo = GetInfo.split("t")[0];
                String CRCInfo = GetInfo.split("t")[1];
                // 对Frame对象进行反序列化
//                byte[] ByteInfo = TrueInfo.getBytes();
                byte[] ByteInfo=CRC_CCITT.parseHexStr2Byte(TrueInfo);
//                System.out.println("Client:TrueInfo"+ByteInfo.toString());
                // 创建存放二进制数据的API
                ByteArrayInputStream byteArrayInputStream = null;
                // 创建反序列化对象
                ObjectInputStream objectInputStream = null;
                // 进行反序列化，获取InfoFrame对象
                byteArrayInputStream = new ByteArrayInputStream(ByteInfo);
                objectInputStream = new ObjectInputStream(byteArrayInputStream);
                InfoFrame=(Frame) objectInputStream.readObject();
                System.out.println("Client:期待接收到的帧序号为:"+ExpectedFrameId);
                System.out.println("Client:接收到帧序号为:" + InfoFrame.GetId()+"\n");
                // 判断获取的帧的状态
                String FrameState = InfoFrame.GetState();
                // 结束状态
                if (FrameState.equals("END")) {
                    EndOfFile = true;
                    DataOutputStream dos1 = new DataOutputStream(UsedSocket.getOutputStream());
                    dos1.writeBytes("END" + '\n');
                    dos1.close();
                    System.out.println("Client:接收文件完成！\n");
                    
                    continue;
                } else {
                	//转为2进制字符串
                	TrueInfo=new BigInteger(TrueInfo,16).toString(2);
                	CRCInfo=new BigInteger(CRCInfo,16).toString(2);
                	while(CRCInfo.length()%8!=0)
                		CRCInfo="0"+CRCInfo;
//                	CRCInfo=CRC_CCITT.byteArrToBinStr(CRC_CCITT.parseHexStr2Byte(CRCInfo));
                    String CRCValue = CRC_CCITT.getCRC16CCITT(TrueInfo+CRCInfo, 0x1021, 0x0000);
                    // 数据无错
                    System.out.println("CLient:CRCValue="+CRCValue);
                    if (Integer.valueOf(CRCValue,2) == 0) {
//                    	synchronized (Server) {
//                       	 UsedSocket = Server.accept();
//                        }
                    	Buffer=InfoFrame.GetMsg();
                    	// 将数据写入将储存的文件数组
                        byte[] Data = new byte[Buffer.length + FileInfo.length];
//                        System.arraycopy(InfoFrame.GetMsg(), 0, Data, 0, 4);
//                        System.arraycopy(FileInfo, 0, Data, 4, FileInfo.length);
                        System.arraycopy(FileInfo,0,Data,0,FileInfo.length);
                        System.arraycopy(Buffer, 0, Data, FileInfo.length, InfoFrame.Length);
                        FileInfo = Data;
                        ExpectedFrameId++;
                        System.out.println("Client:数据无错！接收到的帧序号为：" + InfoFrame.GetId()+"\n");
                        // ACK状态为OK
                        DataOutputStream dos1 = new DataOutputStream(UsedSocket.getOutputStream());
                        dos1.writeBytes("OK" + '\n');
                        dos1.close();
                        System.out.println("Client:发送确认帧成功！"+" 确认帧序号为:"+AckId+"\n");
                        AckId++;
                    }
                    // 数据错误
                    else {
                        DataOutputStream dos1 = new DataOutputStream(UsedSocket.getOutputStream());
                        dos1.writeBytes("ERROR" + '\n');
                        dos1.close();
                        System.out.println("Clinet:数据出错！");
                        System.out.println("Client:发送确认帧成功！"+" 确认帧序号为:"+AckId+"\n");
                        AckId++;
                    }
                }
                in.close();
                in = null;
                UsedSocket.close();
                UsedSocket=null;
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (java.net.SocketTimeoutException e) {
                System.out.println("Client：帧丢失");
                try {
                    DataOutputStream dos = new DataOutputStream(UsedSocket.getOutputStream());
                    dos.writeBytes("LOST" + '\n');
                    dos.close();
                    System.out.println("Client:发送确认帧成功！"+" 确认帧序号为:"+AckId+"\n");
                    AckId++;
                } catch (java.io.IOException ee) {
                    ee.printStackTrace();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
//        System.out.println("Client:文件接受完毕！\n");
        // 将byte数组存储在文件中
        File RecFile = new File(SavePath + FileName);
        try {
            FileOutputStream FileOut = new FileOutputStream(RecFile);
            FileOut.write(FileInfo);
            FileOut.flush();
            FileOut.close();
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            // e.printStackTrace();
            System.out.println("Client:无法创建该文件！");
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            System.out.println("Client:写入文件失败！");
        }
    }
}