package network.chapter3.lab3;

import java.util.Arrays;
import java.math.*;

public class CRC_CCITT{

	public static String getCRC16CCITT(String sendInfo, int polynomial, int crc) {
		Long sendLong=Long.parseLong(sendInfo, 2);
		String inputstr=Long.toHexString(sendLong);
		int strLen = inputstr.length();
		int[] intArray;
		if (strLen % 2 != 0) {
			inputstr = inputstr.substring(0, strLen - 1) + "0" + inputstr.substring(strLen - 1, strLen);
			strLen++;
		}
		intArray = new int[strLen / 2];
		int ctr = 0;
		for (int n = 0; n < strLen; n += 2) {
			intArray[ctr] = Integer.valueOf(inputstr.substring(n, n + 2), 16);
			ctr++;
		}
		for (int b : intArray) {
			for (int i = 0; i < 8; i++) {
				boolean bit = ((b >> (7 - i) & 1) == 1);
				boolean c15 = ((crc >> 15 & 1) == 1);
				crc <<= 1;
				if (c15 ^ bit)
					crc ^= polynomial;
			}
		}
		crc &= 0xFFFF;
		String crcStr = Integer.toHexString(crc).toUpperCase();
		int n = crcStr.length();
		for (int i = 0; i < (4 - n); i++) {
			crcStr = "0" + crcStr;
		}
		byte[] bytes=parseHexStr2Byte(crcStr);
		crcStr=byteArrToBinStr(bytes);
		return crcStr;
	}

	public static byte[] parseHexStr2Byte(String hexStr) {
		if (hexStr.length() < 1)
			return null;
		byte[] result = new byte[hexStr.length() / 2];
		for (int i = 0; i < hexStr.length() / 2; i++) {
			int high = Integer.parseInt(hexStr.substring(i * 2, i * 2 + 1), 16);
			int low = Integer.parseInt(hexStr.substring(i * 2 + 1, i * 2 + 2), 16);
			result[i] = (byte) (high * 16 + low);
		}
		return result;
	}
	public static String byteArrToBinStr(byte[] b) {
		StringBuffer result = new StringBuffer();
		for (int i = 0; i < b.length; i++) {
			String tmp = Long.toString(b[i] & 0xff, 2);

			while (tmp.length() < 8) {
				tmp = "0" + tmp;
			}
			result.append(tmp);
		}
		return result.toString();
	}

}
