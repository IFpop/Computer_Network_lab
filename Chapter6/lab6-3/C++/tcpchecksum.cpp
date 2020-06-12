#include<cstdio>
#include<cstdlib>
#include<iostream>
#include<string.h>
#include<string>
#include<fstream>
#define LEN 1024
using namespace std;

char tcpcheck[4];
int char2num(char ch);
char itox(int a, int flag);
void iptox(char *data);

int main(int argc, char *argv[])
{   
    char tcpsegment[LEN];
    char sourceip[LEN];
    char destip[LEN];
    int tcplen;

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
        else if (!strcmp("tcpsegment", buffer)){
            in >> temp;
            in >> temp;
            strcpy(tcpsegment, temp.c_str());
        }
        else if (!strcmp("sourceip", buffer)){
            in >> temp;
            in >> temp;
            strcpy(sourceip, temp.c_str());
        }
        else if (!strcmp("destip", buffer)){
            in >> temp;
            in >> temp;
            strcpy(destip, temp.c_str());
        }
	}
    in.close();

    tcplen = strlen(tcpsegment) / 2;
    cout << "------information of pseudo header------" << endl;
    cout << "Source IP: " << sourceip << endl;
    cout << "Destination IP: " << destip << endl;
    cout << "TCP Protocol Type Value: 6" << endl;
    cout << "TCP Total Length: " << tcplen << endl;
    cout << endl;
    cout << "--------information of tcp header-------" << endl;
    cout << "Source Port: " 
    << char2num(tcpsegment[0]) * 16 * 16 * 16 
    + char2num(tcpsegment[1]) * 16 * 16 
    + char2num(tcpsegment[2]) * 16 + char2num(tcpsegment[3]) << endl;
    cout << "Destination Port: "
    << char2num(tcpsegment[4]) * 16 * 16 * 16 
    + char2num(tcpsegment[5]) * 16 * 16 
    + char2num(tcpsegment[6]) * 16 + char2num(tcpsegment[7]) << endl;
    cout << "Sequence number: 0x" << tcpsegment[8] << tcpsegment[9]  << tcpsegment[10] << tcpsegment[11] << tcpsegment[12]
    << tcpsegment[13] << tcpsegment[14] << tcpsegment[15] << endl;
    cout << "Acknowledgement number: 0x" << tcpsegment[16] << tcpsegment[17]  << tcpsegment[18] << tcpsegment[19] << tcpsegment[20]
    << tcpsegment[21] << tcpsegment[22] << tcpsegment[23] << endl;
    cout << "Header Length: 0x" << tcpsegment[24] << tcpsegment [25] << endl;
    cout << "Flags: 0x" << tcpsegment[24] << tcpsegment[25] << tcpsegment[26] << tcpsegment[27] << endl;
    cout << "Window size: 0x" << tcpsegment[28] << tcpsegment[29] << tcpsegment[30] << tcpsegment[31] << endl;
    cout << "Checksum: 0x" << tcpsegment[32] << tcpsegment[33] << tcpsegment[34] << tcpsegment[35] << endl;
    cout << "Urgent pointer: 0x" << tcpsegment[36] << tcpsegment[37] << tcpsegment[38] << tcpsegment[39] << endl;

    //校验和部分清0
    *(tcpsegment + 16 * 2) = '0';
    *(tcpsegment + 16 * 2 + 1) = '0';
    *(tcpsegment + 17 * 2) = '0';
    *(tcpsegment + 17 * 2 + 1) = '0';

    //tcp报文
    int j = 0;
    int re = 0;
    for(int i = 0; i < tcplen * 2; i += 2){
        int n;
        n = char2num(tcpsegment[i]) * 16 + char2num(tcpsegment[i+1]);
        if(j % 2 == 0){
            re += n * 256; 
        }
        else{
            re += n;
        } 
        j++;
    }

    //长度信息
    re += tcplen;
    
    //协议类型
    re += 6;            

    //源IP
    int a = 0;
    j = 0;
    for(int i = 0; i < strlen(sourceip); i++){
        a *= 10;
        if(sourceip[i] == '.'){
            a /= 10;
            if(j % 2 == 0){
                a *= 256;
            }
            re += a;    
            a = 0;
            j++;
        }
        else{
            a += char2num(sourceip[i]);
        }
    }
    re += a;

    //目的IP
    a = 0;
    j = 0;
    for(int i = 0; i < strlen(destip); i++){
        a *= 10;
        if(destip[i] == '.'){
            a /= 10;
            if(j % 2 == 0){
                a *= 256;
            }
            re += a;    
            a = 0;
            j++;
        }
        else{
            a += char2num(destip[i]);
        }
    }
    re += a;

    //转换成十六进制输出计算所得的校验和结果
    re = re % (16 * 16 * 16 * 16) + re / (16 * 16 * 16 * 16);
    int cs = 0;
    j = 4;
    while(j){
        j--;
        cs = re % 16;
        re /= 16;
        tcpcheck[j] = itox(cs, 1);
    }
    
    //输出计算好的校验和
    cout << endl;
    cout << "Calaculate checksum is: 0x";
    for(int i = 0; i < 4; i++){
        cout << tcpcheck[i];
    }
    cout << endl;

    return 0;
}

int char2num(char ch) /*将字baidu符转成zhi数字*/
{
    if (ch>='a')
        return ch-'a'+10;
    else if (ch>='A')
        return ch-'A'+10;
    else
        return ch-'0';
}

char itox(int a, int flag)
{   
    if(flag){
        a = 15 - a;         //取反码操作
    }
    if(a >= 0 && a <= 9)
        return a + '0';
    else{
        if(a == 10){
            return 'a';
        }else if(a == 11){
            return 'b';
        }else if(a == 12){
            return 'c';
        }else if(a == 13){
            return 'd';
        }else if(a == 14){
            return 'e';
        }else if(a == 15){
            return 'f';
        }
    }
}

void iptox(char *data)
{
    //源IP
    int a = 0, re = 0;
    for(int i = 0; i < strlen(data); i++){
        a *= 10;
        if(data[i] == '.'){
            a /= 10;
            cout << itox(a / 16, 0) << itox(a % 16, 0);
            a = 0;
        }
        else{
            a += char2num(data[i]);
        }
    }
    cout << itox(a / 16, 0) << itox(a % 16, 0) << endl;
    return;
}