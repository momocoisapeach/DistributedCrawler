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
 * Servlet implementation class DeleteChannelServlet.
 */
public class DeleteChannelServlet extends HttpServlet {
	
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
		String chnl = request.getParameter("channel");
		HttpSession session=request.getSession(false);
		String directory = getServletContext().getInitParameter("BDBstore");
		DBWrapper db = new DBWrapper(directory);
		PrintWriter out= response.getWriter();
		if(session!=null){
			String name=(String)session.getAttribute("name");  

			
			if(db.deleteChannel(chnl, name)){
				response.sendRedirect("/HW2/MyChannels");
			}
			out.println("deletion failed");
			out.println("<form action=\"/HW2/AllChannels\"><input type=\"submit\" value=\"Back\"></form>");
		}
		else{  
            out.print("<p>You are not the owner!</p>");  
            request.getRequestDispatcher("Login").include(request, response);  
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
