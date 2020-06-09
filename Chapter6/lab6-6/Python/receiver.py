import socket

if __name__ == "__main__":
    with socket.socket(socket.AF_INET) as s:
        s.bind(('127.0.0.1', 9999))
        s.listen(1)
        conn, addr = s.accept()
        while True:
            buff = conn.recv(1024)
            print("receive: " + str(buff, encoding='utf-8'))
