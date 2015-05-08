/**
 * 
 */
package edu.upenn.cis455.mapreduce.master;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import Utils.IOUtils;
import edu.upenn.cis455.mapreduce.Context;
import edu.upenn.cis455.mapreduce.Job;
import edu.upenn.cis455.mapreduce.MapReduceUtils;
import edu.upenn.cis455.mapreduce.WebClient.*;

// TODO: Auto-generated Javadoc
/**
 * The Class JobMaster.
 *
 * @author dichenli
 * represents a job submitted by end user, its execution process 
 * and record the status of the job
 */
class JobMaster {
	
	/** The remote workers. */
	private ArrayList<RemoteWorker> remoteWorkers;
	
	/** The job class. */
	private String jobClass; //the class name of map reduce job to be done
	
	/** The input. */
	private String input; //input directory, used for Map 
	
	/** The map threads. */
	private int mapThreads; // number of threads each worker for map
	
	/** The num workers. */
	private int numWorkers; // number of workers
	
	/** The output. */
	private String output; //output directory, used for Reduce 
	
	/** The reduce threads. */
	private int reduceThreads; //number of threads each worker for reduce
	
	/** The msg. */
	private String msg; //flash message to be sent to endUser, set to "" everytime after called
	
	/** The failed. */
	private boolean failed; //job has failed
	
	/** The stage. */
	String stage; /*"init": not started 
	"mapping": workers are idle, mapping or waiting 
	"waiting": workers are all waiting
	"reducing": workers are waiting, reducing or idle
	"done": workers are all idle */
	
	
	/**
  * Instantiates a new job master.
  */
 private JobMaster() {
		stage = "init";
	}
	
	/**
	 * factory method, to create a JobMaster object with the fields for the nature
	 * of the job specified from user input form. It returns a job object
	 * with "failed" true and an error message when the input is invalid
	 *
	 * @param jobRequest the job request
	 * @return the job master
	 */
	static JobMaster createJob(HttpServletRequest jobRequest) {
		JobMaster job = new JobMaster();
		job.remoteWorkers = RemoteWorker.getAvailableWorkers();
		if(job.remoteWorkers == null || job.remoteWorkers.size() == 0) {
			System.err.println("no workers");
			return job.setError("No free workers!");
		}
		
		job.jobClass = jobRequest.getParameter(MasterUtils.ClassName);
		//test if the Job class exists
		Job temp = MapReduceUtils.getJob(job.jobClass);
		if(temp == null) {
			return job.setError("Can't find the job class!");
		}
		
		job.input = jobRequest.getParameter(MasterUtils.InDir);
		job.output = jobRequest.getParameter(MasterUtils.OutDir);
		if(job.input == null || job.output == null || job.input.equals(job.output)) {
			return job.setError("input or output null or they are identical");
		}
		
		try {
			job.mapThreads = Integer.parseInt(jobRequest.getParameter(MasterUtils.MapThreads));
			job.reduceThreads = Integer.parseInt(jobRequest.getParameter(MasterUtils.ReduceThreads));
		} catch (Exception e) {
			System.err.println("illegal number? " + jobRequest.getParameter(MasterUtils.MapThreads));
			System.err.println("illegal number? " + jobRequest.getParameter(MasterUtils.ReduceThreads));
			return job.setError("Illegal Number format");
		}
		if(job.mapThreads <= 0 || job.reduceThreads <= 0) {
			return job.setError("Nonpositive number input!");
		}
		
		job.msg = "The job was successfully submitted.";
		return job;
	}
	
	/**
	 * Valid job dir.
	 *
	 * @param relativeDir the relative dir
	 * @return true, if successful
	 */
	@Deprecated //not in use
	private static boolean validJobDir(String relativeDir) {
		if(relativeDir == null || relativeDir.equals("")) {
			return false;
		}
		if(!IOUtils.dirExists(relativeDir)) {
			return false;
		}
		return true;
	}

	/**
	 * send job requests to workers.
	 */
	private void sendJobRequest() {
		
	}

	/**
	 * Sets the error.
	 *
	 * @param string the string
	 * @return the job master
	 */
	JobMaster setError(String string) {
		this.failed = true;
		this.msg = string;
		return this;
	}

	/**
	 * msg is flash message, delete after reading.
	 *
	 * @return the and remove msg
	 */
	String getAndRemoveMsg() {
		String temp = msg;
		msg = null;
		return temp;
	}
	
	/**
	 * Failed.
	 *
	 * @return true, if successful
	 */
	boolean failed() {
		return failed;
	}
	
	/**
	 * formulate list of workers in the body of /runmap POST
	 * example: "&numWorkers=1&worker1=1.1.1.1:1111"
	 *
	 * @param remoteWorkers the remote workers
	 * @return the string
	 */
	static String workerList(ArrayList<RemoteWorker> remoteWorkers) {
		String numWorkers = "&numWorkers=" + remoteWorkers.size();
		int count = 1;
		for(RemoteWorker w : remoteWorkers) {
			numWorkers += ("&worker" + count + "=" + w.host.toString());
			count++;
		}
		return numWorkers;
	}
	
	/**
	 * formulate request to be sent to a single worker, with the info of all
	 * workers and the nature of the job .
	 *
	 * @param remoteWorker the remote worker
	 * @param remoteWorkers the remote workers
	 * @return true, if successful
	 */
	boolean mapRequest(RemoteWorker remoteWorker, ArrayList<RemoteWorker> remoteWorkers) {
		String url = remoteWorker.host.getHostUrl("http") + "runmap";
		WebClientRequest request = WebClientRequest
				.getWebClientRequest(url, "POST", MasterUtils.masterAgent);
		request.setContentType("application/x-www-form-urlencoded");
		Writer writer = request.getBodyWriter();
		try {
			writer.write("job=" + this.jobClass 
					+ "&input=" + input + "&numThreads=" + mapThreads);
			writer.write(workerList(remoteWorkers));
		} catch (IOException e) {
			//won't happen, it's stringwriter
			e.printStackTrace();
			setError("JobMaster mapRequest: writer IOException");
			return false;
		}
		WebClientResponse response = new HttpSocketIO().sendRequest(request);
		if(response.getStatusCode() >= 300) {
			setError("JobMaster.mapRequest: worker error");
			return false;
		}
		/*at this stage, worker has received map request and correctly understand
		 * the request, as well as may be running. But not necessarily finished running
		 */ 
		return true;
	}
	
	/**
	 * Reduce request.
	 *
	 * @param remoteWorker the remote worker
	 * @return true, if successful
	 */
	boolean reduceRequest(RemoteWorker remoteWorker) {
		String url = remoteWorker.host.getHostUrl("http") + "runreduce";
		WebClientRequest request = WebClientRequest
				.getWebClientRequest(url, "POST", MasterUtils.masterAgent);
		request.setContentType("application/x-www-form-urlencoded");
		Writer writer = request.getBodyWriter();
		try {
			writer.write("job=" + this.jobClass 
					+ "&output=" + output + "&numThreads=" + reduceThreads);
		} catch (IOException e) {
			//won't happen, it's stringwriter
			e.printStackTrace();
			setError("JobMaster mapRequest: writer IOException");
			return false;
		}
		WebClientResponse response = new HttpSocketIO().sendRequest(request);
		if(response.getStatusCode() >= 300) {
			setError("JobMaster.reduceRequest: worker error");
			return false;
		}
		/*at this stage, the worker has received reduce request and correctly understand
		 * the request, as well as may be running. But not necessarily finished running
		 */ 
		return true;
	}
	
	/**
	 * send messages to workers to start map. return false if any worker send report that
	 * the task failed
	 *
	 * @return true, if successful
	 */
	boolean startMap() {
		if(!stage.equals("init")) {
			return false;
		}
		boolean success = true;
		stage = "mapping";
		for(RemoteWorker w : remoteWorkers) {
			success &= mapRequest(w, remoteWorkers);
		}
		return success;
	}

	/**
	 * return true if all remote workers are waiting.
	 *
	 * @return true, if successful
	 */
	public boolean allWaiting() {
		if(stage.equals("waiting")) {
			return true;
		}
		for(RemoteWorker w : remoteWorkers) {
			if(!w.status.equals("waiting")) {
				return false;
			}
		}
		stage = "waiting";
		return true;
	}
	
	/**
	 * All done.
	 *
	 * @return true, if successful
	 */
	public boolean allDone() {
		if(stage.equals("done")) {
			return true;
		}
		for(RemoteWorker w : remoteWorkers) {
			if(!w.status.equals("idle")) {
				return false;
			}
		}
		stage = "done";
		msg = "Job " + jobClass + " is done!";
		return true;
	}

	/**
	 * send messages to workers to start reduce.
	 *
	 * @return true, if successful
	 */
	public boolean StartReduce() {
		if(!stage.equals("waiting")) {
			System.err.println("JobMaster.startReduce: not waiting!");
			return false;
		}
		stage = "reducing";
		boolean success = true;
		for(RemoteWorker w : remoteWorkers) {
			success &= reduceRequest(w);
		}
		return success;
	}
	
}
