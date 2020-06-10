import socket

if __name__ == "__main__":
    with socket.socket(socket.AF_INET) as s:
        s.bind(('127.0.0.1', 7788))
        s.listen(1)
        conn, addr = s.accept()
        i = 26
        while i > 0:
            buff = conn.recv(1024)
            i -= 1
