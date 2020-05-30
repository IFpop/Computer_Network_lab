import socket
BUFSIZE = 1024
client = socket.socket(socket.AF_INET,socket.SOCK_DGRAM)

addr = input("请输入address: ").strip()
port = input("请输入port: ").strip()
port = int(port)

while True:
    msg = input(">> ").strip()
    ip_port = (addr, port)

    print("user input:",msg);
    client.sendto(msg.encode('utf-8'),ip_port)

 
    data,server_addr = client.recvfrom(BUFSIZE)
    print('recv from server',data.decode('utf-8'))
 
client.close()