/**
 * 
 */
package edu.upenn.cis455.mapreduce.master;

import java.util.*;

import javax.servlet.http.HttpServletRequest;

import edu.upenn.cis455.mapreduce.MapReduceUtils;
import edu.upenn.cis455.mapreduce.WebClient.WebClientResponse;
import edu.upenn.cis455.mapreduce.WebClient.WebHost;

/**
 * @author dichenli
 * Represents a worker from the perspective of the master
 */
class RemoteWorker {
	static HashMap<WebHost, RemoteWorker> RemoteWorkers = new HashMap<WebHost, RemoteWorker>(); // each host:port should map to one worker instance
	
	WebHost host; // stores the host and port of the worker 
	//port: the port number on which the worker is listening for HTTP requests (e.g., port=4711)
	int threads; //# of threads to be initialized to run the task, -1 if not specified
	String status; //status: mapping, waiting, reducing or idle, depending on what the worker is doing
	String job; //job: the name of the class that is currently being run (for instance, job=edu.upenn.cis455.mapreduce.job.MyJob)
	long keysRead;/*keysRead: the number of keys that have been read so far 
	(if the status is mapping or reducing), the number of keys that were 
	read by the last map (if the status is waiting) or zero if the status 
	is idle*/
	long keysWritten; /*the number of keys that have been written so far (if the status is mapping or
	reducing), the number of keys that were written by the last map (if the status is waiting) or
	the number of keys that were written by the last reduce (if the status is idle). If the node has
	never run any jobs, return 0.*/
	Date time; //last time a worker status report has been sent to master
	
//	static enum Status {
//		IDLE, MAPPING, WAITING, REDUCING
//	}
	
	/**
	 * get a list of all workers that are available for next job. The only 
	 * standard is that the worker is in idle
	 * @return
	 */
	static ArrayList<RemoteWorker> getAvailableWorkers() {
		//all idle workers
		ArrayList<RemoteWorker> idles = new ArrayList<RemoteWorker>();
		if(RemoteWorkers == null) {
			return null;
		}
		for(RemoteWorker w : RemoteWorkers.values()) {
			if(w.status == null) {
				System.err.println("worker with status null, debug it!");
				continue;
			}
			if(w.status.equalsIgnoreCase("idle")) {
				idles.add(w);
			}
		}
		return idles;
	}
	
	
	/**
	 * Factory method.
	 * first parse the host port from the comming status report request,
	 * find the correct worker object from Workers, create a new worker 
	 * instance if it is newly seen.<br>
	 * worker send a GET requests to master with the URL 
	 * http://masterHost:port/workerstatus, 
	 * with the following parameters (in the query string):
	 * port, status, job, keysRead, keysWritten.<br>
	 * 
	 * Example:
	 * GET /workerstatus?port=9090&status=idle&job=edu.upenn.cis455.mapreduce.job.MyJob&keysRead=153&keysWritten=95& HTTP/1.1<br>
	 * Host: masterHost:port<br>
	 * 
	 * Update the fields of this worker based on the request info
	 * then call updateStatus to that worker, and return the worker object
	 * @return the worker object found or created
	 * @throws NullPointerException if argument is null
	 */
	static RemoteWorker getWorker(HttpServletRequest statusReport) {
		if(statusReport == null) {
			throw new NullPointerException();
		}
		Integer port = MapReduceUtils.parseInt(statusReport.getParameter("port"));
		String status = statusReport.getParameter("status");
		String job = statusReport.getParameter("job");
		Integer keysRead = MapReduceUtils.parseInt(statusReport.getParameter("keysRead"));
		Integer keysWritten = MapReduceUtils.parseInt(statusReport.getParameter("keysWritten"));
		String hostName = statusReport.getRemoteHost();
		if(port == null || status == null || job == null 
				|| keysRead == null || keysWritten == null || hostName == null) {
			System.err.println("master try to get worker from statusreport has exception");
			return null;
		}
		
		WebHost host = new WebHost(hostName, port);
		RemoteWorker worker = RemoteWorkers.get(host);
		if(worker == null) {
			System.out.println("Didn't find remote worker: " + host + " create new one ...");
			worker = new RemoteWorker();
		}
		
		worker.host = host;
		worker.threads = -1;
		worker.status = status;
		worker.job = job;
		worker.keysRead = keysRead;
		worker.keysWritten = keysWritten;
		worker.time = new Date(); //right now
		
		RemoteWorkers.put(host, worker);
		return worker;
	}
	
	
	static long activeInterval = 30 * 1000; //30 seconds
	/**
	 * a worker is considered active if it has posted a /workerstatus within the last 30 seconds
	 * @return
	 */
	boolean isActive() {
		return new Date().getTime() - time.getTime() <= activeInterval;
	}
	
}
