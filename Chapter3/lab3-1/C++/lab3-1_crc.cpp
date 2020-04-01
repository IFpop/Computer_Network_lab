#include<cstdio>
#include<cstdlib>
#include<iostream>
#include<cstring>
#include<fstream>
#define LEN 1024
using namespace std;

char* DivideByTwo(char *dividend, char *CRC_CCITT);      //模2除函数，参数为被除数和除数(这里CRC_CCITT只有当左移一位的被除数为1才为除数)
char* CalculateCRC(char *data, char *CRC_CCITT);         //发送端生成CRC
int Check(char *recv_data, char *CRC_CCITT);             // 检验传输是否出错，返回余数

int main(int argc, char *argv[])
{   
    char data[LEN];
    char CRC_CCITT[LEN];
    char recv_data[LEN];
    
    ifstream in(argv[1]);
	if (!in.is_open())
	{
		cout << "文件读取失败！" << endl;
		return 0;
	}
	char buffer[LEN];
    string temp;
	while (!in.eof())
	{
		in >> buffer;
        if (!strcmp("[DEFAULT]", buffer)){
            continue;
        }
        else if (!strcmp("InfoString1", buffer)){
            in >> temp;
            in >> temp;
            strcpy(data, temp.c_str());
        }
        else if (!strcmp("GenXString", buffer)){
            in >> temp;
            in >> temp;
            strcpy(CRC_CCITT, temp.c_str());
        }
        else if (!strcmp("InfoString2", buffer)){
            in >> temp;
            in >> temp;
            strcpy(recv_data, temp.c_str());
        }
	}
    in.close();

    cout << "待发送的数据信息二进制比特串：" << data << endl;
    cout << "CRC-CCITT生成多项式对应的二进制比特串：" << CRC_CCITT << endl;
    char *crc_send;
    crc_send = CalculateCRC(data, CRC_CCITT);
    cout << "计算得到的循环冗余校验码CRC—Code：" << crc_send << endl;

    char send_data[LEN];
    strcpy(send_data, data);
    strcat(send_data, crc_send);
    cout << "带校验和的发送帧：" << send_data << endl;              //用于计算crc2

    cout << endl;
    cout << "接收到的数据信息二进制比特串：" << recv_data <<endl;

    char *crc_recv;
    crc_recv = CalculateCRC(recv_data, CRC_CCITT);
    cout << "计算出的生成的CRC：" << crc_recv <<endl;

    char check_data[LEN];
    strcpy(check_data, recv_data);
    strcat(check_data, crc_send);
    Check(check_data, CRC_CCITT);
   
    return 0;
}

char* DivideByTwo(char *dividend, char *CRC_CCITT)      //模2除函数，参数为被除数和除数(这里CRC_CCITT只有当左移一位的被除数为1才为除数)
{   
    int k = strlen(CRC_CCITT);
    int r = k-1;
    int ptr = k;                    //标记商的位置以判断是否除完
    int len = 0;
    
    char dividend_for_count[LEN]; 
    strcpy(dividend_for_count, dividend);

    char *all_zero;                 //strlen(CRC_CCITT)-1个0
    all_zero = (char *)malloc(r);
    memset(all_zero, '0', r);

    char *remainder;                //余数，strlen(CRC_CCITT)-1位，存dividend的前r位
    remainder = (char *)malloc(k);    
    for(int i = 0; i < k; i++)
    {
        remainder[i] = dividend_for_count[i];
    }
    remainder[k] = '\0';

    char *divisor;                  //除数（生成多项式的后r位或者r个0）
    divisor = (char *)malloc(r);

    len = strlen(dividend_for_count);
    while(ptr < len + 1)           //即dividend的长度
    {      
        if (remainder[0] == '1')
        {   
             for(int i = 0; i < r; i++)
             {
                 divisor[i] = CRC_CCITT[i+1];    //与多项式后r位异或
             }
        }
        else
        {
            strcpy(divisor, all_zero);           //与r个0进行异或
        }

        for (int i = 0; i < r; i++)
        {
            if (remainder[i+1] == divisor[i])
            {
                remainder[i] = '0';
            }
            else
            {
                remainder[i] = '1';
            }
        }
        remainder[r] = dividend_for_count[ptr];     //借位
        ptr++;
    }
    return remainder;
}

char* CalculateCRC(char *data, char *CRC_CCITT)
{
    char data_for_count[LEN];       //添了strlen(CRC_CCITT)-1个0的data
    strcpy(data_for_count, data);
    for(int i = 0; i < strlen(CRC_CCITT)-1; i++)
    {
        strcat(data_for_count, "0");
    }

    return DivideByTwo(data_for_count, CRC_CCITT);
}

int Check(char *recv_data, char *CRC_CCITT)      // 检验传输是否出错，返回余数
{   
    char *crc_check;
    crc_check = DivideByTwo(recv_data, CRC_CCITT);
    for(int i = 0; i < strlen(crc_check); i++)
    {
        if(crc_check[i] == '1')
        {
            cout << "校验的结果（余数）：" << crc_check << "(出错)" << endl; 
            return 0;
        }
    }
    cout << "校验的结果（余数）：" << crc_check << "(无错)" << endl; 
    return 0;
}
