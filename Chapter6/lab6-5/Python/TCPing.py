#/usr/bin/python3

from scapy.all import *

dst_ip = input("Input destination's IP: ")
dst_port = input("Input destination's port: ")

ip = IP(dst=dst_ip)
SYN = TCP(sport=33445, dport=int(dst_port), seq=100, flags="S")

print("Sending SYN=1 ACK=0")
SYNACK = sr1(ip/SYN)

print("Recieved SYN=1 ACK=1")
print(SYNACK)

