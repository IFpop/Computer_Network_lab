#coding=utf-8

import configparser
import socket
from binascii import hexlify

def checksum(info):
    sum = 0
    for i in range(0,len(info),4):
        val = int(info[i:i+4],16)
        print(info[i:i+4])
        sum = sum + val
        sum = sum & 0xffffffff
    sum = (sum >> 16) + (sum & 0xffff)
    if sum > 0xffff:
        sum = (sum >> 16) + (sum & 0xffff)
    return 0xffff-sum

if __name__ == "__main__":
    # 从配置文件中读取信息
    config = configparser.ConfigParser()
    config.read("./lab6-4.cfg")
    udpsegment = config['DEFAULT']['udpsegment']
    sourceip = config['DEFAULT']['sourceip']
    destip = config['DEFAULT']['destip']

    sourceip = socket.inet_aton(sourceip)
    sourceip = hexlify(sourceip).decode('utf-8')

    destip = socket.inet_aton(destip)
    destip = hexlify(destip).decode('utf-8')

    # 构造伪首部+UDP 首部 sourceIP + DestIP + 00 + 17(8位协议) + UDP长度 + UDP首部
    packet = sourceip+destip+"0011"+"0242"+udpsegment[:12]+"0000"+udpsegment[16:];
    ans = checksum(packet)
    print(hex(ans))