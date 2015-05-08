package edu.upenn.cis455.storage;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

// TODO: Auto-generated Javadoc
/**
 * The Class DBWrapperTest.
 */
public class DBWrapperTest {
	
	/** The directory. */
	String directory = "/Users/peach/Documents/cis555/testDB/";
	
	/** The db. */
	DBWrapper db;

	/**
	 * Sets the up.
	 *
	 * @throws Exception the exception
	 */
	@Before
	public void setUp() throws Exception {
		
		db = new DBWrapper(directory);
		
		User A = new User("A");
		A.setPassword("1234");
		db.addUser(A);
		
		Channel channel = new Channel("a channel");
		channel.setUserName("A");
		db.addChannel(channel);
		
		db.closeEnv();
	}

	/**
	 * Test has user.
	 */
	@Test
	public void testHasUser() {
		db = new DBWrapper(directory);
		assertEquals(db.hasUser("A"), true);
	}

	/**
	 * Test has channel.
	 */
	@Test
	public void testHasChannel() {
		db = new DBWrapper(directory);
		assertEquals(db.hasChannel("a channel"), true);
	}

	/**
	 * Test get channel owner.
	 */
	@Test
	public void testGetChannelOwner() {
		db = new DBWrapper(directory);
		assertEquals(db.getChannelOwner("a channel"), "A");
	}

}
