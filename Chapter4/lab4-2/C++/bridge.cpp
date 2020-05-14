#include <cstdlib>
#include <cstdio>
#include<iostream>
#define WIN32  
#include<pcap.h>
#include<list>
#include<vector>
#include <thread>         
#include <mutex>
#include<ctime>
using namespace std;
#define LINE_LEN 16
//ѧϰ�����ṹ
typedef struct worksheet
{
	//MAC
	u_char mac[7];
	//��Ӧģ��˿ںż�������
	int Sindex;
};
//�Ƚ�MAC
boolean ustrcmp(const u_char* s1, const u_char* s2,int num)
{
	if (s1 == s2)return true;
	if (s1 == NULL) return false;
	if (s2 == NULL) return false;
	for (int i = 0; i < num; i++)
	{
		if (s1[i] != s2[i])
			return false;
	}
	return true;
}
class Adapter
{
	//ѧϰ��
	static list<worksheet> lst;
	//�߳���
	static  mutex mtx;
	//�����б�
	pcap_if_t* alldevs;
	//��Ӧ����
	pcap_t* fp;
	//��������
	vector<pcap_t*> vef;
	struct pcap_pkthdr* header;
	//����֡
	const u_char* pkt_data;
	char errbuf[PCAP_ERRBUF_SIZE];
	//num��������index�Ƕ�Ӧ����������
	int num = 0, index= 0;
	//ת��֡
	void SendFrame(const u_char*,int,int);
public:
	//��ӡ�����б�
	void printAdapterList();
	//��ȡ�����б�
	void getAdapterList();
	//ѡ������
	void selectAdapter();
	//����֡
	void captureFrame();
	//����ѧϰ��ˢ��
	void setTimer();
	~Adapter()
	{
		pcap_freealldevs(alldevs);
	}
};
mutex Adapter::mtx;
list<worksheet> Adapter::lst;
//��ȡ�������б�
void Adapter::getAdapterList() {
	if (pcap_findalldevs(&alldevs, errbuf))
	{
		cout << "Error in pcap_findalldevs:" << errbuf << endl;
		exit(-1);
	}
}
void Adapter::printAdapterList()
{
	//���ÿ��������
	for (pcap_if_t* d = alldevs; d; d = d->next)
	{
		cout << ++num << ". " << d->name << endl;
		if (d->description)
			cout << " (" << d->description << ")" << endl;
		else
			cout << " (No desciption available)" << endl;
		//���������Ϣ
		pcap_addr_t* a;
		for (a = d->addresses; a; a = a->next)
		{
			if (a->addr->sa_family == AF_INET)
			{
				if (a->addr)
				{
					u_char* adr = (u_char*)&((struct sockaddr_in*)a->addr)->sin_addr.s_addr;
					printf("IPv4 adrress: %d.%d.%d.%d\n", adr[0], adr[1], adr[2], adr[3]);
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
		//��vef�����д洢ÿ��������
		pcap_t* temp;
		if ((temp = pcap_open_live(d->name, 100, 1, 20, errbuf)) == NULL)
		{
			cout << "Error opening source: " << errbuf << endl;
			exit(-1);
		}
		vef.push_back(temp);
	}
	if (num == 0)
	{
		cout << "No interfaces found! Exiting." << endl;
		exit(-1);
	}
	
}
//ѡ������������
void Adapter::selectAdapter()
{
	cout << "Enter the interface number (1-" << num << "):";
	cin >> index;
	if (index<1 || index>num)
	{
		cout << "Interface number out of range." << endl;
		pcap_freealldevs(alldevs);
		exit(-1);
	}
	fp = vef[index-1];
}
void Adapter::captureFrame()
{
	int res;
	//��ʼץ��
	while ((res = pcap_next_ex(fp, &header, &pkt_data)) >= 0)
	{
		if (res == 0)
			continue;
		//����ѧϰ����������
		mtx.lock();
		cout << "networking adapter"<<index << endl;
		//Ŀ�ĵ�ַ
		printf("Destination:%.2x:%.2x:%.2x:%.2x:%.2x:%.2x|", pkt_data[0], pkt_data[1], pkt_data[2], pkt_data[3], pkt_data[4], pkt_data[5]);
		//Դ��ַ
		printf("Source:%.2x:%.2x:%.2x:%.2x:%.2x:%.2x\n", pkt_data[6], pkt_data[7], pkt_data[8], pkt_data[9], pkt_data[10], pkt_data[11]);
		u_char dest[7];
		worksheet temp;
		//��ȡ�����ż��˿ں�
		temp.Sindex = index;
		//��ȡ֡��Դ��ַ��Ŀ�ĵ�ַ
		for (int i = 0; i < 6; i++)
		{
			dest[i] = pkt_data[i];
			temp.mac[i] = pkt_data[i + 6];
		}
		//����ѧϰ��
		list<worksheet>::iterator itor;
		itor = lst.begin();
		int i = 1;
		//Ŀ�Ķ˿�
		int port =0;
		//�ж�ѧϰ�����Ƿ���Դ�˿ڵ�MAC
		boolean flag = false;
		while (itor != lst.end())
		{
			printf("List%d:MAC:%.2x:%.2x:%.2x:%.2x:%.2x:%.2x|", i++,(*itor).mac[0], (*itor).mac[1], (*itor).mac[2], (*itor).mac[3], (*itor).mac[4], (*itor).mac[5]);
			printf("Port %d\n", (*itor).Sindex);
			if (port==0)
			{			//�ҵ���ͬĿ��MAC
				if (ustrcmp((*itor).mac, dest, 6))
					port = (*itor).Sindex;
			}
			if (flag==false)
			{	//����ͬԴMAC		
				flag= ustrcmp((*itor).mac, temp.mac, 6);
			}
			itor++;
		}
	
		if (flag==false)
		{//���Ҳ���Դ��ַ������ѧϰ
			lst.push_back(temp);
			cout << "��ѧϰ" << endl;
		}//Ŀ�ĵ�ַ��Դ��ַ������ͬ�˿���ʧ
		if (port==index)
		{
			cout << "����" << endl;

		}//����ת��
		else {
			
			SendFrame(pkt_data,port,header->len);
		}
		cout << endl << endl;
		mtx.unlock();
	}
	if (res == -1)
	{
		cout << "Error reading the packets: " << pcap_geterr(fp) << endl;
		exit(-1);

	}
}
void Adapter::SendFrame(const u_char* frame, int port,int size)
{
	if (port)//��ѧϰ���У�����
	{
		cout << "ת��" << endl;
		if (pcap_sendpacket(vef[port-1], frame, size) != 0)
		{
			fprintf(stderr, "\nError sending the packet: %s\n", pcap_geterr(fp));
			return;
		}
	}
	else {//����ѧϰ����,��˿ڷ���
		int len = vef.size();
		cout << "����" << endl;
		for (int i = 0; i < len; i++)
		{
			//����Դ�˿�
			if (i + 1 == index)
				continue;
			if (pcap_sendpacket(vef[i], frame, size) != 0)
			{
				fprintf(stderr, "\nError sending the packet: %s\n", pcap_geterr(fp));
			}

		}
	}

}
void Adapter::setTimer()
{
	clock_t start, end;
	start = clock();
	while (true)
	{
		end = clock();
		//ÿ30�����
		if (end - start > 30000)
		{
			start = end;
			mtx.lock();
			vef.clear();
			mtx.unlock();
		}
	}
}
void run(Adapter &ad)
{
	//��ʼץ��
	ad.captureFrame();
}
void Timer()
{
	Adapter ad3;
	//�������ѧϰ��
	ad3.setTimer();

}

int main()
{
	Adapter ad1,ad2;
	//���õ�һ������������
	ad1.getAdapterList();
	ad1.printAdapterList();
	ad1.selectAdapter();
	//���õڶ�������������
	ad2.getAdapterList();
	ad2.printAdapterList();
	ad2.selectAdapter();
	//����������ץ��
	thread mythread1(run,ref(ad1));
	thread mythread2(run,ref(ad2));
	//����ѧϰ���ϻ�
	thread mythread3(Timer);
	mythread1.join();
	mythread2.join();
	mythread3.join();
	return 0;
}