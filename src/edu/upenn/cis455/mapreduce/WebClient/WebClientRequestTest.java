package edu.upenn.cis455.mapreduce.WebClient;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.Writer;

import org.junit.Before;
import org.junit.Test;

// TODO: Auto-generated Javadoc
/**
 * The Class WebClientRequestTest.
 */
public class WebClientRequestTest {

	/**
	 * Sets the up.
	 *
	 * @throws Exception the exception
	 */
	@Before
	public void setUp() throws Exception {
	}

	/**
	 * Test get full request.
	 *
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	@Test
	public void testGetFullRequest() throws IOException {
		WebClientRequest request = WebClientRequest.getWebClientRequest("http://www.abc.com/runmap", "POST", "abc");
		Writer writer = request.getBodyWriter();
		writer.write("job=abc&input=def&numThreads=10&numWorkers=1&worker1=1.1.1.1:1111");
		System.out.println(request.getFullRequest());
	}

}
