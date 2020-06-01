from socket import *
BUFSIZE = 1024
client = socket(AF_INET,SOCK_STREAM)

addr = input("请输入address: ").strip()
port = input("请输入port: ").strip()
port = int(port)
ip_port = (addr, port)

client.connect(ip_port)

while True:
    msg = input(">> ").strip()

    print("user input:",msg);
    client.send(msg.encode('utf-8'))
 
    data  = client.recv(BUFSIZE)
    print('recv from server',data.decode('utf-8'))
 
client.close()