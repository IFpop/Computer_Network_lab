#include <cstdio>
#include <cstdlib>
#include <ctime>
#include <string>
#include<iostream>
#include<windows.h>
#include<fstream>
#define BUFF 1024
#define RIT 1000
#define RTM 500
#define RTC 5000
#define WTM 500
#define WTC 2000
#define FILE1 "1.cfg"
#define FILE2 "2.cfg"
using namespace std;
//获取串口信息
void GetCommInfo(string &name, DCB &dcb,string filename,COMMTIMEOUTS &TimeOuts)
{
	ifstream in(filename.c_str());
	if (!in.is_open())
	{
		cout << "文件读取失败！" << endl;
		exit(-1);
	}
	char buffer[BUFF];
	while (!in.eof())
	{
		in >> buffer;
		string temp;
		//串口名
		if (!strcmp("SerialPort:", buffer))
		{
			in >> name;
		}//波特率
		else if (!strcmp("BaudRate:", buffer)) {
			
			in >> temp;
			dcb.BaudRate = atoi(temp.c_str());
		}//校验位
		else if (!strcmp("Parity:", buffer)) {
			in >> temp;
			dcb.Parity = atoi(temp.c_str());
		}//数据位
		else if (!strcmp("DataBits:", buffer)) {
			in >> temp;
			dcb.ByteSize = atoi(temp.c_str());
		}//停止位
		else if (!strcmp("stopBits:", buffer)) {
			in >> temp;
			dcb.StopBits = atoi(temp.c_str());
		}
	}
	TimeOuts.ReadIntervalTimeout = RIT;
	TimeOuts.ReadTotalTimeoutMultiplier = RTM;
	TimeOuts.ReadTotalTimeoutConstant = RTC;
	TimeOuts.WriteTotalTimeoutMultiplier = WTM;
	TimeOuts.WriteTotalTimeoutConstant = WTC;
	in.close();
}

void ModifyDcb(DCB &dcb, DCB dcb1)
{
	dcb.BaudRate = dcb1.BaudRate;
	dcb.ByteSize = dcb1.ByteSize;
	dcb.Parity = dcb1.Parity;
	dcb.StopBits = dcb1.StopBits;
}
//输出端口信息
void PrintDcb(DCB dcb)
{
	cout <<"BaudRate: "<<dcb.BaudRate << endl;
	cout << "DataBits: " << (int)(dcb.ByteSize) << endl;
	cout << "Parity: " << (int)(dcb.Parity) << endl;
	cout << "StopBits: " << (int)(dcb.StopBits) << endl;
}
int main(int args,char **argv)
{
	
	HANDLE hCom1;//串口
	string Cname1;//端口名
	DCB dcb,dcb1;
	COMMTIMEOUTS to1;
	cout << "current config file: " << argv[1] << endl;
	//配置串口
	GetCommInfo(Cname1, dcb1,argv[1],to1);
	hCom1 = CreateFile(Cname1.c_str(), GENERIC_READ | GENERIC_WRITE, 0, NULL, OPEN_EXISTING,0,NULL);
	if (hCom1 ==  INVALID_HANDLE_VALUE)
	{
		cout << "打开COM失败" << endl;
		exit(-1);
	}
	SetupComm(hCom1, BUFF, BUFF);
	GetCommTimeouts(hCom1, &to1);
	SetCommState(hCom1, &dcb);
	ModifyDcb(dcb, dcb1);
	SetCommState(hCom1, &dcb1);
	//输出端口信息
	cout << "SerialPort: " << Cname1 << endl;
	PrintDcb(dcb);
	//清空缓冲区
	PurgeComm(hCom1, PURGE_TXCLEAR | PURGE_RXCLEAR);
	cout << Cname1<<"配置成功" << endl;
	cout << "Start connect..." << endl;

	DWORD dwLength;
	char recvBuf[1024];
	DWORD dwactlen;
	char psendbuf[BUFF] ;
	char ch;
	int flag = atoi(argv[2]);
	while (true)
	{
		if (flag==1)
		{
			//发送数据
			cout << "Please input the message to send: ";
			cin >> psendbuf;
			cout << "send message: " << psendbuf << endl;
			cout << endl;
			PurgeComm(hCom1, PURGE_TXCLEAR );
			WriteFile(hCom1, psendbuf, BUFF, &dwactlen, NULL);

		}
		else if(flag==0){//接受数据
			ReadFile(hCom1, recvBuf, BUFF, &dwLength, NULL);
			if (recvBuf[0] != '\0') {
				cout << "Received message: " << recvBuf << endl;
				cout << endl;
			}
			recvBuf[0] = '\0';
			PurgeComm(hCom1, PURGE_RXCLEAR);
		}
		else {
			cout << "argv error!" << endl;
			exit(-1);
		}


	}
	CloseHandle(hCom1);

	return 0;
}
