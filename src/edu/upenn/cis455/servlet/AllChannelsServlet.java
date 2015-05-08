package edu.upenn.cis455.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import edu.upenn.cis455.storage.Channel;
import edu.upenn.cis455.storage.DBWrapper;

// TODO: Auto-generated Javadoc
/**
 * Servlet implementation class AllChannelsServlet.
 */

public class AllChannelsServlet extends HttpServlet {
	
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
		PrintWriter pw = response.getWriter();
		String directory = getServletContext().getInitParameter("BDBstore");
		DBWrapper db = new DBWrapper(directory);
		HttpSession session=request.getSession(false);
		
		ArrayList<Channel> channels = db.getAllChannels();
//		System.out.println("if rss exists "+channels.get(0).getName().equals("rss"));
		pw.println("<html><body><h2>All Channels</h2>"
	    			+ "<br><br>");
		if(session==null){
			pw.println("<form action=\"/HW2/Login\"><input type=\"submit\" value=\"Log in\"></form>");
		}
		else{
			pw.println("<form action=\"/HW2/Logout\"><input type=\"submit\" value=\"Log out\"></form>");
			pw.println("<form action=\"/HW2/MyChannels\"><input type=\"submit\" value=\"View My Channels\"></form>");
		}
		pw.println("<form action=\"/HW2/Signup\"><input type=\"submit\" value=\"Sign up\"></form>");

//		String html = "";
//		for(int i = 0; i <channels.size(); i++){
//			html += "<li><a href = \"/HW2/OneChannel?key="
//		+channels.get(i).getName()+"\">"+channels.get(i).getName()+"</a></li>";
//		}
//		html += "<br><br>"
//				+ "</body></html>";
//		
        StringBuilder table = new StringBuilder("<table id=\"t01\">"
        		+ "<tr>"
        		+ "<th>Channel Name</th>"
        		+ "<th>Display</th>		"
        		+"<th>Delete</th>"
        		+ "</tr>");
        for(int i = 0; i < channels.size(); i++){
        	String name1 = channels.get(i).getName();
        	table.append("<tr><td>"
        			+ name1
        			+ "</td>");
        	table.append("<td><form action=\"ChannelDisplay\" method=\"post\">  "
					+ "<input type=\"hidden\" name=\"channel\" value=\""
					+ name1
					+ "\" />"
					+ "<input type=\"submit\" value=\"display this channel!\"/></form></td> ");
        	table.append("<td><form action=\"DeleteChannel\" method=\"post\">  "
				+ "<input type=\"hidden\" name=\"channel\" value=\""
				+ name1
				+ "\" />"
				+ "<input type=\"submit\" value=\"delete\"/></form></td>");			
        	table.append("</tr>");
        	
        }        
        table.append("</table></body></html>");
		
		
		
		
		pw.println(table);
		pw.flush();
		
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
