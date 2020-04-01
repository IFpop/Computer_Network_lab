#coding=utf-8
#owner: IFpop
#time: 2020/3/17

import serial
import sys
import time
import threading

'''
serial class --> 将串口需要用到的函数包装到类中
'''
class Ser(object):
    def __init__(self,cfg:list):
        self.port = serial.Serial(port=cfg[0], baudrate=int(cfg[1]), bytesize=int(cfg[2]), parity=cfg[3],stopbits=int(cfg[4]), timeout=1)
        '''
        创建一个Serial对象，其初始化参数说明如下：
            port：设备名称
            baudrate：波特率
            bytesize：数据位数
            parity：奇偶校验控制
            stopbits：停止位数
            timeout：设置读取超时值
        所有参数均从配置文件中读取
        '''
    def send_data(self):
        data = input("Please input your message:")
        self.port.write((data+'\n').encode('utf-8'))
        '''
        Serial 对象的 write 方法，其向打开的串行端口中写入数据
        其传输的数据流以 Byte 为单位，故将 python 字符串的 Unicode encode 为 Byte 流
        '''
    def recv_data(self):
        while True:
            '''
            Serial 对象的 readline 方法，其从串行端口读取数据，遇到 '\n' 停止
            由于其读取的为字节流，故此处使用 decode 函数将其编码为 Unicode
            '''
            self.message = self.port.readline().decode('utf-8')
            self.message = self.message[:-1]
            if(self.message == ""):
                time.sleep(0.5)
            else:
                print("recv data:"+self.message)
                return

'''
主程序
'''
if __name__ == "__main__":
    # 打开配置文件
    cfg1 = [] # cfg[SerialPort,BaudRate,DataBits,Parity,stopBits]
    print("current config file: "+sys.argv[1])
    with open(sys.argv[1],'r') as f:
        line = f.readline().strip('\n')
        while line:
            # 输出配置文件，端口及属性
            print(line)
            cfg1.append((line.split(' ')[1]).replace('\n',''))
            line = f.readline().strip('\n')
    f.close()
    # print(cfg)
    print("Start connect...")
    a = Ser(cfg1)
    print('COM1------>COM2\n')
    if(int(sys.argv[2])):
        print("Send message")
    else:
        print("wait message")
    while True:
        if(int(sys.argv[2])):
            a.send_data()
        else:
            a.recv_data()