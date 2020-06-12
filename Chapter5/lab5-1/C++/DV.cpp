#include <stdio.h>
#include <iostream>
#include <fstream>
#include <sstream>
#include <vector>
#include <queue>
using namespace std;

vector<vector<int>> ReadCfg(string filename);
void Print(vector<vector<int>> martix);
vector<vector<int>> DV(vector<vector<int>> martix);
vector<vector<int>> ExchangeInfo(int router1, int router2, vector<vector<int>> martix);
void RouterPrint(int num, vector<vector<int>> martix);

int totalchange = 0;
vector<vector<int>> nexthop;        //记录下一跳

int main(int argc, char *argv[])
{
    vector<vector<int>> martix;
    martix = ReadCfg(argv[1]);

    cout << "before update the routertable is" << endl;
    Print(martix);

    martix = DV(martix);
    cout << "after update the routertable is" << endl;
    Print(martix);

    while(true){
        totalchange = 0;
        cout << "------------------Menu------------------" << endl;
        cout << "1--modify network topo" << endl;
        cout << "2--print routertable of specified router" << endl;
        cout << "3--update all routertable" << endl;
        cout << "4--print all routertable" << endl;
        cout << "0--quit the program" << endl;
        cout << "please input your option:";

        int conf;
        cin >> conf;
        if(conf == 1){
            int router1, router2, dst;
            cout << "please input the source router:";
            cin >> router1;
            cout << "please input the destination:";
            cin >> router2;
            cout << "please input the new distance:";
            cin >> dst;

            martix[router1][router2] = dst;
            martix[router2][router1] = dst;
        }
        else if(conf == 2){
            cout << "enter router number";
            int num;
            cin >> num;
            RouterPrint(num, martix);
        }
        else if(conf == 3){
            martix = DV(martix);
        }
        else if(conf == 4){
            Print(martix);
        }
        else if(conf == 0){
            break;
        }
        else{
            cout << "option does not exist" << endl;
        }
    }
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
        nexthop.push_back(vec);
        vec.clear();
        instr.clear();
        str.clear();
    }

    infile.close();
    for(int i = 0; i < nexthop.size(); i++){
        for(int j = 0; j < nexthop.size(); j++){
            nexthop[i][j] = j;
        }
    }
    return martix;
}

void Print(vector<vector<int>> martix)
{   
    for(int i = 0; i < martix.size(); i++){
        cout << "------Router Table----------" << endl;
        cout << "Router:" << i << endl;
        cout << "Des   dis   Nexthop" << endl;

        cout.setf(std::ios::left);
        for(int j = 0; j < martix[i].size(); j++){
            cout.width(6);
            cout << j;
            cout.width(7);
            cout << martix[i][j];
            cout << nexthop[i][j] << endl;
        }
    }
    return;
}

void RouterPrint(int num, vector<vector<int>> martix)
{   
    cout << "Router:" << num << endl;
        cout << "Des   dis   Nexthop" << endl;

        cout.setf(std::ios::left);
        for(int j = 0; j < martix[num].size(); j++){
            cout.width(6);
            cout << j;
            cout.width(7);
            cout << martix[num][j];
            cout << nexthop[num][j] << endl;
        }
    return;
}

vector<vector<int>> DV(vector<vector<int>> martix)
{
    int num = martix.size();
    int change = 0;
    int local;
    queue<int> q_src;
    queue<int> q_dst;
    while(true){
        local = totalchange;
        for(int i = 0; i < num; i++){
            for(int j = 0; j < num; j++){
                if(martix[i][j] != 0 && martix[i][j] != 99 && martix[i][j] != 16){    //判断两路由器是否相邻
                    q_src.push(i);
                    q_dst.push(j);
                } 
            }
        }
        
        while(!q_src.empty()){
            martix = ExchangeInfo(q_src.front(), q_dst.front(), martix);     
            q_src.pop();
            q_dst.pop();
        }

        if(local == totalchange){
            break;
        }
    }
    cout << "All update " << totalchange << " times" << endl;
    return martix;
}

vector<vector<int>> ExchangeInfo(int router1, int router2, vector<vector<int>> martix)
{
    int num = martix.size();
    for(int i = 0; i < num; i++){
        if(i == router1){           //router1本身
            continue;
        }

        if(martix[router2][i] > 0 && (martix[router2][i] != 99 && martix[router2][i] != 16)){
            if(martix[router1][i] > martix[router1][router2] + martix[router2][i]){
                martix[router1][i] = martix[router1][router2] + martix[router2][i];
                cout << "update router" << router1 << "'s table through router " << router2 << endl;
                nexthop[router1][i] = router2;
                totalchange++;
            }
        }
    }
    return martix;
}