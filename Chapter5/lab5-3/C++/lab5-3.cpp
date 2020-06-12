#include <cstdio>
#include <cstdlib>
#include <cmath>
#include <iostream>
#include <cstring>
#include <fstream>
#define LEN 1024
#define IDLEN 20
using namespace std;

int BigIPTotalLen;
int ID;
int MTU;
int FragNum;

struct Reload
{
    int TotalLen;
    int ID;
    int FragMF;
    int FragOffset;
}reload[5];

void ShardingCfg();
void ReloadCfg();

int main(int argc, char *argv[])
{

    
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
        else if (!strcmp("BigIPTotalLen", buffer)){
            in >> temp;
            in >> temp;
            BigIPTotalLen = atoi(temp.c_str());
        }
        else if (!strcmp("ID1", buffer)){
            in >> temp;
            in >> temp;
            ID = atoi(temp.c_str());
        }
        else if (!strcmp("MTU", buffer)){
            in >> temp;
            in >> temp;
            MTU = atoi(temp.c_str());
        }
        else if (!strcmp("FragNum", buffer)){
            in >> temp;
            in >> temp;
            FragNum = atoi(temp.c_str());
        } 

        if(!strcmp("TotalLen", buffer)){
            in >>temp;
            for(int i = 0; i < FragNum; i++){
                in >> temp;
                reload[i].TotalLen = atoi(temp.c_str());
                if(i != FragNum - 1){
                    in >> temp;
                }
            }
        }
        else if(!strcmp("ID2", buffer)){
            in >>temp;
            for(int i = 0; i < FragNum; i++){
                in >> temp;
                reload[i].ID = atoi(temp.c_str());
                if(i != FragNum - 1){
                    in >> temp;
                }
            }
        }
        else if(!strcmp("FragMF", buffer)){
            in >>temp;
            for(int i = 0; i < FragNum; i++){
                in >> temp;
                reload[i].FragMF = atoi(temp.c_str());
                if(i != FragNum - 1){
                    in >> temp;
                }
            }
        }
        else if(!strcmp("FragOffset", buffer)){
            in >>temp;
            for(int i = 0; i < FragNum; i++){
                in >> temp;
                reload[i].FragOffset = atoi(temp.c_str());
                if(i != FragNum - 1){
                    in >> temp;
                }
            }
        }
	}
    in.close();

    ShardingCfg();

    cout << endl;

    ReloadCfg();
    
    return 0;
}

void ShardingCfg()
{
    cout << "原始大数据报的主要分片字段信息：" << endl;
    cout << "总长度：" << BigIPTotalLen << " | " << "标识：" << ID << " | ";
    cout << "DF = 1 | MF = 0 | " << "片段偏移：0" << endl;
    cout << "最大MTU值：" << MTU << endl;

    int piecenum;
    piecenum = ceil((double)(BigIPTotalLen - IDLEN) / (double)(MTU - IDLEN));
    cout << "产生的分片数：" << piecenum << endl;

    int locallen = 0;
    int fragoffset;
    for(int i = 0; i < piecenum; i++){
        if(i != piecenum -1){
            locallen += MTU - IDLEN;
            fragoffset = i * (MTU - IDLEN) / 8;
            cout << "第" << i + 1 << "个分片的信息：" <<  "总长度：" << MTU << " | ";
            cout << "标识：" << ID << " | " << "DF = 0 | MF = 1 | 片段偏移：" << fragoffset << endl;
        }
        else{
            fragoffset = i * (MTU - IDLEN) / 8;
            cout << "第" << i + 1 << "个分片的信息：" <<  "总长度：" << BigIPTotalLen - locallen << " | ";
            cout << "标识：" << ID << " | " << "DF = 0 | MF = 0 | 片段偏移：" << fragoffset << endl;
        }
    }
}

void ReloadCfg()
{
    cout << "即将完成分片的重装" << endl;
    cout << "分片的数量：" << FragNum << endl;
    cout << "下面是每个分片的分片信息：" << endl;

    for(int i = 0; i < FragNum; i++){
        cout << "第" << i + 1 << "个分片的信息：总长度：" << reload[i].TotalLen << " | ";
        cout << "标识：" << reload[i].ID << " | " << "DF = 0 | MF = " << reload[i].FragMF << " | 片段偏移：" << reload[i].FragOffset << endl;
    }

    int totallen = 0;
    for(int i = 0; i < FragNum; i++){
        totallen += reload[i].TotalLen - IDLEN;             //有效数据的长度
    }
    totallen += IDLEN;

    cout << "重装后数据报的分片信息：" << endl;
    cout << "总长度：" << totallen << " | " << "标识：" << reload[0].ID << " | DF = 1 | MF = 0 | 片段偏移：0" << endl;
}