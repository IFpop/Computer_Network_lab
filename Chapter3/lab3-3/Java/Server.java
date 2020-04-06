package network.chapter3.lab3;

import java.io.*;
import java.math.BigInteger;
import java.net.*;
import network.*;
import network.chapter3.lab1.CRC_CCITT;

public class Server extends Thread{
	// 设置哪些变量？传输接口，数据，数据发送帧的编号，
	private int NextFrameToSend = 0;
	private String IpAddress = "";
	private int UDPPort=8888;
	private Frame InfoFrame;
	private int FilterError;
	private int FilterLost;
	private int NumOfInfos = 0;
	private byte[] Buffer=new byte[4];
	private File SendFile;
	private boolean EndOfFile = false;
	private boolean KeepId = false;
	private int len;
	private int AckId=0;
	// 读取和写入相关
	// ObjectInputStream in;
	BufferedReader in;
	ObjectOutputStream out;
	DataOutputStream dos;
	public Server(int UDPPort, int FilterError, int FilterLost, String FilePath) {
		this.NextFrameToSend = 0;
		this.IpAddress = "127.0.0.1";
		this.UDPPort = UDPPort;
		this.FilterError = FilterError;
		this.FilterLost = FilterLost;
		this.SendFile = new File(FilePath);
		CRC_CCITT CRC = null;
		Socket Client=null;
		// 设置读取文件
		FileInputStream ReadStream = null;
		try {
			ReadStream = new FileInputStream(this.SendFile);
			System.out.println("Server:服务器读取文件成功!\n");
		} catch (FileNotFoundException e) {
			System.out.println("Server: 读取文件出错！\n");
			System.out.println("Server: FileNotFoundException :" + e.toString());
		}
		 
		// 读取数据到缓冲区
		try {
			
			while ((EndOfFile == false)) 
			{
				if (KeepId == false) {
					len=ReadStream.read(Buffer);
					if (len== -1) {
						EndOfFile = true;
						continue;
					}
//					System.out.println("Server:长度为"+len);
					InfoFrame = new Frame(NextFrameToSend, FilterError, FilterLost);
					InfoFrame.SetMsg(Buffer,len);
				}
				else
				{
					KeepId = false;
					InfoFrame = new Frame(NextFrameToSend, FilterError, FilterLost);
					InfoFrame.SetMsg(Buffer, len);
					InfoFrame.SetState("OK");
				}
					
				
				ByteArrayOutputStream os = new ByteArrayOutputStream();
				ObjectOutputStream out;
				// 将Frame对象转为二进制字符流，计算crc
				out = new ObjectOutputStream(os);
				out.writeObject(InfoFrame);
				byte[] temp = os.toByteArray();
				out.flush();
				out.close();
				out=null;
				String HexString=CRC_CCITT.toHexString(temp);
				String BinString = CRC_CCITT.byteArrToBinStr(temp);
				String CRCValue = CRC_CCITT.getCRC16CCITT(BinString, 0x1021, 0x0000);
//				System.out.println("Server:转换前"+CRCValue);
//				System.out.println("Server:测试CRC为"+CRC_CCITT.getCRC16CCITT(BinString+CRCValue, 0x1021, 0x0000));
				// 在中间插入t以切开信息和CRC
				String HexCRC=new BigInteger(CRCValue,2).toString(16);
//				System.out.println("Server:CRC转换后为"+HexCRC+"CRC转换前为"+CRCValue);
				String SendInfo=HexString+"t"+HexCRC+"\n";
				String State = InfoFrame.GetState();
//				//testCRC
//				{
//					System.out.println("                Server:开始测试CRC");
//					String Bin1=new BigInteger(HexString,16).toString(2);
//					String CRC1=new BigInteger(HexCRC,16).toString(2);
//					while(CRC1.length()%8!=0) CRC1="0"+CRC1;
//					System.out.println("Server:转换后为"+CRC1);
//					System.out.println("Server: 测试 CRC为"+CRC_CCITT.getCRC16CCITT(Bin1+CRC1, 0x1021, 0x0000));
//				}
				Client = new Socket();
				InetSocketAddress ReceiveAdd = new InetSocketAddress(this.IpAddress, this.UDPPort);
				Client.connect(ReceiveAdd, 10000);
				Client.setSoTimeout(15000);
				dos = new DataOutputStream(Client.getOutputStream());
//				System.out.println("       Server:状态为"+InfoFrame.GetState());
				if (!(State.equals("Lost"))) {
					// 将生成的带CRC的字符串输出
					
					dos.writeBytes(SendInfo);
//					System.out.println("Server:"+SendInfo);
//					dos.close();
					System.out.println("Server:正在发送帧编号为" + InfoFrame.GetId());
					System.out.println("Server:next_frame_to_send为" + NextFrameToSend+"\n");
				}
				else {
					try {
						this.sleep(13000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				in = new BufferedReader(new InputStreamReader(Client.getInputStream()));
				String FeedBack = "";
				FeedBack = in.readLine();
//				System.out.println("Server:反馈为"+FeedBack);
				// 根据反馈不同，输出结果
				if (FeedBack.equals("ERROR")) {
					System.out.println("Server:接收到ACK，ACK序号为:"+AckId);
					AckId++;
					System.out.println("Server:ACK内容为： 传输数据错误，重新发送……\n");
					KeepId = true;
					dos.flush();
					Client.close();
					Client=null;
					InfoFrame=null;
					continue;
				} else if (FeedBack.equals("LOST")) {
					System.out.println("Server:接收到ACK，ACK序号为:"+AckId);
					AckId++;
					System.out.println("Server: ACK内容为： 传输数据丢失，重新发送……\n");
					KeepId = true;
					dos.flush();
					Client.close();
					Client=null;
					InfoFrame=null;
					continue;
				} else {
					System.out.println("Server:接收到ACK，ACK序号为:"+AckId);
					AckId++;
					System.out.println("Server:ACK 内容为：传输数据正确接收，发送下一帧数据\n");
					NextFrameToSend++;
				}
				dos.flush();
				Client.close();
				Client=null;
				InfoFrame=null;
			} 
			}
		catch (IOException e1) {
//				System.out.println("Server: Socket连接出错!");
				System.out.println("Server: IOException :" + e1.toString());
			}
		//结束
		System.out.println("Server:传输完毕！\n");
		InfoFrame = new Frame(NextFrameToSend, FilterError, FilterLost);
		InfoFrame.SetState("END");
		try {
			//转变格式
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			ObjectOutputStream out;
			// 将Frame对象转为二进制字符流，计算crc
			out = new ObjectOutputStream(os);
			out.writeObject(InfoFrame);
			byte[] temp = os.toByteArray();
			out.flush();
			out.close();
			out=null;
			String HexString=CRC_CCITT.toHexString(temp);
			String BinString = CRC_CCITT.byteArrToBinStr(temp);
			String CRCValue = CRC_CCITT.getCRC16CCITT(BinString, 0x1021, 0x0000);
			// 在中间插入t以切开信息和CRC
			String HexCRC=new BigInteger(CRCValue,2).toString(16);
			String SendInfo=HexString+"t"+HexCRC+"\n";
//			System.out.println("Server:结束啦！ ");
			
			//开始发送结束信息
			Client = new Socket();
			InetSocketAddress ReceiveAdd = new InetSocketAddress(this.IpAddress, this.UDPPort);
			Client.connect(ReceiveAdd, 10000);
			dos = new DataOutputStream(Client.getOutputStream());
//			System.out.println("       Server:状态为"+InfoFrame.GetState());
			dos.writeBytes(SendInfo);
//			System.out.println("Server:"+SendInfo);
			in = new BufferedReader(new InputStreamReader(Client.getInputStream()));
			String FeedBack = "";
			FeedBack = in.readLine();
			System.out.println("Server:接受最终状态为"+FeedBack+"\n");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		}		
	}
