package Utils;

import java.math.BigInteger;

public class BinaryUtils {
	
	public static BigInteger fromDecimalToBigInteger(String hex) {
		return new BigInteger(hex);
	}
	
	/**
	 * convert from string representation of decimal bytes to byte[]
	 * @throws NumberFormatException
	 * @param hex
	 * @return
	 */
	public static byte[] fromDecimal(String hex) {
		return fromDecimalToBigInteger(hex).toByteArray();
	}

	public static BigInteger fromHexToBigInteger(String hex) {
		return new BigInteger(hex, 16);
	}
	
	/**
	 * convert from string representation of hex bytes to byte[]
	 * @throws NumberFormatException
	 * @param hex
	 * @return
	 */
	public static byte[] fromHex(String hex) {
		return fromHexToBigInteger(hex).toByteArray();
	}
	
	public static String byteArrayToString(byte[] array) {
		StringBuilder sb = new StringBuilder();
		for(byte b : array) {
    		sb.append(b);
    	}
       return sb.toString();
	}
	
	public static boolean arrayEquals(byte[] a, byte[] b) {
		if(a == b) {
			return true;
		} else if (a == null || b == null) {
			return false;
		} else if (a.length != b.length) {
			return false;
		} else {
			for(int i = 0; i < a.length; i++) {
				if(a[i] != b[i]) {
					return false;
				}
			}
			return true;
		}
	}
}
