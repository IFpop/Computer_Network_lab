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
	u_char dest[ETHER_ADDR_LEN]; //MACĿ�ĵ�ַ
	u_char src[ETHER_ADDR_LEN]; //MACԴ��ַ
	u_short type;                 //����
}ether_header;
typedef struct ip_address {
	u_char byte1;
	u_char byte2;
	u_char byte3;
	u_char byte4;
}ip_address;

typedef struct ip_header {
	u_char ver_ihl;         //�汾���ײ���
	u_char tos;            //��������
	u_short tlen;           //�ܳ���
	u_char identification[2]; //��ʶ��
	u_char flags_fo[2];       //���ƫ����
	u_char ttl;             //����
	u_char proto;         //Э��
	u_char crc[2];          //У���
	ip_address saddr;      //Դip
	ip_address daddr;     //Ŀ��ip
	u_int op_pad;         //��ѡ+Padding
}ip_header;
typedef struct tcp_header {
	u_short sport;        	//Դ�˿ں�
	u_short dport;         	//Ŀ�Ķ˿ں�
	u_int seq;            	//���к�
	u_int ack;             	//ȷ�Ϻ�
	u_char hlen_flags;  	//�ײ���
	u_char flags;         //������־
	u_short window;      	//���ܴ���
	u_char Checksum[2];           	//У���
	u_short urp;           	//��������֡
}tcp_header;

typedef struct udp_header {
	u_short sport;         //Դ�˿�
	u_short dport;         //Ŀ�Ķ˿�
	u_short len;              //�ײ���
	u_char Checksum[2];        //У���
} udp_header;
typedef struct icmp_t_header {
	u_char type;       //����  
	u_char code;        //���� 
	u_char Checksum[2];        //У���        
	u_char Unused[4];
} icmp_t_header;
typedef struct icmp_p_header {
	u_char type;       //����  
	u_char code;        //���� 
	u_char Checksum[2];        //У���        
	u_char Identifier[2];
	u_char Sequence[2];
} icmp_p_header;
typedef struct arp_header {
	u_char hardware[2];   	//Ӳ������
	u_short type;          	//�ϲ�Э������
	u_char size;			//MAC��ַ����
	u_char Psize;				//�ܳ�
	u_char opType[2];            //��������
	u_char srcMAC[ETHER_ADDR_LEN];        //ԴMAC
	ip_address saddr;      //Դip
	u_char dstMAC[ETHER_ADDR_LEN];        //Ŀ��MAC
	ip_address daddr;     //Ŀ��ip

} arp_header;
void printIP(const u_char* pkt_data, const struct pcap_pkthdr* header,int);
void printTCP(const u_char* pkt_data, const struct pcap_pkthdr* header,int netlen,int totallen)
{
	tcp_header* th;
	cout << "����㣺" << endl;
	printf("�����Э�����ͣ�TCP\n");
	th = (tcp_header*)(pkt_data + 14 + netlen);
	//Դ�˿ں�
	printf("Source Port:%d|", htons(th->sport));
	//Ŀ�Ķ˿ں�
	printf("Destination Port:%d|", htons(th->dport));
	//���к�
	printf("Sequence number(raw):%d|", htons(th->seq));
	//ȷ�Ϻ�
	printf("Acknowledgment number(raw):%d|", htons(th->ack));
	//�ײ���
	int hl = (th->hlen_flags / 16 * 4);
	printf("Header Length:%d|", hl);
	//������־
	printf("Flags:0x%.1x%.2x|",th->hlen_flags % 16,th->flags);
	//���ܴ���
	printf("Window size value:%d|", htons(th->window));
	//У���
	printf("Checksum:0x%.2x%.2x|", th->Checksum[0], th->Checksum[1]);
	//��������֡
	printf("Urgent pointer:%d", htons(th->urp));
	//ѡ��
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
		cout << "Ӧ�ò�" << endl;
		cout << "Ӧ�ò�Э������:HTTP" << endl;
		cout << "Data:0x";
		printf("%.2x-%.2x", pkt_data[totallen], pkt_data[header->len-1]);
		
	}

}
void printUDP(const u_char* pkt_data, const struct pcap_pkthdr* header, int netlen, int totallen)
{
	udp_header* uh;
	cout << "����㣺" << endl;
	printf("�����Э������:UDP\n");
	uh= (udp_header*)(pkt_data + 14 + netlen);
	//Դ�˿ں�
	printf("Source Port:%d|", htons(uh->sport));
	//Ŀ�Ķ˿ں�
	printf("Destination Port:%d|", htons(uh->dport));
	//UDP��С
	printf("Length:%d|", htons(uh->len));
	//У���
	printf("Checksum:0x%.2x%.2x\n", uh->Checksum[0], uh->Checksum[1]);
	cout << "Ӧ�ò㣺" << endl;
	char* httpHeader = (char*)uh + htons(uh->len);
	int httplen = header->len - totallen;
	printf("Data:0x");
	printf("%.2x-%.2x ", pkt_data[42],pkt_data[header->caplen-1]);
}
void printICMP(const u_char* pkt_data, const struct pcap_pkthdr* header, int netlen, int totallen)
{
	printf("�����Э������:ICMP\n");
	icmp_p_header *ich=(icmp_p_header*)(pkt_data + 14 + netlen);
	//����
	printf("Type:%d|",ich->type);
	//����
	printf("Code:%d|", ich->code);
	//У���
	printf("Checksum:0x%.2x%.2x|", ich->Checksum[0],ich->Checksum[1]);
	int type = ich->type;
	//��ѯ����
	if (type == 0 || type == 8 || type == 9 || type == 10 || (type >= 13 && type <= 18))
	{
		printf("Identifier:0x%.2x%.2x|", ich->Identifier[0], ich->Identifier[1]);
		printf("Sequence:0x%.2x%.2x\n", ich->Sequence[0], ich->Sequence[1]);
		printf("Data:0x");
			printf("%.2x-%.2x", pkt_data[22 + netlen], pkt_data[header->len-1]);
		cout << endl;
	}//�����
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
	cout << "����㣺" << endl;
	printf("�����Э������:IPV4\n");
	//������̫��֡ͷ����ȡip�ײ�,addtion��ֹicmpЭ��Ƕ��
	ih = (ip_header*)(pkt_data + 14+addtion);
	//�汾��
	printf("Version:%d|", ih->ver_ihl /16);
	//�ײ���
	int headerLen = (ih->ver_ihl %16 )* 4;
	printf("Header length:%d byte|", headerLen);
	//��������
	printf("Differentiated Services Field:0x%.2x|", ih->tos);
	//�ܳ�
	printf("Total Length:%d|", htons(ih->tlen));
	//��־
	printf("Identification:0x%.2x%.2x|", ih->identification[0], ih->identification[1]);
	//���3bit
	printf("Flags:0x%.2x%.2x|", ih->flags_fo[0], ih->flags_fo[1]);
	//Ƭƫ��
	printf("Fragment offset:%d|", (ih->flags_fo[0] % 32 * 256 + ih->flags_fo[1]));
	//����
	printf("Time to live:%d|", ih->ttl);
	//Э��
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
	//�ײ�У���
	printf("Header checksum:0x%.2x%.2x|", ih->crc[0], ih->crc[1]);
	//Դip
	printf("Source:%d.%d.%d.%d|", ih->saddr.byte1, ih->saddr.byte2, ih->saddr.byte3, ih->saddr.byte4);
	//Ŀ��ip
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
	cout << "����㣺" << endl;
	printf("�����Э������:ARP\n");
	ah = (arp_header*)(pkt_data + 14);
	//Ӳ������
	if (pkt_data[14] == 0 && pkt_data[15] == 1)
		printf("Hardware Type:Ethernet(%d)|", ah->hardware[1]);
	else
		printf("Hardware Type:0x%.2x%.2x|", ah->hardware[0], ah->hardware[1]);
	//�ϲ�Э������
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
	//MAC��ַ����
	printf("Hardware size:%d|", ah->size);
	//�ܳ�
	printf("Protocol size:%d|", ah->Psize);
	//��������
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
	//Դ��ַ
	printf("Sender MAC address:%.2x:%.2x:%.2x:%.2x:%.2x:%.2x|",ah->srcMAC[0], ah->srcMAC[1], ah->srcMAC[2], ah->srcMAC[3], ah->srcMAC[4], ah->srcMAC[5]);
	//Դip
	printf("Sender IP address:%d.%d.%d.%d|", ah->saddr.byte1, ah->saddr.byte2,  ah->saddr.byte3, ah->saddr.byte4);
	//Ŀ�ĵ�ַ
	printf("Target MAC address:%.2x:%.2x:%.2x:%.2x:%.2x:%.2x|",ah->dstMAC[0], ah->dstMAC[1], ah->dstMAC[2], ah->dstMAC[3], ah->dstMAC[4], ah->dstMAC[5]);
	//Ŀ��ip
	printf("Target IP address:%d.%d.%d.%d\n", ah->daddr.byte1, ah->daddr.byte2, ah->daddr.byte3, ah->daddr.byte4);
}
void packet_handler(u_char* param, const struct pcap_pkthdr* header, const u_char* pkt_data)
{
	struct tm ltime;
	char timestr[16];
	ethernet_header* eh;
	//���֡����ʱ��
	time_t temp = header->ts.tv_sec;
	localtime_s(&ltime,&temp);
	strftime(timestr, sizeof(timestr), "%H:%M:%S", &ltime);
	printf("Arrival time: %s|frame length: %d\n", timestr, header->len);
	cout << endl;
	cout << "������·�㣺" << endl;
	printf("������·��Э������:Ehternet\n");
	eh = (ethernet_header*)pkt_data;
	//Ŀ�ĵ�ַ
	printf("Destination:%.2x:%.2x:%.2x:%.2x:%.2x:%.2x|", eh->dest[0],eh->dest[1], eh->dest[2], eh->dest[3], eh->dest[4], eh->dest[5]);
	//Դ��ַ
	printf("Source:%.2x:%.2x:%.2x:%.2x:%.2x:%.2x|", eh->src[0], eh->src[1], eh->src[2], eh->src[3], eh->src[4], eh->src[5]);
	//����
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
	//��ȡ������ϵ��������б�
	if (pcap_findalldevs(&alldevs, errbuf))
	{
		cout << "Error in pcap_findalldevs:" << errbuf << endl;
		return -1;
	}
	//��ӡ�б�
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
	//ѡ��ӿں�
	cout << "Enter the interface number (1-" << i << "):";
	cin >> num;
	//�ӿ��������
	if (num<1 || num>i)
	{
		cout << "Interface number out of range." << endl;
		pcap_freealldevs(alldevs);
		return -1;
	}
	//��ȡ��Ӧ����������
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