package edu.upenn.cis455.servlet;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import edu.upenn.cis455.storage.Channel;
import edu.upenn.cis455.storage.DBWrapper;
import edu.upenn.cis455.storage.User;

/**
 * Servlet implementation class CreateChannelsServlet
 */
public class CreateChannelsServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		PrintWriter pw = response.getWriter();
		HttpSession session=request.getSession(false);
		if(session!=null){
		response.setContentType("text/html");
		String form = "<form action=\"CreateChannels\" method=\"post\">  "
				+ "Channel Name:<input type=\"text\" name=\"channelname\"/><br/><br/>"
//				+ "User Name:<input type=\"text\" name=\"username\"/><br/><br/>"
				+"XPaths(separated by \"return\"):<br>"
		        +"<textarea name=\"XPaths\" rows = \"10\" cols = \"30\"></textarea><br>"
		        +"Embedding link of XSL stylesheet:<br>"
		        +"<input name=\"xslt\"><br>"
				+ "<input type=\"submit\" value=\"create\"/></form> ";
		pw.println(form);
		pw.flush();
		}
		else{
			pw.print("<p>Please login first</p>");  
            request.getRequestDispatcher("Login").include(request, response);  
		}
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		PrintWriter out= response.getWriter();
		
		HttpSession session=request.getSession(false);
		if(session!=null){
			String userName=(String)session.getAttribute("name");
	        String chnlName = request.getParameter("channelname");
	        String xpath = request.getParameter("XPaths");
	        String xslt = request.getParameter("xslt");
//	        String url = request.getParameter("fileUrl");
	        String[] xpaths = xpath.split("\n");
//	        String[] urls = url.split(";");
			String directory = getServletContext().getInitParameter("BDBstore");
			DBWrapper db = new DBWrapper(directory);
			
			Channel chnl = new Channel(chnlName);
			
			chnl.setUserName(userName);
			
			if(db.addChannel(chnl)){ 
				db.addChannelToUser(chnlName, userName);
				db.setXsltOfChannel(xslt, chnlName);
				for(int i = 0; i <xpaths.length; i++){
					if(xpaths[i].length() != 0){
						db.addXpath(chnlName, xpaths[i]);
					}
				}
				
//				for(int i = 0; i <urls.length; i++){
//					if(urls[i].length() != 0){
//						db.addUrlToChannel(urls[i], chnlName);
//					}
//				}
	            out.print("<p>done!</p>");  
	            db.closeEnv();
	            request.getRequestDispatcher("MyChannels").include(request, response); 
			}
			else{
	            out.println("<font color=blue>used name!</font><br><br>");
	            out.println("<form action=\"/HW2/CreateChannels\"><input type=\"submit\" value=\"try again?\"></form>");
	            out.println("<form action=\"/HW2/AllChannels\"><input type=\"submit\" value=\"View all the channels\"></form>");
			}
			
			
			db.closeEnv();
		}
		else{  
            out.print("<p>Please login first</p>");  
            request.getRequestDispatcher("Login").include(request, response);  
        }
	}

}
