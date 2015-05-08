package edu.upenn.cis455.servlet;

import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Test;

import static org.mockito.Mockito.*;

// TODO: Auto-generated Javadoc
/**
 * The Class XPathServletTest.
 */
public class XPathServletTest {

	/**
	 * Test do post http servlet request http servlet response.
	 *
	 * @throws Exception the exception
	 */
	@Test
	public void testDoPostHttpServletRequestHttpServletResponse() throws Exception {
		HttpServletRequest request = mock(HttpServletRequest.class);
		HttpServletResponse response = mock(HttpServletResponse.class);
		when(request.getParameter("URL")).thenReturn("http://www.w3schools.com/xml/note.xml");
		when(request.getParameter("XPath")).thenReturn("/note");
		
		PrintWriter pw = new PrintWriter(new ByteArrayOutputStream());
		
		when(response.getWriter()).thenReturn(pw);
		new XPathServlet().doPost(request, response);
		assert(response.getContentType().startsWith("text/html"));
	}

//	@Test
//	public void testDoGetHttpServletRequestHttpServletResponse() {
//		fail("Not yet implemented");
//	}

}
