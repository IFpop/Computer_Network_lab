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
USHORT SequenceNo = 0;					//ICMP报文序列号
int MaxNum = 30;						//最多30个跃点
int TTL = 1;							//TTL初始值为1
BOOL flag = FALSE;						//循环退出标志
int cycle = 3;							//每个报文发送3次

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

int MyTracert(u_long DestIP, char * IpAddress);
void InitSocket(SOCKET& sockRaw);
void InitSendPackage(char *ICMPSendBuf, SOCKET& sockRaw);
int RecvPackage(SOCKET& sockRaw, char *ICMPRecvBuf, sockaddr_in& destSockAddr, Decode &DecodeResult);
BOOL DecodePacket(char *Buf, int DestDataLen, Decode &DecodeResult);
USHORT checksum(USHORT *buf, int DataSize);

int main(int argc, char *argv[])
{
	//初始化Windows sockets网络环境
	WSADATA wsa;
	WSAStartup(MAKEWORD(2, 2), &wsa);


	//得到IP地址
	u_long DestIP = inet_addr(argv[2]);

	//转换不成功时按域名解析
	if (DestIP == INADDR_NONE)
	{
		hostent* pHostent = gethostbyname(argv[2]);
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
	cout << "通过最多30个跃点追踪：" << endl;
	cout << "到  " << argv[2] << "[" << (int)cp[0] << "." << (int)cp[1] << "." << (int)cp[2] << "." << (int)cp[3] << "] 的路由" << endl;

	MyTracert(DestIP, argv[2]);

	return 0;
}

int MyTracert(u_long DestIP, char * IpAddress)
{
	//填充目的端socket地址
	sockaddr_in destSockAddr;
	ZeroMemory(&destSockAddr, sizeof(sockaddr_in));
	destSockAddr.sin_family = AF_INET;
	destSockAddr.sin_addr.s_addr = DestIP;

	//创建原始套接字
	SOCKET sockRaw = WSASocket(AF_INET, SOCK_RAW, IPPROTO_ICMP, NULL, 0, WSA_FLAG_OVERLAPPED);


	//填充ICMP报文中每次发送时不变的字段
	char ICMPSendBuf[sizeof(ICMPHeader) + DEF_DATA_SIZE];
	char ICMPRecvBuf[MAX_PACKET_SIZE];

	BOOL bReachDestHost = FALSE;        //循环退出标志

	while (!flag && MaxNum--)
	{
		//设置IP报头的TTL字段
		InitSocket(sockRaw);

		cout << TTL;    //输出当前序号,flush表示将缓冲区的内容马上送进cout,把输出缓冲区刷新

		memset(ICMPSendBuf, 0, sizeof(ICMPSendBuf));
		memset(ICMPRecvBuf, 0, sizeof(ICMPRecvBuf));

		InitSendPackage(ICMPSendBuf, sockRaw);			//初始化要发送的报文

		while (cycle--) {
			Decode DecodeResult;
			DecodeResult.sequence = ((ICMPHeader*)ICMPSendBuf)->sequence;
			DecodeResult.time = GetTickCount();

			//发送报文
			if (-1 == sendto(sockRaw, ICMPSendBuf, sizeof(ICMPSendBuf), 0, (sockaddr*)&destSockAddr, sizeof(destSockAddr))) {
				cout << "send package error" << endl;
				return 0;
			}

			//接收报文
			RecvPackage(sockRaw, ICMPRecvBuf, destSockAddr, DecodeResult);
		}
		cycle = 3;
		TTL++;
	}
	cout << endl;
	cout << "追踪完成。" << endl;
	return 0;
}

void InitSocket(SOCKET& sockRaw)
{
	int timeout = 3000;     //设置延时为3秒
	int rcvsnd;
	//设置接收超时
	rcvsnd = setsockopt(sockRaw, SOL_SOCKET, SO_RCVTIMEO, (char*)&timeout, sizeof(timeout));
	if (rcvsnd == -1) {
		fprintf(stderr, "fail to set rcv timeout: %d", WSAGetLastError());
		cout << "setsockopt(SO_RCVTIMEO) failed" << WSAGetLastError() << endl;
	}
	//设置发送超时
	rcvsnd = setsockopt(sockRaw, SOL_SOCKET, SO_SNDTIMEO, (char*)&timeout, sizeof(timeout));
	if (rcvsnd == -1) {
		cout << "setsockopt(SO_SNDTIMEO) failed" << WSAGetLastError() << endl;
	}
	//设置TTL字段
	rcvsnd = setsockopt(sockRaw, IPPROTO_IP, IP_TTL, (char*)&TTL, sizeof(TTL));
	if (rcvsnd == -1) {
		cout << "setsockopt(IP_TTL) failed" << WSAGetLastError() << endl;
	}
}

void InitSendPackage(char *ICMPSendBuf, SOCKET& sockRaw)
{
	ICMPHeader *IcmpHeader = (ICMPHeader*)ICMPSendBuf;
	IcmpHeader->type = ICMP_ECHO_REQUEST;							//类型设置为请求回显
	IcmpHeader->code = 0;
	IcmpHeader->identifier = (USHORT)GetCurrentProcessId();			//ID字段为当前进程号
	memset(ICMPSendBuf + sizeof(ICMPHeader), 'a', DEF_DATA_SIZE);   //设置数据字段(此处填充数据全为a)

	IcmpHeader->checksum = 0;
	IcmpHeader->sequence = htons(SequenceNo);                                                   //将主机的无符号短整形数转换成网络字节顺序值
	SequenceNo++;
	IcmpHeader->checksum = checksum((USHORT*)ICMPSendBuf, sizeof(ICMPHeader) + DEF_DATA_SIZE);  //计算效验和
}

int RecvPackage(SOCKET& sockRaw, char *ICMPRecvBuf, sockaddr_in& destSockAddr, Decode &DecodeResult)
{
	sockaddr_in dest;				//对端socket地址
	int DestLen = sizeof(dest);     //地址结构大小
	int DestDataLen;				//接收数据长度

	while (1)
	{
		//接收数据
		DestDataLen = recvfrom(sockRaw, ICMPRecvBuf, MAX_PACKET_SIZE, 0, (sockaddr*)&dest, &DestLen);
		if (DestDataLen != SOCKET_ERROR)//有数据到达
		{
			//对数据包进行解码
			if (DecodePacket(ICMPRecvBuf, DestDataLen, DecodeResult))
			{
				//到达目的地，退出循环
				if (DecodeResult.ipaddr.s_addr == destSockAddr.sin_addr.s_addr)
					flag = true;
				//输出IP地址
				if (cycle == 0) {
					cout << "\t" << inet_ntoa(DecodeResult.ipaddr) << endl;
				}
				Sleep(1000);
				return 1;
			}
		}
		else if (WSAGetLastError() == WSAETIMEDOUT){						//接收超时，输出*号
			cout << "\t*";
			if (cycle == 0) {
				cout << "\t" << "Request timed out." << endl;
			}
			return 0;
		}
		else{
			return 0;
		}
	}
}

//对数据包进行解码
BOOL DecodePacket(char *Buf, int DestDataLen, Decode &DecodeResult)
{
	//检查数据报大小的合法性
	IPHeader *IpHeader = (IPHeader*)Buf;
	int IpHeaderLen = IpHeader->header_len * 4;
	if (DestDataLen < (int)(IpHeaderLen + sizeof(ICMPHeader)))
		return FALSE;

	//根据ICMP报文类型提取ID字段和序列号字段
	ICMPHeader *IcmpHeader = (ICMPHeader*)(Buf + IpHeaderLen);
	USHORT ID, SequenceNo;
	if (IcmpHeader->type == ICMP_ECHO_REPLY)    //ICMP回显应答报文
	{
		ID = IcmpHeader->identifier;   //报文ID
		SequenceNo = IcmpHeader->sequence;  //报文序列号
	}
	else if (IcmpHeader->type == ICMP_TIMEOUT)   //ICMP超时差错报文
	{
		char *InnerIpHeader = Buf + IpHeaderLen + sizeof(ICMPHeader);  //载荷中的IP头
		int InnerIPHeaderLen = ((IPHeader*)InnerIpHeader)->header_len * 4; //载荷中的IP头长
		ICMPHeader *pInnerIcmpHdr = (ICMPHeader*)(InnerIpHeader + InnerIPHeaderLen);//载荷中的ICMP头
		ID = pInnerIcmpHdr->identifier;  //报文ID
		SequenceNo = pInnerIcmpHdr->sequence;  //序列号
	}
	else {
		return false;
	}
	//检查ID和序列号以确定收到期待数据报
	if (ID != (USHORT)GetCurrentProcessId() || SequenceNo != DecodeResult.sequence)
	{
		return false;
	}
	//记录IP地址并计算往返时间
	DecodeResult.ipaddr.s_addr = IpHeader->SourceIP;
	DecodeResult.time = GetTickCount() - DecodeResult.time;


	//处理正确收到的ICMP数据报
	if (IcmpHeader->type == ICMP_ECHO_REPLY || IcmpHeader->type == ICMP_TIMEOUT)
	{
		//输出往返时间信息
		if (DecodeResult.time)
			cout << "\t" << DecodeResult.time << "ms";
		else
			cout << "\t" << "<1ms";
	}
	return true;
}

//计算效验和
USHORT checksum(USHORT *buf, int DataSize)
{
	unsigned long sum = 0;

	//16位为单位数字相加
	while (DataSize > 1) {
		sum += *buf++;
		DataSize -= sizeof(USHORT);
	}

	//长度为奇数情况
	if (DataSize) {
		sum += *((USHORT*)buf);
	}

	//高位有进位，进位到低位，下面两行代码保证了高16位为0
	sum = (sum >> 16) + (sum & 0xffff);
	sum += (sum >> 16);

	//最后取反
	sum = ~sum;
	return sum;
}