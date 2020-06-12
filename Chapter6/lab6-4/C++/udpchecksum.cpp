#include<cstdio>
#include<cstdlib>
#include<iostream>
#include<string.h>
#include<string>
#include<fstream>
#define LEN 1024
using namespace std;

char udpcheck[4];
int char2num(char ch);
char itox(int a);

int main(int argc, char *argv[])
{   
    char udpsegment[LEN];
    char sourceip[LEN];
    char destip[LEN];
    char message[LEN];
    int udplen;
    int msglen;

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
        else if (!strcmp("udpsegment", buffer)){
            in >> temp;
            in >> temp;
            strcpy(udpsegment, temp.c_str());
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

    udplen = strlen(udpsegment) / 2;
    cout << "------Information of pseudo header------" << endl;
    cout << "Source IP: " << sourceip << endl;
    cout << "Destinatoin IP: " << destip << endl;
    cout << "UDP Protocol Type Value: 17" << endl;
    cout << "UDP Total Length: " << udplen << endl;
    cout << endl;
    cout << "--------information of tcp header-------" << endl;
    cout << "Source Port: " 
    << char2num(udpsegment[0]) * 16 * 16 * 16 
    + char2num(udpsegment[1]) * 16 * 16 
    + char2num(udpsegment[2]) * 16 + char2num(udpsegment[3]) << endl;
    cout << "Destination Port: "
    << char2num(udpsegment[4]) * 16 * 16 * 16 
    + char2num(udpsegment[5]) * 16 * 16 
    + char2num(udpsegment[6]) * 16 + char2num(udpsegment[7]) << endl;
    cout << "UDP Length: 0x" << udpsegment[8] << udpsegment[9]  << udpsegment[10] << udpsegment[11] << endl;
    cout << "Checksum: 0x" << udpsegment[12] << udpsegment[13]  << udpsegment[14] << udpsegment[15] << endl;

    //校验和部分清0
    *(udpsegment + 6 * 2) = '0';
    *(udpsegment + 6 * 2 + 1) = '0';
    *(udpsegment + 7 * 2) = '0';
    *(udpsegment + 7 * 2 + 1) = '0';

    //udp报文
    int j = 0;
    int re = 0;
    for(int i = 0; i < udplen * 2; i += 2){
        int n;
        n = char2num(udpsegment[i]) * 16 + char2num(udpsegment[i+1]);
        if(j % 2 == 0){
            re += n * 256; 
        }
        else{
            re += n;
        } 
        j++;
    }

    //长度信息
    re += udplen;   // + msglen;
    
    //协议类型
    re += 17;            

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
        udpcheck[j] = itox(cs);
    }

    cout << endl;
    cout << "Calaculate checksum is: 0x";
    for(int i = 0; i < 4; i++){
        cout << udpcheck[i];
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

char itox(int a)
{   
    a = 15 - a;         //取反码操作
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