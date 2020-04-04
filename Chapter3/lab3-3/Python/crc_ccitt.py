import crcmod

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
