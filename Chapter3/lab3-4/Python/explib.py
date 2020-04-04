import struct, crcmod, socket, threading, time, copy



class crc_ccitt :

    def __init__(self, raw=b'', packed=b'') :
        if raw == b'' :
            self.packed = packed
            self.raw = packed[:-2]
            self.crc_code = packed[-2:]
        elif packed == b'' :
            self.raw = raw
            packer = crcmod.predefined.Crc('crc-ccitt-false')
            packer.update(raw)
            self.crc_code = packer.crcValue.to_bytes(2, byteorder='little')
            self.packed = self.raw + self.crc_code

    def check_crc(self) :
        packer = crcmod.predefined.Crc('crc-ccitt-false')
        packer.update(self.raw)
        crc_code = packer.crcValue.to_bytes(2, byteorder='little')
        return self.crc_code == crc_code



class data_frame :

    def __init__(self, seq=0, msg=b'', crc=b'', state=b'wait') :
        self.seq = seq
        self.msg = msg
        self.crc = crc
        self.state = state
    '''
        seq:    4 Bytes
        msg:    1024 Bytes
        crc:    2 Bytes
        state:  4 Bytes         For sender use only
            b'wait'
            b'sent'
            b'ackd'
    '''

    def pack(self) :
        return struct.pack('<I', self.seq) + self.msg + self.crc + self.state

    def unpack(self, raw_frame) : 
        self.seq = struct.unpack('<I', raw_frame[0:4])[0]
        self.msg = raw_frame[4:-6]
        self.crc = raw_frame[-6:-4]
        self.state = raw_frame[-4:]



class file_frames :

    def __init__(self, file) :
        self.frame_num = -1
        self.frame_list = []
        while True:
            raw_frame = file.read(1024)
            if len(raw_frame) == 0: break
            self.frame_num += 1
            frame_crc = crc_ccitt(raw = raw_frame).crc_code
            frame = data_frame(seq=self.frame_num, msg=raw_frame, crc=frame_crc)
            self.frame_list.append(frame)
