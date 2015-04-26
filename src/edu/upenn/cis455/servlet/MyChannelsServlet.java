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

/**
 * Servlet implementation class MyChannelsServlet
 */

public class MyChannelsServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		PrintWriter pw = response.getWriter();
		HttpSession session=request.getSession(false);  
		String directory = getServletContext().getInitParameter("BDBstore");
		DBWrapper db = new DBWrapper(directory);
		
        if(session!=null){  
	        String name=(String)session.getAttribute("name");  
	        ArrayList<Channel> myChannels = db.allMyChannels(name);
	        System.out.println("channel size is"+myChannels.size());
	        String css = "<html><head><style>table {width:70%;}"
	        		+ "table, th, td {"
	        		+ "border: 1px solid black;"
	        		+ "border-collapse: collapse;"
	        		+ "}"
	        		+ "th, td {"
	        		+ "padding: 5px;"
	        		+ "text-align: left;"
	        		+ "}"
	        		+ "table#t01 tr:nth-child(even) {"
	        		+ "background-color: #eee;"
	        		+ "}"
	        		+ "table#t01 tr:nth-child(odd) {"
	        		+ "background-color:#fff;"
	        		+ "}"
	        		+ "</style>"
	        		+ "</head>";
//	        pw.println(css);
	        pw.println("<body><h2>Hello, "+name+" Welcome to Your Control Panel!</h2>"); 
	        pw.println("<form action=\"/HW2/Logout\"><input type=\"submit\" value=\"Log out\"></form>");
	        pw.println("<form action=\"/HW2/AllChannels\"><input type=\"submit\" value=\"Back\"></form>");
	        pw.println("<form action=\"/HW2/CreateChannels\"><input type=\"submit\" value=\"Create a new Channel\"></form>");
//	        String channels = "";
//	        for(int i = 0; i < myChannels.size(); i++){
//	        	channels += "<li><a href = \"/HW2/OneChannel?key="
//	        			+myChannels.get(i).getName()+"\">"+myChannels.get(i).getName()+"</a></li>";
//	        }
	        StringBuilder table = new StringBuilder("<table id=\"t01\">"
	        		+ "<tr>"
	        		+ "<th>Channel Name</th>"
	        		+ "<th>Display</th>		"
	        		+"<th>Delete</th>"
	        		+ "</tr>");
	        for(int i = 0; i < myChannels.size(); i++){
	        	String name1 = myChannels.get(i).getName();
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
	        pw.println(table.toString());
//	        pw.println(channels);
	        
	        
        }  
        else{  
            pw.print("<p>Please login first</p>");  
            request.getRequestDispatcher("Login").include(request, response);  
        }
        db.closeEnv();
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}

}
