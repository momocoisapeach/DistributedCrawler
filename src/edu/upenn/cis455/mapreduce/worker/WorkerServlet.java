package edu.upenn.cis455.mapreduce.worker;

import java.io.*;

import javax.servlet.*;
import javax.servlet.http.*;

import edu.upenn.cis455.mapreduce.MapReduceUtils;
import edu.upenn.cis455.mapreduce.WebClient.WebHost;

public class WorkerServlet extends HttpServlet {

	static final long serialVersionUID = 455555002;
	static final Worker worker = Worker.getInstance(); //the worker "myself"

	/**
	 * nothing special yet
	 */
	public void doGet(HttpServletRequest request, HttpServletResponse response) 
			throws java.io.IOException {
		String urlInfo = request.getPathInfo();
		response.setContentType("text/html");
		if (urlInfo == null) {
			System.err.println("WorkerServlet: urlInfo == null");
			response.sendError(500);
		} else if(urlInfo.equals("/")) {
			sendWelcome(response);
		} else {
			sendWelcome(response);
		}
	}
	
	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws java.io.IOException {
		String urlInfo = request.getPathInfo();
		String userAgent = request.getHeader("User-Agent");
		if(userAgent == null) {
			System.err.println("WorkerServlet: User-Agent absent");
//			response.sendError(500);
		}
		
		if (urlInfo == null) {
			System.err.println("WorkerServlet: urlInfo == null");
			response.sendError(500);
		} else if(urlInfo.equals("/runmap")) {
			System.out.println("Worker received /runmap");
			if (worker.parseRunmapRequest(request)) {
				System.out.println("Worker parse /runmap successful");
				MapReduceUtils.sendSuccesMsg(response);
				worker.runmap();
			} else {
				System.err.println("Worker parse /runmap failed");
				MapReduceUtils.sendFailedMsg(response, 400);
			}
		} else if(urlInfo.equals("/runreduce")) {
			if (worker.parseRunreduceRequest(request)) {
				MapReduceUtils.sendSuccesMsg(response);
				worker.runruduce();
			} else {
				MapReduceUtils.sendFailedMsg(response, 400);
			}
		} else if (urlInfo.equals("/pushdata")) { //get data from peer worker
			worker.getSpoolInFile(request, response);
		}else {
			response.sendError(404);
		}
	}

	private void sendWelcome(HttpServletResponse response) throws IOException {
		PrintWriter out;
		try {
			out = response.getWriter();
			out.println("<html><head><title>Worker</title></head>");
			out.println("<body><h1>Hi, I am the worker!</h1><p>Author: Dichen Li</p><p>SEAS login: dichenli</p>\r\n</body></html>");
		} catch (IOException e) {
			response.sendError(500);
		}
	}
	
	/**
	 * initialize the servlet
	 */
	@Override
	public void init() throws ServletException {
		/* the worker servlet should look for an init parameter called 
		 * master, which should be in the form IP:port (Example: 
		 * 158.138.53.72:3000)
		 */
		
		String master = this.getInitParameter("master");
		if(master == null) {
			throw new ServletException("No initial parameter master found!");
		}
		try {
			worker.master = WebHost.parseHostPort(master);
		} catch (Exception e) {
			e.printStackTrace();
			throw new ServletException(e.getMessage());
		}
		
		try {
			worker.listenPort = Integer.parseInt(this.getInitParameter("port"));
		} catch (Exception e) {
			e.printStackTrace();
			throw new ServletException("Invalid port!");
		}
		
		//get storage directory root
		String storage = this.getInitParameter("storagedir");
		if(storage == null) {
			throw new ServletException("No initial parameter storage found!");
		}

		if(!worker.setStorage(storage)) {
			throw new ServletException("Can't create Storage folders!");
		}
		System.out.println("Start worker servlet. port: " + worker.listenPort + " master: " + worker.master);
		
		//send to master current status every 10 seconds
		worker.startStatusReporter();
	}
	
	@Override
	public void destroy() {
		worker.shutdown();
	}
}

