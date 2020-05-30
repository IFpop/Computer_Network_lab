import socket
BUFSIZE = 1024
ip_port = ('127.0.0.1', 9999)
server = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)  # udp协议
server.bind(ip_port)

while True:
    data,client_addr = server.recvfrom(BUFSIZE)
    print('recv_data_from_client', data)

    data = data.upper()
    print("send_data_to_client:",data)
    server.sendto(data,client_addr)
 
server.close()