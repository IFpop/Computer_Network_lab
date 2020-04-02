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

//根据口令和随机数生成密文
string MD5(const string& ps, const string& rd)
{
	//生成明文
	string src = ps + rd;
	MD5_CTX ctx;
	string StrMid5;
	unsigned char md5[16] = { 0 };
	char tmp[33] = { 0 };
	//得到mid5
	if (!MD5_Init(&ctx))//初始化MD5_CTX结构
	{
		cout << "MD5 Init failed" << endl;
		exit(-1);
	}
	MD5_Update(&ctx, src.c_str(), src.size());
	MD5_Final(md5, &ctx);
	//mid5转为字符串
	for (int i = 0; i < 16; ++i)
	{
		memset(tmp, 0, sizeof(tmp));
		sprintf_s(tmp, "%02X", md5[i]);
		StrMid5 += tmp;
	}
	return StrMid5;
}
//获取6位的随机数
void MyRandom(vector<int>& numbres)
{
	srand(time(NULL));
	for (int i = 0; i < MAX_RANDOM; i++)
	{
		numbres.push_back(i);
	}
	//随机打乱
	random_shuffle(numbres.begin(), numbres.end());
}
int main()
{
	//设置口令
	string Password, MyPassword = "abc123456efg", randomStr;
	string send_md5, recv_md5;
	vector<int> rd;

	cout << "请输入你的口令：";
	cin >> Password;
	cout << "当前需要验证的口令：" << Password << endl;

	//获取不重复的随机数序列
	MyRandom(rd);
	int random = rd[0];
	rd.erase(rd.begin());
	randomStr = to_string(random);
	for (int i = randomStr.length(); i < 6; i++)
		randomStr = "0" + randomStr;
	cout << "生成的随机数：" << randomStr << endl;

	//被验证方根据输入口令和随机数生成md5，并发送
	send_md5 = MD5(Password, randomStr);
	cout << "发送来的MD5摘要值" << send_md5 << endl;

	//验证方根据保存的口令和随机生成md5
	recv_md5 = MD5(MyPassword, randomStr);
	cout << "验证方的MD5摘要值:" << recv_md5 << endl;

	if (recv_md5 == send_md5)
		cout << "验证成功!" << endl;
	else
		cout << "验证失败!" << endl;
	return 0;
}