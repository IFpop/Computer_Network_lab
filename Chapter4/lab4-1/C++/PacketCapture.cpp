#include <cstdlib>
#include <cstdio>
#include<iostream>
#define WIN32  
#include<pcap.h>
using namespace std;
#define LINE_LEN 16
#define ETHER_ADDR_LEN 6
#define IP4_TYPE 0x0800
#define IP6_TYPE 0x08DD
#define ARP_TYPE 0x0806
#pragma comment(lib,"ws2_32.lib")
typedef struct ethernet_header {
	u_char dest[ETHER_ADDR_LEN]; //MAC目的地址
	u_char src[ETHER_ADDR_LEN]; //MAC源地址
	u_short type;                 //类型
}ether_header;
typedef struct ip_address {
	u_char byte1;
	u_char byte2;
	u_char byte3;
	u_char byte4;
}ip_address;

typedef struct ip_header {
	u_char ver_ihl;         //版本和首部长
	u_char tos;            //服务类型
	u_short tlen;           //总长度
	u_char identification[2]; //标识符
	u_char flags_fo[2];       //标记偏移量
	u_char ttl;             //寿命
	u_char proto;         //协议
	u_char crc[2];          //校验和
	ip_address saddr;      //源ip
	ip_address daddr;     //目的ip
	u_int op_pad;         //可选+Padding
}ip_header;
typedef struct tcp_header {
	u_short sport;        	//源端口号
	u_short dport;         	//目的端口号
	u_int seq;            	//序列号
	u_int ack;             	//确认号
	u_char hlen_flags;  	//首部长
	u_char flags;         //保留标志
	u_short window;      	//接受窗口
	u_char Checksum[2];           	//校验和
	u_short urp;           	//紧急数据帧
}tcp_header;

typedef struct udp_header {
	u_short sport;         //源端口
	u_short dport;         //目的端口
	u_short len;              //首部长
	u_char Checksum[2];        //校验和
} udp_header;
typedef struct icmp_t_header {
	u_char type;       //类型  
	u_char code;        //代码 
	u_char Checksum[2];        //校验和        
	u_char Unused[4];
} icmp_t_header;
typedef struct icmp_p_header {
	u_char type;       //类型  
	u_char code;        //代码 
	u_char Checksum[2];        //校验和        
	u_char Identifier[2];
	u_char Sequence[2];
} icmp_p_header;
typedef struct arp_header {
	u_char hardware[2];   	//硬件类型
	u_short type;          	//上层协议类型
	u_char size;			//MAC地址长度
	u_char Psize;				//总长
	u_char opType[2];            //操作类型
	u_char srcMAC[ETHER_ADDR_LEN];        //源MAC
	ip_address saddr;      //源ip
	u_char dstMAC[ETHER_ADDR_LEN];        //目的MAC
	ip_address daddr;     //目的ip

} arp_header;
void printIP(const u_char* pkt_data, const struct pcap_pkthdr* header,int);
void printTCP(const u_char* pkt_data, const struct pcap_pkthdr* header,int netlen,int totallen)
{
	tcp_header* th;
	cout << "运输层：" << endl;
	printf("运输层协议类型：TCP\n");
	th = (tcp_header*)(pkt_data + 14 + netlen);
	//源端口号
	printf("Source Port:%d|", htons(th->sport));
	//目的端口号
	printf("Destination Port:%d|", htons(th->dport));
	//序列号
	printf("Sequence number(raw):%d|", htons(th->seq));
	//确认号
	printf("Acknowledgment number(raw):%d|", htons(th->ack));
	//首部长
	int hl = (th->hlen_flags / 16 * 4);
	printf("Header Length:%d|", hl);
	//保留标志
	printf("Flags:0x%.1x%.2x|",th->hlen_flags % 16,th->flags);
	//接受窗口
	printf("Window size value:%d|", htons(th->window));
	//校验和
	printf("Checksum:0x%.2x%.2x|", th->Checksum[0], th->Checksum[1]);
	//紧急数据帧
	printf("Urgent pointer:%d", htons(th->urp));
	//选项
	if(hl-20>0)
	printf("|Options:0x");
	int i;
	for (i = 50; i < 50 + hl - 20; i++)
		printf("%.2x", pkt_data[i]);
	if (i < totallen) {
		printf("\nTCP segment data:");

			printf("%.2x-%.2x ", pkt_data[i],pkt_data[totallen-1]);

		cout << endl;
	}
	char* httpHeader = (char*)th + hl;
	int httplen = header->len - totallen;
	if (httplen && (strstr(httpHeader, "GET")|| strstr(httpHeader, "HTTP")|| strstr(httpHeader, "POST"))&&(htons(th->sport)==80|| htons(th->dport) == 80))
	{
		cout << "应用层" << endl;
		cout << "应用层协议类型:HTTP" << endl;
		cout << "Data:0x";
		printf("%.2x-%.2x", pkt_data[totallen], pkt_data[header->len-1]);
		
	}

}
void printUDP(const u_char* pkt_data, const struct pcap_pkthdr* header, int netlen, int totallen)
{
	udp_header* uh;
	cout << "运输层：" << endl;
	printf("运输层协议类型:UDP\n");
	uh= (udp_header*)(pkt_data + 14 + netlen);
	//源端口号
	printf("Source Port:%d|", htons(uh->sport));
	//目的端口号
	printf("Destination Port:%d|", htons(uh->dport));
	//UDP大小
	printf("Length:%d|", htons(uh->len));
	//校验和
	printf("Checksum:0x%.2x%.2x\n", uh->Checksum[0], uh->Checksum[1]);
	cout << "应用层：" << endl;
	char* httpHeader = (char*)uh + htons(uh->len);
	int httplen = header->len - totallen;
	printf("Data:0x");
	printf("%.2x-%.2x ", pkt_data[42],pkt_data[header->caplen-1]);
}
void printICMP(const u_char* pkt_data, const struct pcap_pkthdr* header, int netlen, int totallen)
{
	printf("网络层协议类型:ICMP\n");
	icmp_p_header *ich=(icmp_p_header*)(pkt_data + 14 + netlen);
	//类型
	printf("Type:%d|",ich->type);
	//代码
	printf("Code:%d|", ich->code);
	//校验和
	printf("Checksum:0x%.2x%.2x|", ich->Checksum[0],ich->Checksum[1]);
	int type = ich->type;
	//查询报文
	if (type == 0 || type == 8 || type == 9 || type == 10 || (type >= 13 && type <= 18))
	{
		printf("Identifier:0x%.2x%.2x|", ich->Identifier[0], ich->Identifier[1]);
		printf("Sequence:0x%.2x%.2x\n", ich->Sequence[0], ich->Sequence[1]);
		printf("Data:0x");
			printf("%.2x-%.2x", pkt_data[22 + netlen], pkt_data[header->len-1]);
		cout << endl;
	}//差错报文
	else {
		icmp_t_header* icht = (icmp_t_header*)(pkt_data + 14 + netlen);
		printf("Unused:0x%.2x%.2x%.2x%.2x\n", icht->Unused[0], icht->Unused[1], icht->Unused[2], icht->Unused[3]);
		printIP(pkt_data , header,28);
		
	}


}
void printIP(const u_char* pkt_data,const struct pcap_pkthdr* header,int addtion)
{
	int i = 0;
	int pt = 0;
	ip_header* ih;
	cout << "网络层：" << endl;
	printf("网络层协议类型:IPV4\n");
	//跳过以太网帧头，获取ip首部,addtion防止icmp协议嵌套
	ih = (ip_header*)(pkt_data + 14+addtion);
	//版本号
	printf("Version:%d|", ih->ver_ihl /16);
	//首部长
	int headerLen = (ih->ver_ihl %16 )* 4;
	printf("Header length:%d byte|", headerLen);
	//服务类型
	printf("Differentiated Services Field:0x%.2x|", ih->tos);
	//总长
	printf("Total Length:%d|", htons(ih->tlen));
	//标志
	printf("Identification:0x%.2x%.2x|", ih->identification[0], ih->identification[1]);
	//标记3bit
	printf("Flags:0x%.2x%.2x|", ih->flags_fo[0], ih->flags_fo[1]);
	//片偏移
	printf("Fragment offset:%d|", (ih->flags_fo[0] % 32 * 256 + ih->flags_fo[1]));
	//寿命
	printf("Time to live:%d|", ih->ttl);
	//协议
	printf("Protocol:");
	if (ih->proto == 6)
	{
		pt = 6;
		printf("TCP(%d)|", ih->proto);
	}
	else if (ih->proto == 17)
	{
		pt = 17;
		printf("UDP(%d)|", ih->proto);

	}
	else if (ih->proto == 1)
	{
		pt = 1;
		printf("ICMP(%d)|", ih->proto);
	}
	else
	{
		pt = ih->proto;
		printf("(%d)|", ih->proto);
	}
	//首部校验和
	printf("Header checksum:0x%.2x%.2x|", ih->crc[0], ih->crc[1]);
	//源ip
	printf("Source:%d.%d.%d.%d|", ih->saddr.byte1, ih->saddr.byte2, ih->saddr.byte3, ih->saddr.byte4);
	//目的ip
	printf("Destination:%d.%d.%d.%d\n", ih->daddr.byte1, ih->daddr.byte2, ih->daddr.byte3, ih->daddr.byte4);
	//TCP
	if (pt == 6)
	{
		printTCP(pkt_data, header,headerLen+addtion, htons(ih->tlen));
	}//UDP
	else if (pt == 17)
	{
		printUDP(pkt_data, header,headerLen+addtion, htons(ih->tlen));

	}
	else if (pt == 1)
	{
		printICMP(pkt_data, header,headerLen+addtion, htons(ih->tlen));
	}

}

void printARP(const u_char* pkt_data, const struct pcap_pkthdr* header)
{
	int i = 0;
	int pt = 0;
	arp_header * ah;
	cout << "网络层：" << endl;
	printf("网络层协议类型:ARP\n");
	ah = (arp_header*)(pkt_data + 14);
	//硬件类型
	if (pkt_data[14] == 0 && pkt_data[15] == 1)
		printf("Hardware Type:Ethernet(%d)|", ah->hardware[1]);
	else
		printf("Hardware Type:0x%.2x%.2x|", ah->hardware[0], ah->hardware[1]);
	//上层协议类型
	printf("Protocol type:");
	if (ah->type == htons(IP4_TYPE))
	{
		printf("IPv4(0x0800)\n");
	}
	else if (ah->type == htons(IP6_TYPE))
	{
		printf("IPv6(0x08DD)\n");
	}
	else
		printf("(0x%.2x%.2x)|", pkt_data[16], pkt_data[17]);
	//MAC地址长度
	printf("Hardware size:%d|", ah->size);
	//总长
	printf("Protocol size:%d|", ah->Psize);
	//操作类型
	printf("Opcode:");
	if (ah->opType[0] == 0 && ah->opType[1] == 1)
	{
		printf("request(%d)|", ah->opType[1]);
	}
	else if (ah->opType[0] == 0 && ah->opType[1] == 2)
	{
		printf("reply(%d)|", ah->opType[1]);
	}
	else
		printf("(0x%.2x%.2x)|", ah->opType[0], ah->opType[1]);
	//源地址
	printf("Sender MAC address:%.2x:%.2x:%.2x:%.2x:%.2x:%.2x|",ah->srcMAC[0], ah->srcMAC[1], ah->srcMAC[2], ah->srcMAC[3], ah->srcMAC[4], ah->srcMAC[5]);
	//源ip
	printf("Sender IP address:%d.%d.%d.%d|", ah->saddr.byte1, ah->saddr.byte2,  ah->saddr.byte3, ah->saddr.byte4);
	//目的地址
	printf("Target MAC address:%.2x:%.2x:%.2x:%.2x:%.2x:%.2x|",ah->dstMAC[0], ah->dstMAC[1], ah->dstMAC[2], ah->dstMAC[3], ah->dstMAC[4], ah->dstMAC[5]);
	//目的ip
	printf("Target IP address:%d.%d.%d.%d\n", ah->daddr.byte1, ah->daddr.byte2, ah->daddr.byte3, ah->daddr.byte4);
}
void packet_handler(u_char* param, const struct pcap_pkthdr* header, const u_char* pkt_data)
{
	struct tm ltime;
	char timestr[16];
	ethernet_header* eh;
	//输出帧到达时间
	time_t temp = header->ts.tv_sec;
	localtime_s(&ltime,&temp);
	strftime(timestr, sizeof(timestr), "%H:%M:%S", &ltime);
	printf("Arrival time: %s|frame length: %d\n", timestr, header->len);
	cout << endl;
	cout << "数据链路层：" << endl;
	printf("数据链路层协议类型:Ehternet\n");
	eh = (ethernet_header*)pkt_data;
	//目的地址
	printf("Destination:%.2x:%.2x:%.2x:%.2x:%.2x:%.2x|", eh->dest[0],eh->dest[1], eh->dest[2], eh->dest[3], eh->dest[4], eh->dest[5]);
	//源地址
	printf("Source:%.2x:%.2x:%.2x:%.2x:%.2x:%.2x|", eh->src[0], eh->src[1], eh->src[2], eh->src[3], eh->src[4], eh->src[5]);
	//类型
	printf("Type:");
	if (eh->type == htons(IP4_TYPE))
	{
		printf("IPv4(0x0800)\n");
		printIP(pkt_data, header,0);
	}
	else if (eh->type == htons(IP6_TYPE))
	{
		printf("IPv6(0x08DD)\n");
		printIP(pkt_data, header,0);
	}
	else if (eh->type == htons(ARP_TYPE))
	{
		printf("ARP(0x0806)\n");
		printARP(pkt_data, header);
	}
	cout << endl << endl;
}
int main()
{
	pcap_if_t* alldevs, * d;
	pcap_t* fp;
	struct pcap_pkthdr* header;
	const u_char* pkt_data;
	char errbuf[PCAP_ERRBUF_SIZE];
	int num, i = 0;
	//获取计算机上的适配器列表
	if (pcap_findalldevs(&alldevs, errbuf))
	{
		cout << "Error in pcap_findalldevs:" << errbuf << endl;
		return -1;
	}
	//打印列表
	for (d = alldevs; d; d = d->next)
	{
		cout << ++i << ". " << d->name << endl;
		if (d->description)
			cout << " (" << d->description << ")" << endl;
		else
			cout << " (No desciption available)" << endl;
		pcap_addr_t* a;
		for (a = d->addresses; a; a = a->next)
		{
			if (a->addr->sa_family == AF_INET)
			{
				if (a->addr)
				{
					u_char* adr = (u_char*)&((struct sockaddr_in*)a->addr)->sin_addr.s_addr;
					printf("IPv4 adrress: %d.%d.%d.%d\n",adr[0],adr[1],adr[2],adr[3]);
				}
				if (a->netmask)
				{
					u_char* adr = (u_char*)&((struct sockaddr_in*)a->netmask)->sin_addr.s_addr;
					printf("Subnet mask,: %d.%d.%d.%d\n", adr[0], adr[1], adr[2], adr[3]);
				}
				if (a->broadaddr)
				{
					u_char* adr = (u_char*)&((struct sockaddr_in*)a->broadaddr)->sin_addr.s_addr;
					printf("broadcast address: %d.%d.%d.%d\n", adr[0], adr[1], adr[2], adr[3]);
				}
				puts("");
			}
		}
		puts("");
	}
	if (i == 0)
	{
		cout << "No interfaces found! Exiting." << endl;
		return -1;
	}
	//选择接口号
	cout << "Enter the interface number (1-" << i << "):";
	cin >> num;
	//接口输入错误
	if (num<1 || num>i)
	{
		cout << "Interface number out of range." << endl;
		pcap_freealldevs(alldevs);
		return -1;
	}
	//获取相应适配器并打开
	for (d = alldevs, i = 0; i < num - 1; d = d->next, i++);
	if ((fp = pcap_open_live(d->name, 100, 1, 20, errbuf)) == NULL)
	{
		cout << "Error opening source: " << errbuf << endl;
		return -1;
	}
	//Ethernet
	if (pcap_datalink(fp) != DLT_EN10MB)
	{
		fprintf(stderr, "\nThis program works only on Ethernet networks.\n");
		pcap_freealldevs(alldevs);
		return -1;
	}
	pcap_freealldevs(alldevs);
	pcap_loop(fp,0,packet_handler,NULL);
	return 0;
}