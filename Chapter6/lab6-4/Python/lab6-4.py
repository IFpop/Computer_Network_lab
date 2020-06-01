#coding=utf-8

import configparser
import socket
from binascii import hexlify

def checksum(info):
    sum = 0
    for i in range(0,len(info),4):
        val = int(info[i:i+4],16)
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
    fakeheader = sourceip+destip+"0011"+udpsegment[8:12]
    udp_content = fakeheader + udpsegment[:12]+"0000"+udpsegment[16:]
    ans = checksum(udp_content)

    # 输出信息
    print("伪首部(16进制)：\n"+"source_ip: %s\ndest_ip: %s\nprotocol: %s\nUDP_length: %s\n" %(sourceip,destip,"0011",udpsegment[8:12]))
    print("UDP首部(16进制)：\n"+"source_port: %s\ndest_port: %s\nUDP_length: %s\nUDP_checksum: %s\n" %(udpsegment[:4],udpsegment[4:8],udpsegment[8:12],udpsegment[12:16]))

    print("计算后：",hex(ans))