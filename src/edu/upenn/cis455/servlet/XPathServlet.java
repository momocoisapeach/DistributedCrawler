package edu.upenn.cis455.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

import javax.net.ssl.HttpsURLConnection;
import javax.servlet.http.*;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import edu.upenn.cis455.crawler.HttpClient;
import edu.upenn.cis455.xpathengine.XPathEngineFactory;
import edu.upenn.cis455.xpathengine.XPathEngineImpl;

// TODO: Auto-generated Javadoc
/**
 * The Class XPathServlet.
 */
@SuppressWarnings("serial")
public class XPathServlet extends HttpServlet {
	
	/* TODO: Implement user interface for XPath engine here */
	
	/* You may want to override one or both of the following methods */

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServlet#doPost(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
		HttpSession sess = request.getSession();
//		System.out.println("IN XpathSERV DOPOST");
		String xpath = request.getParameter("XPath");
		String url = request.getParameter("URL");
//		String xpath = "/note";
//		String url = "http://www.w3schools.com/xml/note.xml";
		Document doc = null;
		int statusCode = 0;
	
		HttpClient client = new HttpClient(url);
		client.setRequest("GET");
	    try {
	      // Execute the method.
	      statusCode = client.executeMethod();

	      // Read the response body.
	      if(statusCode == 200){
		      doc = client.getResponseBody();
	      }
	    } finally {
	      // Release the connection only if it is a supported url
	    	if(statusCode != 1000){
	    		client.releaseConnection();
	    	}
	    }  
	    response.setContentType("text/html");
	    PrintWriter out = response.getWriter();
//	    if(statusCode != 200){
//	    	if(statusCode == 1000) statusCode = 404;
//	    	response.sendError(statusCode);
//	    }
	    
	    if(statusCode != 200){
	    	out.println("<html><body><h2>ERROR</h2>"
	    			+ "<br><p>you may"
	    			+ "<li>entered an invalid url</li>"
	    			+ "<li>or the resource you requested is not found</li>"
	    			+ "<li>or ....</li>"
	    			+ "</p>"
	    			+ "</body></html>");
	    	out.flush();
	    }
	    else if(statusCode == 200){
		    String[] xpaths = xpath.split("\r\n");
	//	    System.out.println("new xpath engine");
		    XPathEngineImpl x = (XPathEngineImpl) new XPathEngineFactory().getXPathEngine();
	//	    System.out.println("setting xpath");
		    x.setXPaths(xpaths);
		    boolean[] result = x.evaluate(doc);
//		    System.out.println(result[0]);
		    
			out.println("<html>");
			String head = "<head><style>table "
					+ "{width:70%;"
					+ "}"
					+ "th, td {"
					+ "padding: 5px;"
					+ "text-align: left;"
					+ "}"
					+ "table tr:nth-child(even) {"
					+ "background-color: #eee;"
					+ "}"
					+ "table tr:nth-child(odd) {"
					+ "background-color:#fff;"
					+ "}"
					+ "</style>"
					+ "</head>";
			out.println(head);
	        out.println("<body>");
	        out.println("<h2>Your Result:</h2><br>");
	        String body = "<br>"
	        		+ "<table>"
	        		+ "<tr>"
	        		+ "<th>XPath</th>"
	        		+ "<th>Match?</th>	"+ "</tr>";
	        out.println(body);
	        for(int i = 0; i < xpaths.length; i++){
	        	out.println("<tr><td>"+xpaths[i]+"</td>");
		    	out.print("<td>"+result[i]+"</td></tr>");
		    }
	        body = "</table></body>";
	        out.println("</html>");		
		    
		    out.flush();
	    }
		
		
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
		HttpSession sess = request.getSession();
//		System.out.println("IN DOGET");
		response.setContentType("text/html");
		PrintWriter out = response.getWriter();
   		    out.println("<html>");
	        out.println("<body>");
	        out.println("<form method=\"post\" action=\"XPathServlet\">");
	        
	        out.println("URL:<br>");
	        out.println("<input name=\"URL\"><br>");
	        // border=\"2\" width=\"50%\"
	        out.println("XPaths:<br>");
	        String html  = "<textarea name=\"XPath\" rows = \"10\" cols = \"30\"></textarea><br>";
	        out.println(html);
	        out.println("<input type=\"submit\" name=\"Compare\" value=\"Compare\">");
	        
	        out.println("</form>");
	        out.println("</body>");
	        out.println("</html>");		
	}

}









