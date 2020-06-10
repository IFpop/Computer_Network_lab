import configparser
import socket

if __name__ == "__main__":
    config = configparser.ConfigParser()
    config.read('./config.ini')
    mss = int(config["config"]["MSS"])
    print("MSS = {}".format(mss))
    threshold = int(config["config"]["Threshold"])
    print("Initial threshold = {}".format(threshold))
    triACKRound = int(config["config"]["TriACKRound"])
    timeoutRound = int(config["config"]["TimeoutRound"])
    endRound = int(config["config"]["EndRound"])
    
    conWindow = 1
    with socket.socket(socket.AF_INET) as s:
        s.bind(("127.0.0.1", 6677))
        s.connect(("127.0.0.1", 7788))
        round = 0
        flag = '*2'
        while round < endRound:
            round += 1

            if conWindow >= threshold:
                flag = '+1'

            if round % triACKRound == 0:
                print(" Triple ACK ")
                round -= 1
                print("sending: " + str(round))
                print("round: " + str(round))
                print("window size" + str(conWindow))
                s.send(bytes(str(round), encoding='utf-8'))
                round += 1
                threshold = conWindow / 2
                conWindow = threshold + 3
                continue
            
            if round % timeoutRound == 0:
                print(" Time out ")
                threshold = conWindow / 2
                conWindow = 1
                flag = '*2'

            print("sending: " + str(round))
            print("round: " + str(round))
            print("window size" + str(conWindow))
            s.send(bytes(str(round), encoding='utf-8'))

            if flag == '*2':
                conWindow *= 2
            elif flag == '+1':
                conWindow += 1

            
