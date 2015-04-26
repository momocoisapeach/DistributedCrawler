/**
 * 
 */
package edu.upenn.cis455.mapreduce.master;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import edu.upenn.cis455.mapreduce.Job;

/**
 * @author dichenli
 * static helper methods to generate response
 */
class MasterUtils {
	
	static final String ClassName = "ClassName";
	static final String InDir = "inDir";
	static final String OutDir = "outDir";
	static final String MapThreads = "MapThreads";
	static final String ReduceThreads = "ReduceThreads";
	static final String masterAgent = "Master"; //master user agent
	
	private static String textInput(String label, String name, String value) {
		if(label == null || name == null || value == null) {
			throw new NullPointerException();
		}
		return label + "<br><input type=\"text\" name=\"" + name 
				+ "\" value=\"" + value + "\"><br><br>\r\n";
	}
	
	private static String htmlEnd() {
		return "</body></html>\r\n";
	}
	
	private static String numberInput(String label, String name, String value) {
		if(label == null || name == null || value == null) {
			throw new NullPointerException();
		}
		return label + "<br><input type=\"number\" min=\"1\" name=\"" + name 
				+ "\" value=\"" + value + "\"><br><br>\r\n";
	}
	
	private static String htmlHead(String title) {
		return "<!DOCTYPE html><html><head><title>" 
				+ title + "</title></head><body>\r\n";
	}
	
	private static String jobForm() {
		return "<form action=\"/job\" method=\"POST\">\r\n"
		+ textInput("Class Name of Job", ClassName, "edu.upenn.cis455.mapreduce.job.WordCount")
		+ textInput("Input Directory", InDir, "/wordCountInput")
		+ textInput("Output Directory", OutDir, "/wordCountOutput")
		+ numberInput("Number of map threads", MapThreads, "1")
		+ numberInput("Number of reduce threads", ReduceThreads, "1")
		+ "<input type=\"submit\" value=\"submit job\">";
	}
	
	private static String message(String msg) {
		if(msg == null) {
			return "";
		}
		return "<p>Author: Dichen Li</p><p>SEAS login: dichenli</p>"
		+ "<p>" + msg + "</p><br>\r\n";
	}
	
	/**
	 * return a status table showing status of all workers
	 * @return
	 * html.append("\r\n<table style=\"text-align: left;width: 40%\">\r\n");
		html.append("<caption style=\"text-align: left;font-weight: bold;\">Channels List</caption>\r\n");
		html.append("<tr><th>Name</th><th>Creator</th></tr>\r\n");
		for(int i = 0; i < channels.length; i++) {
			html = appendChannelInfo(html, channels[i]);
		}
		html.append("</table>\r\n");
	 */
	private static String statusTable() {
		String table = "\r\n<table style=\"text-align: left;width: 40%\">\r\n"
				+ "<caption style=\"text-align: left;font-weight: bold;\">Workers List</caption>\r\n"
				+ "<tr><th>IP:port</th><th>status</th><th>Job Running</th><th>Keys Read</th><th>Keys Written</th></tr>\r\n";
		
		for(RemoteWorker w : RemoteWorker.RemoteWorkers.values()) {
			table = table + "<tr><td>" + w.host.toString() 
					+ "</td><td>" + w.status 
					+ "</td><td>" + w.job 
					+ "</td><td>" + w.keysRead 
					+ "</td><td>" + w.keysWritten + "</td></tr>";
		}
		table += "</table><br><br>\r\n";
		return table;
	}
	
	/**
	 * create status page
	 * @param msg a message shown on the page
	 * @return
	 */
	static String statusPage(String msg) {
		String head = htmlHead("Dichen Li MapReduce");
		return htmlHead("Dichen Li MapReduce") 
				+ message(msg) + statusTable() 
				+ jobForm() + htmlEnd();
	}
	
	/**
	 * When a client requests the URL /status from the master 
	 * servlet, the servlet should return a web page that contains 
	 * a) a table with status information about the workers, and 
	 * b) a web form for submitting jobs.
	 * The table should contain one row for each active worker 
	 * (a worker is considered active if it has posted a/workerstatus 
	 * within the last 30 seconds) and columns for 
	 * 1) IP:port, 2) the status; 3) the job; 4) the keys read, and 
	 * 5) the keys written.
	 * The web form for submitting jobs should contain fields for:
	 * 1. The class name of the job (e.g., edu.upenn.cis.cis455.mapreduce.job.MyJob)
	 * 2. The input directory, relative to the storage directory 
	 * (e.g., if this is set to bar and the storage directory is set 
	 * to ~/foo, the input should be read from ~/foo/bar)
	 * 3. The output directory, relative to the storage directory
	 * 4. The number of map threads to run on each worker
	 * 5. The number of reduce threads to run on each worker
	 * @param response
	 */
	static void sendStatusPage(HttpServletResponse response, String msg) throws IOException {
		try {
			response.setContentType("text/html");
			response.getWriter().print(MasterUtils.statusPage(msg));
			response.flushBuffer();
		} catch (IOException e) {
			e.printStackTrace();
			response.sendError(500);
		}
	}
	
}
