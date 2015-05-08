package edu.upenn.cis455.mapreduce.master;

import java.io.*;

import javax.servlet.*;
import javax.servlet.http.*;

import edu.upenn.cis455.mapreduce.MapReduceUtils;

// TODO: Auto-generated Javadoc
/**
 * The Class MasterServlet.
 */
public class MasterServlet extends HttpServlet {

	/** The Constant serialVersionUID. */
	static final long serialVersionUID = 455555001;
	
	/** The current job. */
	JobMaster currentJob;
	

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	public void doGet(HttpServletRequest request, HttpServletResponse response) 
			throws java.io.IOException {
		String urlInfo = request.getPathInfo();
		if(urlInfo == null) {
			System.err.println("urlInfo == null");
			response.sendError(500);
		} else if(urlInfo.equals("/")) {
			sendWelcome(response);
		} else if (urlInfo.equals("/workerstatus")) {
			System.out.println("MasterServlet GET /workerstatus");
			parseWorkerStatus(request, response);
		} else if (urlInfo.equals("/status")) {
			System.out.println("MasterServlet GET /status");
			String msg = "";
			if(currentJob != null) {
				msg = currentJob.getAndRemoveMsg();
			}
			MasterUtils.sendStatusPage(response, msg);
		} else { // others
			sendWelcome(response);
		}
	}
	
	
	/**
	 * Do post.
	 *
	 * @param request the request
	 * @param response the response
	 * @throws IOException POST /job: submit new job
	 */
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
		String urlInfo = request.getPathInfo();
		if(urlInfo == null) {
			System.err.println("urlInfo == null");
			response.sendError(500);
		} else if(urlInfo.equals("/job")) {
			jobHandler(request, response);
		} else {
			response.sendError(404);
		}
	}

	/**
	 * do the following things: parse request to get fields from the form
	 * submitted, send job requests to all proper workers, then send response
	 * to browser with a message that the job was submitted. 
	 *
	 * @param request the request
	 * @param response the response
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	private void jobHandler(HttpServletRequest request,
			HttpServletResponse response) throws IOException {
		
		currentJob = JobMaster.createJob(request);
		MasterUtils.sendStatusPage(response, currentJob.getAndRemoveMsg());
		if(!currentJob.failed()) {
			currentJob.startMap();
		}
	}


	/**
	 * parse status request sent by worker, update info of the worker record,
	 * then send response to worker (probably a very simple response).
	 *
	 * @param request the request
	 * @param response the response
	 */
	private void parseWorkerStatus(HttpServletRequest request,
			HttpServletResponse response) {
		RemoteWorker.getWorker(request); //will also update the fields of the RemoteWorker object
		if (currentJob == null) {
			//do nothing
		} else if (currentJob.allWaiting()) { //see if all workers has finished executing
			System.out.println("MasterServlet.parseWorkerStatus: all waiting");
			currentJob.StartReduce();
		} else if (currentJob.allDone()) { //mapreduce done
			//Do nothing
		}
		MapReduceUtils.sendSuccesMsg(response);
	}

	/**
	 * Send welcome.
	 *
	 * @param response the response
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	private void sendWelcome(HttpServletResponse response) throws IOException {
		response.setContentType("text/html");
		PrintWriter out = response.getWriter();
		out.println("<html><head><title>Master</title></head>");
		out.println("<body><h1>Hi, I am the master!</h1><p>Author: Dichen Li</p><p>SEAS login: dichenli</p></body></html>");
	}
}

