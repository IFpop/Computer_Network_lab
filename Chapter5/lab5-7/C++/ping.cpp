#include <iostream>
#include <winsock2.h>
#include <ws2tcpip.h>
#pragma comment(lib,"ws2_32.lib")
using namespace std;

const int MAX_PACKET_SIZE = 1024;		//ICMP最大报文长度
const int DEF_DATA_SIZE = 32;			//ICMP默认数据字段长度
const DWORD DEF_ICMP_TIMEOUT = 3000;	//回显应答超时时间
const BYTE ICMP_TIMEOUT = 11;			//传输超时
const BYTE ICMP_ECHO_REPLY = 0;			//回显应答
const BYTE ICMP_ECHO_REQUEST = 8;		//请求回显
USHORT SequenceNo = 0;             //ICMP报文序列号

typedef struct {
	BYTE type;
	BYTE code;
	USHORT checksum;
	USHORT identifier;
	USHORT sequence;
}ICMPHeader;

typedef struct {
	USHORT sequence;
	DWORD time;                      //返回时间
	in_addr ipaddr;
}Decode;

//IP报头
typedef struct {
	unsigned char header_len : 4;
	unsigned char versoin : 4;
	unsigned char tos;              //8位服务类型
	unsigned short total_len;
	unsigned short identifier;
	unsigned short frag_and_flag;   //3位标志+13位片偏移
	unsigned char ttl;
	unsigned char protocol;
	unsigned short checksum;
	unsigned long SourceIP;
	unsigned long DestIP;
}IPHeader;

//计算网际效验和函数
USHORT checksum(USHORT *buf, int DataSize)
{
	unsigned long sum = 0;
	while (DataSize > 1)
	{
		sum += *buf++;
		DataSize -= 2;
	}
	if (DataSize)
	{
		sum += *(USHORT*)buf;
	}
	while (sum >> 16) {
		sum = (sum >> 16) + (sum & 0xffff);
	}
	sum = ~sum;
	return sum;
}

//对数据包进行解码
BOOL DecodePacket(char *pBuf, int iPacketSize, Decode &DecodeResult, BYTE ICMP_ECHO_REPLY, BYTE ICMP_TIMEOUT)
{
	//检查数据报大小的合法性
	IPHeader *IpHdr = (IPHeader*)pBuf;
	int iIpHdrLen = IpHdr->header_len * 4;
	if (iPacketSize < (int)(iIpHdrLen + sizeof(ICMPHeader)))
		return FALSE;
	//根据ICMP报文类型提取ID字段和序列号字段
	ICMPHeader *IcmpHdr = (ICMPHeader*)(pBuf + iIpHdrLen);
	USHORT usID, usSquNo;
	if (IcmpHdr->type == ICMP_ECHO_REPLY)    //ICMP回显应答报文
	{
		usID = IcmpHdr->identifier;   //报文ID
		usSquNo = IcmpHdr->sequence;  //报文序列号
	}
	else if (IcmpHdr->type == ICMP_TIMEOUT)   //ICMP超时差错报文
	{
		char *pInnerIpHdr = pBuf + iIpHdrLen + sizeof(ICMPHeader);  //载荷中的IP头
		int iInnerIPHdrLen = ((IPHeader*)pInnerIpHdr)->header_len * 4; //载荷中的IP头长
		ICMPHeader *pInnerIcmpHdr = (ICMPHeader*)(pInnerIpHdr + iInnerIPHdrLen);//载荷中的ICMP头
		usID = pInnerIcmpHdr->identifier;  //报文ID
		usSquNo = pInnerIcmpHdr->sequence;  //序列号
	}
	else {
		return false;
	}
	//检查ID和序列号以确定收到期待数据报
	if (usID != (USHORT)GetCurrentProcessId() || usSquNo != DecodeResult.sequence)
	{
		return false;
	}
	//记录IP地址并计算往返时间
	DecodeResult.ipaddr.s_addr = IpHdr->SourceIP;
	DecodeResult.time = GetTickCount() - DecodeResult.time;

	//处理正确收到的ICMP数据报
	if (IcmpHdr->type == ICMP_ECHO_REPLY || IcmpHdr->type == ICMP_TIMEOUT)
	{
		return true;
	}
	else {
		return false;
	}
	return true;
}

void InitSocket(SOCKET& sockRaw)
{
	int timeout = 3000;     //设置延时为3秒
	int rcvsnd;
	//接收超时
	rcvsnd = setsockopt(sockRaw, SOL_SOCKET, SO_RCVTIMEO, (char*)&timeout, sizeof(timeout));
	if (rcvsnd == -1) {
		fprintf(stderr, "fail to set rcv timeout: %d", WSAGetLastError());
		cout << "setsockopt(SO_RCVTIMEO) failed" << WSAGetLastError() << endl;
	}
	//发送超时
	rcvsnd = setsockopt(sockRaw, SOL_SOCKET, SO_SNDTIMEO, (char*)&timeout, sizeof(timeout));
	if (rcvsnd == -1) {
		cout << "setsockopt(SO_SNDTIMEO) failed" << WSAGetLastError() << endl;
	}
	//设置TTL字段
	int TTL = 64;
	rcvsnd = setsockopt(sockRaw, IPPROTO_IP, IP_TTL, (char*)&TTL, sizeof(TTL));
	if (rcvsnd == -1) {
		cout << "setsockopt(IP_TTL) failed" << WSAGetLastError() << endl;
	}
}

void SendPackage(char *ICMPSendBuf, SOCKET& sockRaw)
{	
	ICMPHeader *IcmpHeader = (ICMPHeader*)ICMPSendBuf;
	IcmpHeader->type = ICMP_ECHO_REQUEST;							//类型设置为请求回显
	IcmpHeader->code = 0;
	IcmpHeader->identifier = (USHORT)GetCurrentProcessId();			//ID字段为当前进程号
	memset(ICMPSendBuf + sizeof(ICMPHeader), 'E', DEF_DATA_SIZE);   //数据字段

	IcmpHeader->checksum = 0;
	IcmpHeader->sequence = htons(SequenceNo);                                                   //将主机的无符号短整形数转换成网络字节顺序值
	SequenceNo++;
	IcmpHeader->checksum = checksum((USHORT*)ICMPSendBuf, sizeof(ICMPHeader) + DEF_DATA_SIZE);  //计算效验和

}

int MyPing(u_long DestIP, char * IpAddress)
{

	//填充目的端socket地址
	sockaddr_in destSockAddr;
	ZeroMemory(&destSockAddr, sizeof(sockaddr_in));
	destSockAddr.sin_family = AF_INET;
	destSockAddr.sin_addr.s_addr = DestIP;
	//创建原始套接字
	SOCKET sockRaw = WSASocket(AF_INET, SOCK_RAW, IPPROTO_ICMP, NULL, 0, WSA_FLAG_OVERLAPPED);

	InitSocket(sockRaw);

	//构造ICMP回显请求消息，并以TTL递增的顺序发送报文
	//ICMP类型字段
	int TTL = 64;
	char ICMPSendBuf[sizeof(ICMPHeader) + DEF_DATA_SIZE];
	char ICMPRecvBuf[MAX_PACKET_SIZE];
	memset(ICMPSendBuf, 0, sizeof(ICMPSendBuf));
	memset(ICMPRecvBuf, 0, sizeof(ICMPRecvBuf));


	Decode DecodeResult;			   //传递给报文解码函数的结构化参数
	BOOL ReachDestHost = FALSE;        //循环退出标志
	int cont = 4;                      //循环次数
	int m = 0, n = 0;				   //记录丢包率

	while (!ReachDestHost && cont--)
	{
		
		SendPackage(ICMPSendBuf, sockRaw);

		Decode DecodeResult;
		DecodeResult.sequence = ((ICMPHeader*)ICMPSendBuf)->sequence;
		DecodeResult.time = GetTickCount();
		
		// ICMPHeader *IcmpHeader = (ICMPHeader*)ICMPSendBuf;
		// IcmpHeader->type = ICMP_ECHO_REQUEST;							//类型设置为请求回显
		// IcmpHeader->code = 0;
		// IcmpHeader->identifier = (USHORT)GetCurrentProcessId();			//ID字段为当前进程号
		// memset(ICMPSendBuf + sizeof(ICMPHeader), 'E', DEF_DATA_SIZE);   //数据字段

		// IcmpHeader->checksum = 0;
		// IcmpHeader->sequence = htons(SequenceNo);                                                   //将主机的无符号短整形数转换成网络字节顺序值
		// SequenceNo++;
		// IcmpHeader->checksum = checksum((USHORT*)ICMPSendBuf, sizeof(ICMPHeader) + DEF_DATA_SIZE);  //计算效验和


		//发送报文
		if (-1 == sendto(sockRaw, ICMPSendBuf, sizeof(ICMPSendBuf), 0, (sockaddr*)&destSockAddr, sizeof(destSockAddr))) {
			cout << "send package error" << endl;
			return 0;
		}

		//接收ICMP差错报文并进行解析处理
		sockaddr_in dest;              //对端socket地址
		int DestLen = sizeof(dest);     //地址结构大小
		int DestDataLen;              //接收数据长度

		while (true)
		{

			DestDataLen = recvfrom(sockRaw, ICMPRecvBuf, MAX_PACKET_SIZE, 0, (sockaddr*)&dest, &DestLen);
			if (DestDataLen != -1)//有数据达到
			{
				//对数据包进行解码
				if (DecodePacket(ICMPRecvBuf, DestDataLen, DecodeResult, ICMP_ECHO_REPLY, ICMP_TIMEOUT))
				{
					//到达目的地，退出循环
					if (DecodeResult.ipaddr.s_addr == destSockAddr.sin_addr.s_addr) {
						//输出IP地址
						if (DecodeResult.time)
							cout << "来自 " << inet_ntoa(DecodeResult.ipaddr) << " 的回复：字节32 时间=" << DecodeResult.time << "ms TTL:" << TTL << endl;
						else
							cout << "来自 " << inet_ntoa(DecodeResult.ipaddr) << " 的回复：字节32 时间< 1ms TTL:" << TTL << endl;
						m++;
						break;
					}

				}
			}
			else if (WSAGetLastError() == WSAETIMEDOUT)      //连接超时
			{
				cout << "请求超时。" << endl;
				n++;
				break;
			}
			else {
				break;
			}
		}
	}
	cout << endl;
	cout << IpAddress << " 的 Ping 统计信息：" << endl;
	cout << '\t' << "数据包：已发送=4，已接收=" << m << "，丢失=" << n << endl;
	return 0;
}


int main()
{
	//初始化Windows sockets网络环境
	WSADATA wsa;
	WSAStartup(MAKEWORD(2, 2), &wsa);

	char IpAddress[255];
	cout << "请输入一个IP地址或域名：";
	cin >> IpAddress;
	//得到IP地址
	u_long DestIP = inet_addr(IpAddress);

	//转换不成功时按域名解析
	if (DestIP == INADDR_NONE)
	{
		hostent* pHostent = gethostbyname(IpAddress);
		if (pHostent)
		{
			DestIP = (*(in_addr*)pHostent->h_addr).s_addr;

		}
		else
		{
			cout << "输入的IP地址或域名无效" << endl;
			WSACleanup();
			return 0;
		}
	}
	BYTE *cp = (BYTE *)&DestIP;
	cout << "正在 Ping " << (int)cp[0] << "." << (int)cp[1] << "." << (int)cp[2] << "." << (int)cp[3] << "具有 32 字节的数据：" << endl;

	MyPing(DestIP, IpAddress);
}