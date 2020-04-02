#include <cstdio>
#include <cstdlib>
#include <ctime>
#include <string>
#include<iostream>
#include<fstream>
#include<vector>
#include<algorithm>
#include "openssl/md5.h"
#define MAX_RANDOM 1000000
using namespace std;

//���ݿ�����������������
string MD5(const string& ps, const string& rd)
{
	//��������
	string src = ps + rd;
	MD5_CTX ctx;
	string StrMid5;
	unsigned char md5[16] = { 0 };
	char tmp[33] = { 0 };
	//�õ�mid5
	if (!MD5_Init(&ctx))//��ʼ��MD5_CTX�ṹ
	{
		cout << "MD5 Init failed" << endl;
		exit(-1);
	}
	MD5_Update(&ctx, src.c_str(), src.size());
	MD5_Final(md5, &ctx);
	//mid5תΪ�ַ���
	for (int i = 0; i < 16; ++i)
	{
		memset(tmp, 0, sizeof(tmp));
		sprintf_s(tmp, "%02X", md5[i]);
		StrMid5 += tmp;
	}
	return StrMid5;
}
//��ȡ6λ�������
void MyRandom(vector<int>& numbres)
{
	srand(time(NULL));
	for (int i = 0; i < MAX_RANDOM; i++)
	{
		numbres.push_back(i);
	}
	//�������
	random_shuffle(numbres.begin(), numbres.end());
}
int main()
{
	//���ÿ���
	string Password, MyPassword = "abc123456efg", randomStr;
	string send_md5, recv_md5;
	vector<int> rd;

	cout << "��������Ŀ��";
	cin >> Password;
	cout << "��ǰ��Ҫ��֤�Ŀ��" << Password << endl;

	//��ȡ���ظ������������
	MyRandom(rd);
	int random = rd[0];
	rd.erase(rd.begin());
	randomStr = to_string(random);
	for (int i = randomStr.length(); i < 6; i++)
		randomStr = "0" + randomStr;
	cout << "���ɵ��������" << randomStr << endl;

	//����֤�����������������������md5��������
	send_md5 = MD5(Password, randomStr);
	cout << "��������MD5ժҪֵ" << send_md5 << endl;

	//��֤�����ݱ���Ŀ�����������md5
	recv_md5 = MD5(MyPassword, randomStr);
	cout << "��֤����MD5ժҪֵ:" << recv_md5 << endl;

	if (recv_md5 == send_md5)
		cout << "��֤�ɹ�!" << endl;
	else
		cout << "��֤ʧ��!" << endl;
	return 0;
}