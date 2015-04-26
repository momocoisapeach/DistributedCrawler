package edu.upenn.cis455.mapreduce.worker;

import static org.junit.Assert.*;

import java.math.BigInteger;

import org.junit.Before;
import org.junit.Test;

public class SHAUtilsTest {

	SHAUtils sha;
	@Before
	public void setUp() throws Exception {
		sha = new SHAUtils();
	}
	
	@Test
	public void testBigIntegerParseHex() {
		BigInteger i = new BigInteger("ffffffffffffffff", 16);
		assertEquals(i.toString(), "10");
	}

	@Test
	public void testHashToString() {
		assertEquals("86f7e437faa5a7fce15d1ddcb9eaeaea377667b8", sha.hashToString("a").toLowerCase());
	}

	@Test
	public void testBytesToString() {
		byte[] bytes = {1, 0xa};
		assertEquals("010a", SHAUtils.bytesToString(bytes).toLowerCase());
	}

	@Test
	public void testHashMod() {
		assertEquals(0, sha.hashMod("a", 4));
	}
	
	@Test
	public void testSplit() {
		byte[] bytes0 = {0, 0};
		byte[] bytes1 = {(byte) 0x3F, (byte) 0xFF}; //3FFF
		byte[] bytes2 = {(byte) 0x40, (byte) 0x00}; //40ff
		byte[] bytes3 = {(byte) 0xBF, (byte) 0xFF}; //BFFF
		byte[] bytes4 = {(byte) 0xFF, (byte) 0xFF}; //FFFF
		assertEquals(1, SHAUtils.split(bytes0, 4));
		assertEquals(1, SHAUtils.split(bytes1, 4));
		assertEquals(2, SHAUtils.split(bytes2, 4));
		assertEquals(3, SHAUtils.split(bytes3, 4));
		assertEquals(4, SHAUtils.split(bytes4, 4));
	}
	
	@Test
	public void testUnsigned() {
		byte[] bytes3 = {(byte) 0xBF, (byte) 0xFF}; //BFFF
		assertEquals(3, SHAUtils.unsigned(bytes3).length);
		assertEquals(0x0BFFF, new BigInteger(SHAUtils.unsigned(bytes3)).intValue());
	}

}
