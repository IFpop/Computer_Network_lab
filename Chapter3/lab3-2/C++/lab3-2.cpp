#include <cstdio>
#include <cstdlib>
#include <ctime>
#include <string.h>
#include <string>
#include <iostream>
#include <fstream>
#include <list>
#define BUFF 1024
using namespace std;

//获取信息和开始结束标志
void GetInfo(string& infoString1, string& FlagString,string FileName)
{
	ifstream in(FileName.c_str());
	if (!in.is_open())
	{
		cout << "文件读取失败！" << endl;
		exit(-1);
	}
	char buffer[BUFF];
	while (!in.eof())
	{
		in >> buffer;
		//获取待发送数据
		if (!strcmp("InfoString1=", buffer))
		{
			in >> infoString1;
		}//获取起始结束标志
		else if (!strcmp("FlagString=", buffer)) 
		{
			in >> FlagString;
		}
	}
	in.close();
}
//显示信息和开始结束标志
void PrintConfig(string infoString1, string FlagString)
{
	cout << "BeginFlagString: " << FlagString << endl;
	cout << "InfoString: " << infoString1 << endl;
	cout << "EndFlagString: " << FlagString << endl;
}

//处理待发送的比特填充信息
string DealBitString(string infoString1)
{
	int num = 0;
	for (int i = 0; infoString1[i]!='\0'; i++)
	{
		if (infoString1[i] == '1')//记录连续1的个数
		{
			num++;
		}
		else
			num = 0;
		if (num == 5)//连续5个1
		{
			infoString1.insert(i+1, "0");
		}
	}
	return infoString1;
}

//处理待发送的字符填充信息
string DealBtyeString(string infoString1, string FlagString)
{
	int len = infoString1.length();
	int sublen = FlagString.length();
	string res = "";
	for (int i = 0; i < len; i += 2)
	{
		//若在数据信息中出现标志或者ESC字符
		if (infoString1.substr(i, sublen) == FlagString || infoString1.substr(i, 2) == "1B")
		{
			res += "1B";
		}
		res += infoString1.substr(i, sublen);
	}
	return res;
}
//处理接受的比特填充信息
void GetBitString(string infoString1, string FlagString, list<string>& resList)
{
	int len = infoString1.length();
	int flen = FlagString.length();
	int num = 0;
	bool flag = 1;
	string res = "";
	for (int i = 0; i<len-flen+1; i++)
	{
		//若识别出标志
		if (infoString1.substr(i, flen) == FlagString)
		{
			if (flag == 1)//开始标志
			{
				flag = 0;
				res = "";
				i += (flen - 1);
			}
			else {//结束标志
				flag = 1;
				//获取帧
				resList.push_back(res);
				res = "";
				i += (flen - 1);
			}
		}
		else if(flag == 0)
		{
			if (infoString1[i] == '0' && num == 5)//5个1后出现0
			{
				num = 0;
			}
			else if (infoString1[i] == '0')//其他0
			{
				res += infoString1[i];
				num = 0;
			}
			else {//记录连续1的个数
				num++;
				res += infoString1[i];
			}
		}
	}
}

//处理接受的字符填充信息
void GetBtyeString(string infoString1, string FlagString, list<string> &resList)
{
	int len = infoString1.length();
	int flen = FlagString.length();
	int num = 0;
	bool flag = 1,FlagESC=0;
	string res = "";
	for (int i = 0; i < len  + 1; i+=2)
	{
		if (flag == 1)
		{
			if (infoString1.substr(i, flen) == FlagString)//开始标志
			{
				res = "";
				flag = 0;
			}
		}
		else 
		{
			string temp = infoString1.substr(i, flen);
			if (FlagESC == 1)//在每一个ESC之后的一个字节
			{
				res += temp;
				FlagESC = 0;
			}
			else 
			{
				if (temp == "1B")//识别到ESC
					FlagESC = 1;
				else 
				{
					if (infoString1.substr(i, flen) == FlagString)//结束标志
					{
						flag = 1;
						resList.push_back(res);
					}
					else
						res += temp;
				}
			}
		}
	}
}
int main(int args, char** argv)
{
	if (args != 3)
	{
		cout << "argument error" << endl;
		exit(-1);
	}
	string infoString = "", FlagString = "";	//信息，开始结束标志
	list<string>  RecieveList;					//存放接受信息
	//读取配置文件
	GetInfo(infoString, FlagString, argv[1]);
	string SendString = "", RecieveString;
	PrintConfig(infoString, FlagString);
	//比特填充
	if (!strcmp(argv[2], "0"))
	{
		SendString += FlagString;
		SendString += DealBitString(infoString);
		SendString += FlagString;
		cout << "SendString: " << SendString << endl;
		GetBitString(SendString, FlagString, RecieveList);
		while (!RecieveList.empty())
		{
			cout << "RecieveString: " << RecieveList.front() << endl;
			RecieveList.pop_front();
		}
	}
	else if (!strcmp(argv[2], "1"))//字节填充
	{
		SendString += FlagString;
		SendString += DealBtyeString(infoString, FlagString);
		SendString += FlagString;
		cout << "SendString: " << SendString << endl;
		GetBtyeString(SendString, FlagString, RecieveList);
		while (!RecieveList.empty())
		{
			cout << "RecieveString: " << RecieveList.front() << endl;
			RecieveList.pop_front();
		}
	}
	return 0;
}