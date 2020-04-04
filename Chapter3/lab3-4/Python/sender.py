from explib import *

FILE_PATH = "./test.png"
FilterError = 10
FilterLost = 10
RECV = ('localhost', 8888)
SEND = ('localhost', 8880)
WIN_CAP = 5

    
# file_frames
with open(FILE_PATH, 'rb') as file:
    file_frames = file_frames(file)
file_frames.frame_num += 1
file_frames.frame_list.append(data_frame(seq=file_frames.frame_num, msg=b'A'*1024, crc=b'BB', state=b'cmpl'))

# sock
sock = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
sock.settimeout(2)
sock.bind(SEND)

# window & its lock
window = []
win_lock = threading.Lock()

# complete
complete = False

# fs_thread & wc_thread event
event = threading.Event()

def frame_send() :
    send_count = 0
    loop_count = 0

    while True:
        if complete == True :    return

        if loop_count % 3 == 0 :
                event.clear()
                event.wait()

        win_lock.acquire()
        for i in range( len(window) ) :

            if window[i].state == b'wait' :
                if send_count % FilterError == 7 :  # Error
                    error_frame = copy.deepcopy(window[i])
                    error_frame.crc = b'WA'
                    print("Simulating Error")
                    print("frame_expected : ", end='')
                    print(error_frame.pack()[0:16])
                    print("index : " + str(error_frame.seq))
                    print("next_frame_to_send : " + str(error_frame.seq + 1))
                    sock.sendto(error_frame.pack(), RECV)
                    window[i].state = b'sent' 
                    send_count += 1
                elif send_count % FilterLost == 3 : # Lost
                    print("Simulating Lost")
                    print("frame_expected : ", end='')
                    print(window[i].pack()[0:16])
                    print("index : " + str(window[i].seq))
                    print("next_frame_to_send : " + str(window[i].seq + 1))
                    window[i].state = b'sent'
                    send_count += 1
                else :                              # Normal
                    print("frame_expected : ", end='')
                    print(window[i].pack()[0:16])
                    print("index : " + str(window[i].seq))
                    print("next_frame_to_send : " + str(window[i].seq + 1))
                    sock.sendto(window[i].pack(), RECV)
                    window[i].state = b'sent' 
                    send_count += 1
            elif window[i].state == b'cmpl' :
                sock.sendto(window[i].pack(), RECV)

        win_lock.release()

        loop_count += 1


def window_control() :
    while True:
        if complete == True :   return

        win_lock.acquire()
        while len(window) < WIN_CAP and len(file_frames.frame_list) != 0 :
            window.append(file_frames.frame_list[0])
            del file_frames.frame_list[0]

        for frame in window : 
            if frame.state == b'ackd' :
                window.remove(frame)
        win_lock.release()

        event.set()


def acknowledge() :
    global complete

    while True :

        try :
            ack = sock.recv(8)

            win_lock.acquire()
            if ack[0:4] == b'ackd' :
                print("ack_excepted : ", end = '')
                print("ackd" + str(struct.unpack('<I', ack[4:8])[0]))
                print("ack_received : ", end = '')
                print(ack)
                for i in range( len(window) ) :
                    if window[i].seq == struct.unpack('<I', ack[4:8])[0] :
                        window[i].state = b'ackd'
                        break
            elif ack[0:4] == b'nack' :
                print("ack_excepted : ", end = '')
                print("ackd" + str(struct.unpack('<I', ack[4:8])[0]))
                print("ack_received : ", end = '')
                print(ack)
                for i in range( len(window) ) :
                    if window[i].seq >= struct.unpack('<I', ack[4:8])[0] and window[i].state != b'cmpl':
                        window[i].state = b'wait'
            elif ack[0:4] == b'cmpl' :
                complete = True
                win_lock.release()
                return
            win_lock.release()
                
        except socket.timeout :
            print("Receive ack timeout, retrying ......\n")



if __name__ == '__main__' :

    try :
        fs_thread, wc_thread, ac_thread = threading.Thread(target=frame_send, name='fs_thread'), threading.Thread(target=window_control, name='wc_thread'), threading.Thread(target=acknowledge, name='ac_thread')
    except:
        print("Thread initial failed!")

    fs_thread.start(); wc_thread.start(); ac_thread.start()

    fs_thread.join() 
    wc_thread.join() 
    ac_thread.join()

    print("Done!")
    sock.close()
