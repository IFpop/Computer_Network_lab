#include <stdio.h>
#include <iostream>
#include <fstream>
#include <sstream>
#include <vector>
using namespace std;
#define MAXN 0x3f3f3f3f
#define LL 1024

vector<vector<int>> ReadCfg(string filename);
void Print(vector<vector<int>> martix);
void InitialLink(vector<vector<int>> martix);
vector<vector<int>> SPF(int source, vector<vector<int>> martix);

struct PointList        //每个路由器维护两个列表
{   
    int pathlist[LL];
    int nextpoint[LL] = {0};
};

int main(int argc, char *argv[])
{
    vector<vector<int>> martix;
    martix = ReadCfg(argv[1]);

    cout << "------------Initial link state---------------" << endl;
    InitialLink(martix);

    for(int i = 0; i < martix.size(); i++){
        martix = SPF(i, martix);    
    }

    cout << "----the routing table after SPF processing----" << endl;
    Print(martix);

    return 0;
}

vector<vector<int>> ReadCfg(string filename)
{
    vector<vector<int>> martix;
    ifstream infile;
    infile.open(filename.c_str());
    if(!infile.is_open()){
        cout << "配置文件打开失败" << endl;
        return martix;
    }

    //将文件内容存放在创建的二维数组中
    istringstream instr;
    string str;
    vector<int> vec;
    int temp;
    while(getline(infile, str)){
        instr.str(str);
        while(instr >> temp){
            vec.push_back(temp);
        }

        martix.push_back(vec);
        vec.clear();
        instr.clear();
        str.clear();
    }

    infile.close();
    return martix;
}

void Print(vector<vector<int>> martix)
{   
    cout.setf(std::ios::left);
    for(int i = 0; i < martix.size(); i++){
        for(int j = 0; j < martix[i].size(); j++){
            cout.width(4);
            cout << martix[i][j];
        }
        cout << endl;
    }
    return;
}

void InitialLink(vector<vector<int>> martix)
{   
    for(int i = 0; i < martix.size(); i++){
        cout << "router " << i << " link state: "; 
        for(int j = 0; j < martix[i].size(); j++){
            if(martix[i][j] != 0 && martix[i][j] != 16 && martix[i][j] != 99)
            cout << "(" << j << " " << martix[i][j] << ")";
        }
        cout << endl;
    }
    return;
}


vector<vector<int>> SPF(int source, vector<vector<int>> martix)
{
    PointList point;
    int num = martix.size();
    int dstmin, temp;
    
    cout << "-----update the routing table of route " << source << "-----" << endl;

    for(int i = 0; i < num; i++){
        point.pathlist[i] = 0;      
        point.nextpoint[i] = i;     //先将下一跳都设置为结点自己 
    }

    point.pathlist[source] = 1;     //将自身加入PATH表中

    for(int i = 0; i < num; i++){
        if(i != num - 1)
            cout << "updating......" << endl;
        dstmin = MAXN;  
        temp = source;
        for(int j = 0; j < num; j++){       
            if(martix[source][j] < dstmin && martix[source][j] != 0 
                && martix[source][j] != 16 && martix[source][j] != 99 
                && point.pathlist[j] == 0){    //找一个未加入PATH列表的路由器
                dstmin = martix[source][j];
                temp = j;
                break;
            }
        }

        if(temp != source){
            point.pathlist[temp] = 1;   //加入PATH表
            for(int j = 0; j < num; j++){
                if(martix[source][j] > martix[source][temp] + martix[temp][j]){
                    martix[source][j] = martix[source][temp] + martix[temp][j];
                    point.nextpoint[j] = temp;
                }
            }    
            for(int m = 0; m < num; m++){
                if(point.pathlist[m]){
                    cout << m << " " << point.nextpoint[m] << " " << martix[source][m] << endl;
                }
                else{
                    cout << m << " - -" << endl;
                }
            }
        }
    }
    return martix;
}