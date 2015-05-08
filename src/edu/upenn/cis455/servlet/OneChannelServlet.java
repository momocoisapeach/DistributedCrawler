package edu.upenn.cis455.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import edu.upenn.cis455.storage.DBWrapper;

// TODO: Auto-generated Javadoc
/**
 * Servlet implementation class OneChannelServlet.
 */

public class OneChannelServlet extends HttpServlet {
	
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;

	/**
	 * Do get.
	 *
	 * @param request the request
	 * @param response the response
	 * @throws ServletException the servlet exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String channelName = request.getParameter("key");
		String directory = getServletContext().getInitParameter("BDBstore");
		DBWrapper db = new DBWrapper(directory);
		PrintWriter out= response.getWriter();
		if(channelName != null && db.hasChannel(channelName)){
			out.println("<p>channel name:"+channelName+"</p>");
			out.println("<p>it belongs to: "+db.getChannelOwner(channelName)+"</p>");
			String delete_form = "<form action=\"DeleteChannel\" method=\"post\">  "
					+ "<input type=\"hidden\" name=\"channel\" value=\""
					+ channelName
					+ "\" />"
					+ "<input type=\"submit\" value=\"delete\"/></form> ";
			
			out.println(delete_form);
//			out.println("<p><a href = \"/HW2/ChannelDisplay?channel="+channelName
//					+"\">"+"Display this channel!"+"</a></p>");
			
			String display_form = "<form action=\"ChannelDisplay\" method=\"post\">  "
					+ "<input type=\"hidden\" name=\"channel\" value=\""
					+ channelName
					+ "\" />"
					+ "<input type=\"submit\" value=\"display this channel!\"/></form> ";
			
			out.println(display_form);
			
			out.println("<p>xpaths:<br>");
			ArrayList<String> xpaths = db.getXpaths(channelName);
			for(int i = 0; i <xpaths.size(); i ++){
				out.println(xpaths.get(i)+"<br>");
			}
			out.println("</p><p>xslt url is "+db.getXslt(channelName)+"</p>");
			ArrayList<String> matchedUrls = db.getMatchedUrls(channelName);
			String html = null;
			for(int i = 0; i <matchedUrls.size(); i++){
				html += "<li><a href = \"/HW2/DeleteURL?url="
			+matchedUrls.get(i)+"&channel="+channelName
						+"\">"+matchedUrls.get(i)+"</a></li>";
			}
			out.println(html);
			out.flush();
		}
		else{
			
            out.println("<font color=red>No Such Channel!</font><br><br>");
            out.println("<form action=\"/HW2/Login\"><input type=\"submit\" value=\"Log in\"></form>");
            out.println("<form action=\"/HW2/Signup\"><input type=\"submit\" value=\"Sign up\"></form>");
		}
		db.closeEnv();
	}

	/**
	 * Do post.
	 *
	 * @param request the request
	 * @param response the response
	 * @throws ServletException the servlet exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}

}
