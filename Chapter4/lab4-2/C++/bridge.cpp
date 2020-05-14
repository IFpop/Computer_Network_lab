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
//学习表表项结构
typedef struct worksheet
{
	//MAC
	u_char mac[7];
	//对应模拟端口号即网卡号
	int Sindex;
};
//比较MAC
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
	//学习表
	static list<worksheet> lst;
	//线程锁
	static  mutex mtx;
	//网卡列表
	pcap_if_t* alldevs;
	//对应网卡
	pcap_t* fp;
	//所有网卡
	vector<pcap_t*> vef;
	struct pcap_pkthdr* header;
	//接受帧
	const u_char* pkt_data;
	char errbuf[PCAP_ERRBUF_SIZE];
	//num网卡数，index是对应那张网卡号
	int num = 0, index= 0;
	//转发帧
	void SendFrame(const u_char*,int,int);
public:
	//打印网卡列表
	void printAdapterList();
	//获取网卡列表
	void getAdapterList();
	//选择网卡
	void selectAdapter();
	//捕获帧
	void captureFrame();
	//设置学习表刷新
	void setTimer();
	~Adapter()
	{
		pcap_freealldevs(alldevs);
	}
};
mutex Adapter::mtx;
list<worksheet> Adapter::lst;
//获取适配器列表
void Adapter::getAdapterList() {
	if (pcap_findalldevs(&alldevs, errbuf))
	{
		cout << "Error in pcap_findalldevs:" << errbuf << endl;
		exit(-1);
	}
}
void Adapter::printAdapterList()
{
	//输出每个适配器
	for (pcap_if_t* d = alldevs; d; d = d->next)
	{
		cout << ++num << ". " << d->name << endl;
		if (d->description)
			cout << " (" << d->description << ")" << endl;
		else
			cout << " (No desciption available)" << endl;
		//输出网卡信息
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
		//在vef链表中存储每个适配器
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
//选择网络适配器
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
	//开始抓包
	while ((res = pcap_next_ex(fp, &header, &pkt_data)) >= 0)
	{
		if (res == 0)
			continue;
		//竞争学习表和输出窗口
		mtx.lock();
		cout << "networking adapter"<<index << endl;
		//目的地址
		printf("Destination:%.2x:%.2x:%.2x:%.2x:%.2x:%.2x|", pkt_data[0], pkt_data[1], pkt_data[2], pkt_data[3], pkt_data[4], pkt_data[5]);
		//源地址
		printf("Source:%.2x:%.2x:%.2x:%.2x:%.2x:%.2x\n", pkt_data[6], pkt_data[7], pkt_data[8], pkt_data[9], pkt_data[10], pkt_data[11]);
		u_char dest[7];
		worksheet temp;
		//获取网卡号即端口号
		temp.Sindex = index;
		//获取帧中源地址和目的地址
		for (int i = 0; i < 6; i++)
		{
			dest[i] = pkt_data[i];
			temp.mac[i] = pkt_data[i + 6];
		}
		//查找学习表
		list<worksheet>::iterator itor;
		itor = lst.begin();
		int i = 1;
		//目的端口
		int port =0;
		//判断学习表中是否有源端口的MAC
		boolean flag = false;
		while (itor != lst.end())
		{
			printf("List%d:MAC:%.2x:%.2x:%.2x:%.2x:%.2x:%.2x|", i++,(*itor).mac[0], (*itor).mac[1], (*itor).mac[2], (*itor).mac[3], (*itor).mac[4], (*itor).mac[5]);
			printf("Port %d\n", (*itor).Sindex);
			if (port==0)
			{			//找到相同目的MAC
				if (ustrcmp((*itor).mac, dest, 6))
					port = (*itor).Sindex;
			}
			if (flag==false)
			{	//找相同源MAC		
				flag= ustrcmp((*itor).mac, temp.mac, 6);
			}
			itor++;
		}
	
		if (flag==false)
		{//若找不到源地址，则逆学习
			lst.push_back(temp);
			cout << "逆学习" << endl;
		}//目的地址和源地址来自相同端口则丢失
		if (port==index)
		{
			cout << "丢弃" << endl;

		}//否则转发
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
	if (port)//在学习表中，单播
	{
		cout << "转发" << endl;
		if (pcap_sendpacket(vef[port-1], frame, size) != 0)
		{
			fprintf(stderr, "\nError sending the packet: %s\n", pcap_geterr(fp));
			return;
		}
	}
	else {//不在学习表中,则端口泛洪
		int len = vef.size();
		cout << "泛洪" << endl;
		for (int i = 0; i < len; i++)
		{
			//除了源端口
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
		//每30秒清空
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
	//开始抓包
	ad.captureFrame();
}
void Timer()
{
	Adapter ad3;
	//定义清空学习表
	ad3.setTimer();

}

int main()
{
	Adapter ad1,ad2;
	//配置第一个网络适配器
	ad1.getAdapterList();
	ad1.printAdapterList();
	ad1.selectAdapter();
	//配置第二个网络适配器
	ad2.getAdapterList();
	ad2.printAdapterList();
	ad2.selectAdapter();
	//运行适配器抓包
	thread mythread1(run,ref(ad1));
	thread mythread2(run,ref(ad2));
	//设置学习表老化
	thread mythread3(Timer);
	mythread1.join();
	mythread2.join();
	mythread3.join();
	return 0;
}