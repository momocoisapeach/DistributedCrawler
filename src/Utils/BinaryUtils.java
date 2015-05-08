package Utils;

import java.math.BigInteger;

// TODO: Auto-generated Javadoc
/**
 * The Class BinaryUtils.
 */
public class BinaryUtils {
	
	/**
	 * From decimal to big integer.
	 *
	 * @param hex the hex
	 * @return the big integer
	 */
	public static BigInteger fromDecimalToBigInteger(String hex) {
		return new BigInteger(hex);
	}
	
	/**
	 * convert from string representation of decimal bytes to byte[].
	 *
	 * @param hex the hex
	 * @return the byte[]
	 */
	public static byte[] fromDecimal(String hex) {
		return fromDecimalToBigInteger(hex).toByteArray();
	}

	/**
	 * From hex to big integer.
	 *
	 * @param hex the hex
	 * @return the big integer
	 */
	public static BigInteger fromHexToBigInteger(String hex) {
		return new BigInteger(hex, 16);
	}
	
	/**
	 * convert from string representation of hex bytes to byte[].
	 *
	 * @param hex the hex
	 * @return the byte[]
	 */
	public static byte[] fromHex(String hex) {
		return fromHexToBigInteger(hex).toByteArray();
	}
	
	/**
	 * Byte array to string.
	 *
	 * @param array the array
	 * @return the string
	 */
	public static String byteArrayToString(byte[] array) {
		StringBuilder sb = new StringBuilder();
		for(byte b : array) {
    		sb.append(b);
    	}
       return sb.toString();
	}
	
	/**
	 * Array equals.
	 *
	 * @param a the a
	 * @param b the b
	 * @return true, if successful
	 */
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
	
	/**
	 * get a bit from the int "b" with given position "pos".
	 *
	 * @param b the b
	 * @param pos the pos
	 * @return 0 or 1
	 */
	public static int getBit(int b, int pos) {
		if(pos <= 0 || pos > 32) {
			throw new IllegalArgumentException();
		}
		
		return (b >>> (pos - 1)) & 1;
	}
}
