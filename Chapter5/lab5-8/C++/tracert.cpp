#include <iostream>
#include <winsock2.h>
#include <ws2tcpip.h>
#pragma comment(lib,"ws2_32.lib")
using namespace std;

const int MAX_PACKET_SIZE = 1024;		//ICMP����ĳ���
const int DEF_DATA_SIZE = 32;			//ICMPĬ�������ֶγ���
const DWORD DEF_ICMP_TIMEOUT = 3000;	//����Ӧ��ʱʱ��
const BYTE ICMP_TIMEOUT = 11;			//���䳬ʱ
const BYTE ICMP_ECHO_REPLY = 0;			//����Ӧ��
const BYTE ICMP_ECHO_REQUEST = 8;		//�������
USHORT SequenceNo = 0;					//ICMP�������к�
int MaxNum = 30;						//���30��Ծ��
int TTL = 1;							//TTL��ʼֵΪ1
BOOL flag = FALSE;						//ѭ���˳���־
int cycle = 3;							//ÿ�����ķ���3��

typedef struct {
	BYTE type;
	BYTE code;
	USHORT checksum;
	USHORT identifier;
	USHORT sequence;
}ICMPHeader;

typedef struct {
	USHORT sequence;
	DWORD time;                      //����ʱ��
	in_addr ipaddr;
}Decode;

//IP��ͷ
typedef struct {
	unsigned char header_len : 4;
	unsigned char versoin : 4;
	unsigned char tos;              //8λ��������
	unsigned short total_len;
	unsigned short identifier;
	unsigned short frag_and_flag;   //3λ��־+13λƬƫ��
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
	//��ʼ��Windows sockets���绷��
	WSADATA wsa;
	WSAStartup(MAKEWORD(2, 2), &wsa);


	//�õ�IP��ַ
	u_long DestIP = inet_addr(argv[2]);

	//ת�����ɹ�ʱ����������
	if (DestIP == INADDR_NONE)
	{
		hostent* pHostent = gethostbyname(argv[2]);
		if (pHostent)
		{
			DestIP = (*(in_addr*)pHostent->h_addr).s_addr;
		}
		else
		{
			cout << "�����IP��ַ��������Ч" << endl;
			WSACleanup();
			return 0;
		}
	}
	BYTE *cp = (BYTE *)&DestIP;
	cout << "ͨ�����30��Ծ��׷�٣�" << endl;
	cout << "��  " << argv[2] << "[" << (int)cp[0] << "." << (int)cp[1] << "." << (int)cp[2] << "." << (int)cp[3] << "] ��·��" << endl;

	MyTracert(DestIP, argv[2]);

	return 0;
}

int MyTracert(u_long DestIP, char * IpAddress)
{
	//���Ŀ�Ķ�socket��ַ
	sockaddr_in destSockAddr;
	ZeroMemory(&destSockAddr, sizeof(sockaddr_in));
	destSockAddr.sin_family = AF_INET;
	destSockAddr.sin_addr.s_addr = DestIP;

	//����ԭʼ�׽���
	SOCKET sockRaw = WSASocket(AF_INET, SOCK_RAW, IPPROTO_ICMP, NULL, 0, WSA_FLAG_OVERLAPPED);


	//���ICMP������ÿ�η���ʱ������ֶ�
	char ICMPSendBuf[sizeof(ICMPHeader) + DEF_DATA_SIZE];
	char ICMPRecvBuf[MAX_PACKET_SIZE];

	BOOL bReachDestHost = FALSE;        //ѭ���˳���־

	while (!flag && MaxNum--)
	{
		//����IP��ͷ��TTL�ֶ�
		InitSocket(sockRaw);

		cout << TTL;    //�����ǰ���,flush��ʾ�������������������ͽ�cout,�����������ˢ��

		memset(ICMPSendBuf, 0, sizeof(ICMPSendBuf));
		memset(ICMPRecvBuf, 0, sizeof(ICMPRecvBuf));

		InitSendPackage(ICMPSendBuf, sockRaw);			//��ʼ��Ҫ���͵ı���

		while (cycle--) {
			Decode DecodeResult;
			DecodeResult.sequence = ((ICMPHeader*)ICMPSendBuf)->sequence;
			DecodeResult.time = GetTickCount();

			//���ͱ���
			if (-1 == sendto(sockRaw, ICMPSendBuf, sizeof(ICMPSendBuf), 0, (sockaddr*)&destSockAddr, sizeof(destSockAddr))) {
				cout << "send package error" << endl;
				return 0;
			}

			//���ձ���
			RecvPackage(sockRaw, ICMPRecvBuf, destSockAddr, DecodeResult);
		}
		cycle = 3;
		TTL++;
	}
	cout << endl;
	cout << "׷����ɡ�" << endl;
	return 0;
}

void InitSocket(SOCKET& sockRaw)
{
	int timeout = 3000;     //������ʱΪ3��
	int rcvsnd;
	//���ý��ճ�ʱ
	rcvsnd = setsockopt(sockRaw, SOL_SOCKET, SO_RCVTIMEO, (char*)&timeout, sizeof(timeout));
	if (rcvsnd == -1) {
		fprintf(stderr, "fail to set rcv timeout: %d", WSAGetLastError());
		cout << "setsockopt(SO_RCVTIMEO) failed" << WSAGetLastError() << endl;
	}
	//���÷��ͳ�ʱ
	rcvsnd = setsockopt(sockRaw, SOL_SOCKET, SO_SNDTIMEO, (char*)&timeout, sizeof(timeout));
	if (rcvsnd == -1) {
		cout << "setsockopt(SO_SNDTIMEO) failed" << WSAGetLastError() << endl;
	}
	//����TTL�ֶ�
	rcvsnd = setsockopt(sockRaw, IPPROTO_IP, IP_TTL, (char*)&TTL, sizeof(TTL));
	if (rcvsnd == -1) {
		cout << "setsockopt(IP_TTL) failed" << WSAGetLastError() << endl;
	}
}

void InitSendPackage(char *ICMPSendBuf, SOCKET& sockRaw)
{
	ICMPHeader *IcmpHeader = (ICMPHeader*)ICMPSendBuf;
	IcmpHeader->type = ICMP_ECHO_REQUEST;							//��������Ϊ�������
	IcmpHeader->code = 0;
	IcmpHeader->identifier = (USHORT)GetCurrentProcessId();			//ID�ֶ�Ϊ��ǰ���̺�
	memset(ICMPSendBuf + sizeof(ICMPHeader), 'a', DEF_DATA_SIZE);   //���������ֶ�(�˴��������ȫΪa)

	IcmpHeader->checksum = 0;
	IcmpHeader->sequence = htons(SequenceNo);                                                   //���������޷��Ŷ�������ת���������ֽ�˳��ֵ
	SequenceNo++;
	IcmpHeader->checksum = checksum((USHORT*)ICMPSendBuf, sizeof(ICMPHeader) + DEF_DATA_SIZE);  //����Ч���
}

int RecvPackage(SOCKET& sockRaw, char *ICMPRecvBuf, sockaddr_in& destSockAddr, Decode &DecodeResult)
{
	sockaddr_in dest;				//�Զ�socket��ַ
	int DestLen = sizeof(dest);     //��ַ�ṹ��С
	int DestDataLen;				//�������ݳ���

	while (1)
	{
		//��������
		DestDataLen = recvfrom(sockRaw, ICMPRecvBuf, MAX_PACKET_SIZE, 0, (sockaddr*)&dest, &DestLen);
		if (DestDataLen != SOCKET_ERROR)//�����ݵ���
		{
			//�����ݰ����н���
			if (DecodePacket(ICMPRecvBuf, DestDataLen, DecodeResult))
			{
				//����Ŀ�ĵأ��˳�ѭ��
				if (DecodeResult.ipaddr.s_addr == destSockAddr.sin_addr.s_addr)
					flag = true;
				//���IP��ַ
				if (cycle == 0) {
					cout << "\t" << inet_ntoa(DecodeResult.ipaddr) << endl;
				}
				Sleep(1000);
				return 1;
			}
		}
		else if (WSAGetLastError() == WSAETIMEDOUT){						//���ճ�ʱ�����*��
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

//�����ݰ����н���
BOOL DecodePacket(char *Buf, int DestDataLen, Decode &DecodeResult)
{
	//������ݱ���С�ĺϷ���
	IPHeader *IpHeader = (IPHeader*)Buf;
	int IpHeaderLen = IpHeader->header_len * 4;
	if (DestDataLen < (int)(IpHeaderLen + sizeof(ICMPHeader)))
		return FALSE;

	//����ICMP����������ȡID�ֶκ����к��ֶ�
	ICMPHeader *IcmpHeader = (ICMPHeader*)(Buf + IpHeaderLen);
	USHORT ID, SequenceNo;
	if (IcmpHeader->type == ICMP_ECHO_REPLY)    //ICMP����Ӧ����
	{
		ID = IcmpHeader->identifier;   //����ID
		SequenceNo = IcmpHeader->sequence;  //�������к�
	}
	else if (IcmpHeader->type == ICMP_TIMEOUT)   //ICMP��ʱ�����
	{
		char *InnerIpHeader = Buf + IpHeaderLen + sizeof(ICMPHeader);  //�غ��е�IPͷ
		int InnerIPHeaderLen = ((IPHeader*)InnerIpHeader)->header_len * 4; //�غ��е�IPͷ��
		ICMPHeader *pInnerIcmpHdr = (ICMPHeader*)(InnerIpHeader + InnerIPHeaderLen);//�غ��е�ICMPͷ
		ID = pInnerIcmpHdr->identifier;  //����ID
		SequenceNo = pInnerIcmpHdr->sequence;  //���к�
	}
	else {
		return false;
	}
	//���ID�����к���ȷ���յ��ڴ����ݱ�
	if (ID != (USHORT)GetCurrentProcessId() || SequenceNo != DecodeResult.sequence)
	{
		return false;
	}
	//��¼IP��ַ����������ʱ��
	DecodeResult.ipaddr.s_addr = IpHeader->SourceIP;
	DecodeResult.time = GetTickCount() - DecodeResult.time;


	//������ȷ�յ���ICMP���ݱ�
	if (IcmpHeader->type == ICMP_ECHO_REPLY || IcmpHeader->type == ICMP_TIMEOUT)
	{
		//�������ʱ����Ϣ
		if (DecodeResult.time)
			cout << "\t" << DecodeResult.time << "ms";
		else
			cout << "\t" << "<1ms";
	}
	return true;
}

//����Ч���
USHORT checksum(USHORT *buf, int DataSize)
{
	unsigned long sum = 0;

	//16λΪ��λ�������
	while (DataSize > 1) {
		sum += *buf++;
		DataSize -= sizeof(USHORT);
	}

	//����Ϊ�������
	if (DataSize) {
		sum += *((USHORT*)buf);
	}

	//��λ�н�λ����λ����λ���������д��뱣֤�˸�16λΪ0
	sum = (sum >> 16) + (sum & 0xffff);
	sum += (sum >> 16);

	//���ȡ��
	sum = ~sum;
	return sum;
}