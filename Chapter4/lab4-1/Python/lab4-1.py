#!/usr/bin/python

from scapy.all import *

print(repr(conf.route))

print("====== capturing data packages on wlp4s0 ======")

dpkt = sniff(iface = "wlp4s0", count = 10)

print("====== capturing data packages finished ======")

# wrpcap("demo.pcap", dpkt)

for i in range(10):
    if dpkt[i][IP].proto == 6 : # is TCP

        print("print Ether")
        ls(dpkt[i][Ether])

        print("print IP")
        ls(dpkt[i][IP])

        print("print TCP")
        ls(dpkt[i][TCP])

        # only if TCP package does have Raw field 
        print("print HTTP")
        ls(dpkt[i][Raw].load)

        break;
