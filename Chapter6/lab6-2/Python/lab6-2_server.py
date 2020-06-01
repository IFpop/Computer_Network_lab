from socket import *
BUFSIZE = 1024

server = socket(AF_INET, SOCK_STREAM)  # tcp协议

ip_port = ('127.0.0.1', 9999)
server.bind(ip_port)
server.listen(5)

while True:
    tcpClient,addr = server.accept()
    print('[+]...connected from:',addr)

    while True:
        data =  tcpClient.recv(BUFSIZE)
        print('server recv from clinet: ', data)
        # 变为大写
        data = data.upper()
        print('server send to clinet: ', data)
        tcpClient.send(data)
    tcpClient.close()
 
server.close()