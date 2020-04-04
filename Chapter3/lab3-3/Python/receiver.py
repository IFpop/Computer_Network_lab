import socket, struct
from crc_ccitt import *

FILE_PATH = "./test_recv.png"
RECV = ('localhost', 8888) 
SEND = ('localhost', 8880) 


if __name__ == "__main__":

    file = open(FILE_PATH, 'wb')
    sock = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
    sock.settimeout(2)
    sock.bind(RECV)

    file_data = b''
    index = 1
    while True :
        try:
            data_recv = sock.recv(2048)
            if data_recv == b'eof':
                break;
            recv_index = struct.unpack('<I', data_recv[:4])[0]
            data_recv = data_recv[4:]
            data_frame = crc_ccitt(packed = data_recv)
            print("Frame excepted: " + str(recv_index))

            if data_frame.check_crc() == False:
                print("Crc check failed!\n")
                sock.sendto(struct.pack('<I', index) + b'erro', SEND)
                index += 1
            else:
                print("Crc verification succeeded!")
                print("Data frame number:" + str(recv_index) + "\n")
                file_data += data_frame.raw
                sock.sendto(struct.pack('<I', index) + b'cmpl', SEND)
                index += 1
        except socket.timeout:
            print("Receive timeout ......\n")
            sock.sendto(struct.pack('<I', index) + b'lost', SEND)
            index += 1
    
    file.write(file_data)
    print("Done!")
    file.close()
    sock.close()
