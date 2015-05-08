package edu.upenn.cis455.servlet;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import edu.upenn.cis455.storage.DBWrapper;
import edu.upenn.cis455.storage.User;

// TODO: Auto-generated Javadoc
/**
 * Servlet implementation class SignupServlet.
 */

public class SignupServlet extends HttpServlet {
	
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;
       
    /**
     * Instantiates a new signup servlet.
     *
     * @see HttpServlet#HttpServlet()
     */
    public SignupServlet() {
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
		String form = "<form action=\"Signup\" method=\"post\">  "
				+ "Create a User Name:<input type=\"text\" name=\"username\"/><br/><br/>"
				+ "Password:<input type=\"password\" name=\"userpass\"/><br/><br/>"
				+ "<input type=\"submit\" value=\"signup\"/></form> ";
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
		
		PrintWriter out= response.getWriter();
        String userName = request.getParameter("username");
        String pwd = request.getParameter("userpass");
		String directory = getServletContext().getInitParameter("BDBstore");
		DBWrapper db = new DBWrapper(directory);
		
		User user = new User(userName);
//		user.setUserName(userName);
		user.setPassword(pwd);
		if(db.addUser(user)){
			HttpSession session=request.getSession();  
	        session.setAttribute("name",userName);
			response.sendRedirect("/HW2/MyChannels");
		}
		else{
            out.println("<font color=blue>used name!</font><br><br>");
            out.println("<form action=\"/HW2/Signup\"><input type=\"submit\" value=\"Sign up\"></form>");
            out.println("<form action=\"/HW2/Login\"><input type=\"submit\" value=\"Log in\"></form>");
		}
		
		db.closeEnv();
		
	}

}
