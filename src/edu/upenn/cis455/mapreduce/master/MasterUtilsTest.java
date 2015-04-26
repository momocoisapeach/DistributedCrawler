package edu.upenn.cis455.mapreduce.master;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class MasterUtilsTest {

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void testStatusPage() {
		System.out.println(MasterUtils.statusPage("Message"));
	}

}
