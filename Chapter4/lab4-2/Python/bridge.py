#coding=utf-8
import pcap
from scapy.all import *
from multiprocessing import Lock,Process
import threading
import time

class learnItem:
    mac_address = None
    index = -1

learnlist = []
devs = get_if_list()
lock=Lock()   #初始化一把锁
class Adapter:
    deviceId = -1
    
    # 初始化
    def __init__(self,_deviveid):
        self.deviceId = _deviveid
    
    # 抓包
    def captureFrame(self):
        # 开始抓包
        # print(devs[self.deviceId])
        pkt = pcap.pcap(devs[self.deviceId], promisc=True, immediate=True, timeout_ms=50)
        for ptime,pkt_data in pkt:
            # print("receive a packet")
            # 申请锁
            lock.acquire()
            print("networking adapter ",self.deviceId)
            dst_mac = "%.2x:%.2x:%.2x:%.2x:%.2x:%.2x"%(pkt_data[0], pkt_data[1], pkt_data[2], pkt_data[3], pkt_data[4], pkt_data[5])
            src_mac = "%.2x:%.2x:%.2x:%.2x:%.2x:%.2x"%(pkt_data[6], pkt_data[7], pkt_data[8], pkt_data[9], pkt_data[10], pkt_data[11])
            print("Destination: ",dst_mac)
            print("Source: ",src_mac)

            srcItem = learnItem()
            dstItem = learnItem()

            # 遍历学习表，看能否找到源地址与目的地址
            # 非空才进行寻找，否则直接进行逆学习
            if(len(learnlist) == 0):
                # 逆学习
                srcItem.index = self.deviceId
                srcItem.mac_address = src_mac
                learnlist.append(srcItem)
                print(">>learn table message update...")
                for item in learnlist:
                    print(item.mac_address,item.index)
            else:
                for item in learnlist:
                    # 寻找源地址
                    if(item.mac_address == src_mac):
                        srcItem.mac_address = item.mac_address
                        srcItem.index = item.index
                    # 寻找目标地址
                    if(item.mac_address == dst_mac):
                        dstItem.mac_address = item.mac_address
                        dstItem.mac_address = item.index
                if(srcItem.index == -1):
                    # 逆学习
                    print("逆学习")
                    srcItem.index = self.deviceId
                    srcItem.mac_address = src_mac
                    learnlist.append(srcItem)
                elif(srcItem.index == dstItem.index):
                    print("源端口和目的端口相同或MAC地址相同，丢弃信息")
                elif(dstItem.index == -1):
                    print("目的端口未知，泛洪处理")
                    self.SendFrame(-1,pkt_data)
                else:
                    print("目的端口已知: ",dstItem.index,", 进行转发。") 
                    self.SendFrame(dstItem.index,pkt_data)
            lock.release()
    def SendFrame(self,dst_index,packet_buffer):
        # 单播
        if(dst_index != -1):
            print(">>单播...")
            sendp(packet_buffer,iface=devs[dst_index])
        else:
            # 泛洪
            print(">>泛洪...")
            for item in devs:
                print(item)
                if(item != devs[self.deviceId]):
                    sendp(packet_buffer,iface=item)

if __name__ == "__main__":
    if(len(devs) == 0):
        print("无网卡信息")
    else:
        for i in range(len(devs)):
            print("index ",i,": ",devs[i])
        print("-------------------------------------------")
        device1 = input(">>请输入第一个捕获的设备号: ")
        device1 = int(device1)
        device2 = input(">>请输入第二个捕获的设备号: ")
        device2 = int(device2)

        Dev1 = Adapter(device1)
        Dev2 = Adapter(device2)

        # 准备捕获
        print(">>Start....")
        p1=Process(target=Dev1.captureFrame(),args=(1,lock))
        p2=Process(target=Dev2.captureFrame(),args=(2,lock))

        p1.start()
        p2.start()