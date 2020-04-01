class serial.Serial
    __init__(port=None, baudrate=9600, bytesize=EIGHTBITS, parity=PARITY_NONE, stopbits=STOPBITS_ONE, timeout=None,
     xonxoff=False, rtscts=False, write_timeout=None, dsrdtr=False, inter_byte_timeout=None)

Parameters:	
port – 设备名称或无
baudrate (int) – 波特率，例如9600或115200等
bytesize – 数据位数。可能的值： 5，6，7， 8
parity – 启用奇偶校验。可能的值： PARITY_NONE，PARITY_EVEN，PARITY_ODD PARITY_MARK，PARITY_SPACE
stopbits – 停止位数。可能的值:STOPBITS_ONE, STOPBITS_ONE_POINT_FIVE, STOPBITS_TWO
timeout (float) – 设置读取超时值
xonxoff (bool) – 启用软件流控制
rtscts (bool) – 启用硬件（RTS / CTS）流控制
dsrdtr (bool) – 启用硬件（DSR / DTR）流控制
write_timeout (float) – 设置写超时值。
inter_byte_timeout (float) – 字符间超时，“ 无”禁用（默认）
Raises:	
ValueError – 当参数超出范围时（例如波特率，数据位），将引发此错误。

write(data)
Parameters:	data (bytes) – data to write
Thread safe writing (uses lock).

'''
- 介绍：
    Serial 对象的 write 方法，其向打开的串行端口中写入数据
    其传输的数据流以 Byte 为单位，故将 python 字符串的 Unicode encode 为 Byte 流
- 用法：
    serial.Serial.wirte((data+'\n').encode('utf-8'))
'''

'''
- 介绍：
    Serial 对象的 readline 方法，其从串行端口读取数据，遇到 '\n' 停止
    由于其读取的为字节流，故此处使用 decode 函数将其编码为 Unicode
- 用法：
    serial.Serial.readline().decode('utf-8')
'''

python Ser.py 1.cfg 1  # 1.cfg是配置文件名,1代表是发送方
python Ser.py 2.cfg 2  # 2.cfg是配置文件名,0代表是接收方
