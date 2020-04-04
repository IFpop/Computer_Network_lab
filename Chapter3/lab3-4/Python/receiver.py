from explib import *

FILE_PATH = "./test_recv.png"
RECV = ('localhost', 8888)
SEND = ('localhost', 8880)



if __name__ == '__main__' :

    # file
    file = open(FILE_PATH, 'wb')

    # sock
    sock = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
    sock.settimeout(2)
    sock.bind(RECV)

    # file data
    data = b''
    
    # count
    count = 0

    while True :
        
        try :
            raw_frame = sock.recv(1034)
            frame = data_frame(); frame.unpack(raw_frame)
            frame_crc = crc_ccitt(packed=frame.msg+frame.crc)

            print("frame_expected : " + str(frame.seq) ) 
 
            if frame.state == b'cmpl' and frame.seq == count :
                print("next_frame_to_send : ", end='')
                print(b'cmpl\x00\x00\x00\x00')
                sock.sendto(b'cmpl\x00\x00\x00\x00', SEND)
                break
            elif frame.seq != count :
                print("Data lost") 
                print("next_frame_to_send : ", end='')
                print(b'nack' + struct.pack('<I', count))
                sock.sendto(b'nack' + struct.pack('<I', count), SEND)
            elif frame_crc.check_crc() == False :
                print("Crc check failed") 
                print("next_frame_to_send : ", end='')
                print(b'nack' + struct.pack('<I', count))
                sock.sendto(b'nack' + struct.pack('<I', count), SEND)
            else :
                print("Accept") 
                print("Ack count : " + str(count))
                print("next_frame_to_send : ", end='')
                print(b'ackd' + struct.pack('<I', count))
                sock.sendto(b'ackd' + struct.pack('<I', count), SEND)
                data += frame.msg
                count += 1
                    
        except socket.timeout :
            print("Receive data frame timeout, retrying ......\n")

    file.write(data)
    print("Done!")
    file.close()
    sock.close()
