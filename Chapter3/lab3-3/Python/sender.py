import socket, struct
from crc_ccitt import *

FILE_PATH = "./test.png"
FilterError = 10
FilterLost = 10
RECV = ('localhost', 8888) 
SEND = ('localhost', 8880) 


if __name__ == "__main__":

    file = open(FILE_PATH, 'rb')
    sock = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
    sock.settimeout(2)
    sock.bind(SEND)

    count = 0
    index = 1
    data_raw = file.read(1024)
    while True : 
        count += 1
        if len(data_raw) == 0 :
            sock.sendto(b'eof', RECV)
            break
        data_frame = crc_ccitt(raw = data_raw)
        if count % FilterError == 3 :
            # Error
            data_frame.packed += bytes().fromhex('deadbeef')
            print("Sending data frame, index: " + str(index))
            sock.sendto(struct.pack('<I', index) + data_frame.packed, RECV)
        elif count % FilterLost == 7 :
            # Lost
            print("Sending data frame, index: " + str(index))
            pass
        else:
            # Normal
            print("Sending data frame, index: " + str(index))
            sock.sendto(struct.pack('<I', index) + data_frame.packed, RECV)
            
        while True:
            try:
                ack = sock.recv(8)
                if ack[4:] == b'erro':
                    print("Acknowledge frame " + str(struct.unpack('<I', ack[:4])[0]) + " says: ")
                    print("Data is corrupted, next frame to send:     ")
                    print(data_raw[:16], end = '')
                    print(" ......\n")
                    break
                elif ack[4:] == b'lost':
                    print("Acknowledge frame " + str(struct.unpack('<I', ack[:4])[0]) + " says: ")
                    print("Data lost, next frame to send:     ")
                    print(data_raw[:16], end = '')
                    print(" ......\n")
                    break
                elif ack[4:] == b'cmpl':
                    print("Acknowledge frame " + str(struct.unpack('<I', ack[:4])[0]) + " says: ")
                    data_raw = file.read(1024)
                    print("Complete, next frame to send:     ")
                    print(data_raw[:16], end = '')
                    print(" ......\n")
                    index += 1
                    break
            except socket.timeout:
                print("Receive timeout, retrying ......")

    print("Done!")
    file.close()
    sock.close()
