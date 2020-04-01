#coding=utf-8

import configparser
'''
程序运行屏幕输出要点：
首先显示待发送的数据信息二进制比特串
然后显示收发双方预定的生成多项式采用CRC-CCITT，对应的二进制比特串
计算循环冗余校验码CRC-Code
显示生成的CRC-Code，以及带校验和的发送帧

显示接收的数据信息二进制比特串，以及计算生成的CRC-Code
计算余数
显示余数，为零表示无错，不为零表示出错
'''

def Mod_two(info_string,crc_ccitt):
    # 模2除法
    list_string = list(info_string)
    for i in range(len(info_string)-len(crc_ccitt)+1):
        if list_string[i] != '1':
            continue
        for j in range(len(crc_ccitt)):
            # 生成多项式与r位info进行异或
            if crc_ccitt[j] == list_string[i+j]:
                # 向该位置添加0
                list_string[i+j] = '0'
            else:
                # 向该位置添加1
                list_string[i+j] = '1'
    # 获取crc code 
    info_string = ''.join(list_string)
    crc_code = info_string[(1-len(crc_ccitt)):]
    # print(crc_code)
    return crc_code

def cacl_crc_code(info_string,crc_ccitt):
    # 对发送帧添加0
    Send_string = info_string +'0'*(len(crc_ccitt)-1)
    # 计算crc code
    crc_code = Mod_two(Send_string,crc_ccitt)
    Send_string = info_string+ crc_code
    return crc_code,Send_string

def Crc_check(info_string,crc_ccitt):
    # 获取余数
    Remainer = Mod_two(info_string,crc_ccitt)
    # 判断是否余数为0
    if( Remainer.count('0') == (len(crc_ccitt)-1) ):
        return True,Remainer,info_string[:(len(info_string)-len(crc_ccitt)+1)]
    else:
        return False,Remainer,info_string[:(len(info_string)-len(crc_ccitt)+1)]

if __name__ == "__main__":
    # 读取配置文件
    config = configparser.ConfigParser()
    config.read("./temp.cfg")
    InfoString1 = config['DEFAULT']['InfoString1'].replace(' ','')
    CRC_CCITT  = config['DEFAULT']['GenXString'].replace(' ','')
    InfoString2 = config['DEFAULT']['InfoString2'].replace(' ','')
    # data = int(InfoString1)
    # print(data)
    # print(InfoString1)
    # print(CRC-CCITT)
    # print(InfoString2)
    print("待发送的数据信息二进制比特串: "+InfoString1)
    print("预定的生成多项式对应的二进制比特串: "+CRC_CCITT+"\n")

    # 发送信息生成
    CRC_CODE1, Send_String = cacl_crc_code(InfoString1,CRC_CCITT)
    print("计算得到的CRC校验码："+CRC_CODE1)
    print("带校验和的发送帧："+Send_String+"\n")

    # 接收信息校验
    CRC_CODE2, Send_String = cacl_crc_code(InfoString2,CRC_CCITT)
    print("接收到的数据信息二进制比特串: "+InfoString2)
    print("计算得到的CRC校验码: "+CRC_CODE2+"\n")


    # 进行校验
    InfoString2+=CRC_CODE2
    Check_code,Remainer, Recv_string = Crc_check(InfoString2,CRC_CCITT)

    # 打印余数，以及校验正误
    print("余数为: ",Remainer)
    if(Check_code):
        print("正确")
    else:
        print("错误")

