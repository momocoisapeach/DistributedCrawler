package edu.upenn.cis455.servlet;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import edu.upenn.cis455.storage.DBWrapper;

// TODO: Auto-generated Javadoc
/**
 * Servlet implementation class LoginServlet.
 */

public class LoginServlet extends HttpServlet {
	
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;
       
    /**
     * Instantiates a new login servlet.
     *
     * @see HttpServlet#HttpServlet()
     */
    public LoginServlet() {
        super();
        // TODO Auto-generated constructor stub
    }

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
		response.setContentType("text/html");
		String form = "<form action=\"Login\" method=\"post\">  "
				+ "Name:<input type=\"text\" name=\"username\"/><br/><br/>"
				+ "Password:<input type=\"password\" name=\"userpass\"/><br/><br/>"
				+ "<input type=\"submit\" value=\"login\"/></form> ";
		form += "<a href=\"/HW2/Signup\">Don't have an account? Sign up!</a>";
		pw.println(form);
		pw.flush();
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
		//get request parameters for userID and password
        String user = request.getParameter("username");
        String pwd = request.getParameter("userpass");
		String directory = getServletContext().getInitParameter("BDBstore");
		DBWrapper db = new DBWrapper(directory);
		
		if(user != null && pwd != null && db.hasUser(user)){
			//get username and the corresponding password from the db
			if(db.checkPassword(user, pwd)){
				HttpSession session=request.getSession();  
				session.setMaxInactiveInterval(1200);
		        session.setAttribute("name",user);
				response.sendRedirect("/HW2/MyChannels");
			}
			else{
				PrintWriter out= response.getWriter();
	            out.println("<font color=red>Wrong Password!</font><br><br>");
	            out.println("<form action=\"/HW2/Login\"><input type=\"submit\" value=\"Log in\"></form>");
	            out.println("<form action=\"/HW2/Signup\"><input type=\"submit\" value=\"Sign up\"></form>");
			}
		}
		else{
			PrintWriter out= response.getWriter();
            out.println("<font color=red>No Such User!</font><br><br>");
            out.println("<form action=\"/HW2/Login\"><input type=\"submit\" value=\"Log in\"></form>");
            out.println("<form action=\"/HW2/Signup\"><input type=\"submit\" value=\"Sign up\"></form>");
		}
		
		db.closeEnv();
        

	}

}
