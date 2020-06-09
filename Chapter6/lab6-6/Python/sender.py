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
        s.bind(("127.0.0.1", 8888))
        s.connect(("127.0.0.1", 9999))
        round = 0
        while round < endRound:
            round += 1
            if round % triACKRound == 0:
                print("triple-ACK, ")

            print("Round {}\tthreshold {}".format(round, threshold))
            s.send(bytes(str(round), encoding='utf-8'))
