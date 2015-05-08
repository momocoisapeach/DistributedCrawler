package edu.upenn.cis455.mapreduce.master;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

// TODO: Auto-generated Javadoc
/**
 * The Class MasterUtilsTest.
 */
public class MasterUtilsTest {

	/**
	 * Sets the up.
	 *
	 * @throws Exception the exception
	 */
	@Before
	public void setUp() throws Exception {
	}

	/**
	 * Test status page.
	 */
	@Test
	public void testStatusPage() {
		System.out.println(MasterUtils.statusPage("Message"));
	}

}
