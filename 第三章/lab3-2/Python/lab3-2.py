#coding=utf-8

import configparser
import sys

def Send_message(FlagString,InfoString,Flag):
    temp_str = FlagString
    # 标志位代表bit传输
    if(Flag == 0):
        # 遍历所有Infostring中所有的字符，每隔五个填0
        count = 0
        for i in range(0,len(InfoString)):
            if (InfoString[i] == '1'):
                count += 1
            else:
                count = 0
            
            # 连续5个1，后面填0
            if(count == 5):
                temp_str += InfoString[i]
                temp_str += '0'
                count = 0
            # 其他情况直接加在末尾
            else:
                temp_str += InfoString[i]
        # 最后要加上终止符
        temp_str += FlagString
        return temp_str
    # 字节传输
    else:
        for i in range(0,len(InfoString)):
            # 识别到标记位
            if(InfoString[i] == '7' and InfoString[i+1] == 'E'):
                temp_str += '1B7'
            elif(InfoString[i] == '1' and InfoString[i+1] == 'B'):
                temp_str += '1B1'
            else:
                temp_str += InfoString[i]
    
        temp_str += FlagString
        return temp_str


# 对接收的信息进行处理，获取其中信息
def Recv_message(recv_String,Flag):
    Message_String = ""
    # bit接收
    if(Flag == 0):
        # 通过find函数找到起始符和终止符位置
        start = recv_String.find('01111110', 0, len(recv_String))
        end = recv_String.find('01111110', start + 1, len(recv_String))
        # 获取中间信息串
        temp_str = recv_String[start+8:end]
        count = 0
        tag = 0
        # 没找五个连续1则去掉后面的0
        for i in range(0,len(temp_str)):
            if(temp_str[i] == '1'):
                count += 1
            else:
                count = 0
            
            if(count == 5):
                Message_String += temp_str[tag:i+1]
                count = 0
                tag = i + 2
        Message_String += temp_str[tag:len(temp_str)]
    else:
        tag = 0
        temp_str = recv_String[2:len(recv_String)-2]
        
        # 遍历求解
        for i in range(0,len(temp_str)):
            if(temp_str[i:i+4] == '1B7E'):
                # print(temp_str[tag:i])
                Message_String += temp_str[tag:i]
                Message_String += '7E'
                tag  = i+4
            if(temp_str[i:i+4] == '1B1B'):
                Message_String += temp_str[tag:i]
                # print(temp_str[tag:i])
                Message_String += '1B'
                tag = i+4
        Message_String += temp_str[tag:len(temp_str)]
    return Message_String





if __name__ == "__main__":
    # 从配置文件中读取信息
    config = configparser.ConfigParser()
    if(int(sys.argv[1]) == 0):
        print("当前为零比特填充")
        config.read("./lab3-2_bit.cfg")
    else:
        print("当前为字节填充")
        config.read("./lab3-2_byte.cfg")
    InfoString = config['DEFAULT']['InfoString'].replace(' ','')
    FlagString = config['DEFAULT']['FlagString'].replace(' ','')
    print("帧起始标志:"+FlagString)
    print("帧结束标志:"+FlagString)
    print("帧数据信息:"+InfoString+'\n')

    Send_String = Send_message(FlagString,InfoString,int(sys.argv[1]))
    print("发送帧信息"+Send_String+'\n')
    
    Message = Recv_message(Send_String,int(sys.argv[1]))
    print("接受帧信息:"+Message+'\n')
    if(Message == InfoString):
        print("传输成功")
    else:
        print("传输有误")