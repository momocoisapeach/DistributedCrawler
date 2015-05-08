/**
 * 
 */
package edu.upenn.cis455.mapreduce.worker;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import Utils.IOUtils;
import edu.upenn.cis455.mapreduce.MapReduceUtils;
import edu.upenn.cis455.mapreduce.WebClient.WebHost;

// TODO: Auto-generated Javadoc
/**
 * The Class Worker.
 *
 * @author dichenli
 * The worker (that is, "myself" for the worker servlet)
 * Each worker owns one thread for sending status report to master,
 * and a job runner which holds information for each job to be run.
 */
class Worker {
	
	/** The instance. */
	static Worker instance;

	/**
	 * singleton.
	 *
	 * @return single instance of Worker
	 */
	static Worker getInstance() {
		if(instance == null) {
			instance = new Worker();
		}
		return instance;
	}
	
	/** The spool in path. */
	static String spoolInPath = "/spool-in";
	
	/** The spool out path. */
	static String spoolOutPath = "/spool-out";
//	static String inputPath = "/input";
//	static String outputPath = "/output";
	
	/** The running. */
Boolean running; //always true, false only when server is turning off
	
	/** The master. */
	WebHost master; //IP and port number of master, example: 158.138.53.72:3000
	
	/** The listen port. */
	int listenPort; //the listening port number of the worker itself
//	String jobClass; //class name of the job being run. "None" if in idle
	
	/** The user agent. */
String userAgent; //useful when sending request. 
	
	/** The status reporter. */
	Thread statusReporter; /*a independent thread which constantly sleep for 10 seconds, 
	then wake up to send status report to master, then sleep again. Each time we call
	statusReporter.interrupt(), it will be waken up and send a report*/ 
	//the four folders are subfolders inside root folder
	/** The storage. */
 String storage; //specified by servlet.initParameter, root path
	
	/** The root. */
	File root;
//	File input;
	/** The spool in. */
File spoolIn;
	
	/** The spool out. */
	File spoolOut;
//	String outputPath; //output path is specified by user job submission
//	File output;
	
//	int numThread; //number of threads to run the next task
	/** The job runner. */
JobRunner jobRunner;
	
	
	/**
	 * Instantiates a new worker.
	 */
	private Worker() {
		userAgent = "Worker";
		running = true;
		statusReporter = new Thread(new StatusReporter(this)); //let them know each other
		jobRunner = new JobRunner(this);
	}

	/**
	 * Shutdown.
	 */
	void shutdown() {
		synchronized (running) {
			running = false;
		}
		statusReporter.interrupt();
		try {
			statusReporter.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		jobRunner.shutdown();
	}
	
	/**
	 * parse runmap request. return false if request fields has problem
	 *
	 * @param request the request
	 * @return true, if successful
	 */
	boolean parseRunmapRequest(HttpServletRequest request) {
		boolean success = jobRunner.parseRunmapRequest(request);
		if(!success) {
			return false;
		}
		setStorage(storage);
		return success;
	}
	
	/**
	 * Parses the runreduce request.
	 *
	 * @param request the request
	 * @return true, if successful
	 */
	boolean parseRunreduceRequest(HttpServletRequest request) {
		return jobRunner.parseRunreduceRequest(request);
	}
	
	/**
	 * start to run reduce, called after parseRunreduceRequest.
	 */
	void runruduce() {
		System.err.println("Worker.runreduce...");
		jobRunner.runreduce();
		System.err.println("Worker.runreduce done");
	}

	/**
	 * start to run map, called after parseRunmapRequest. It creates threads
	 * to run map, then send files to other workers, then send reply to master
	 * about "finished"
	 */
	void runmap() {
//		clearSpool();
		jobRunner.runmap();
		sendStatusRequest();
	}

	
	/**
	 * start the status reporter thread if it is not alive, otherwise do nothing.
	 */
	void startStatusReporter() {
		if(!statusReporter.isAlive()) {			
			statusReporter.start();
		}
	}
	
	/**
	 * wake up sleeping status reporter and let it send a status report.
	 */
	void sendStatusRequest() {
		statusReporter.interrupt();
	}

	/**
	 * set storage directory environment, creates subfolders, etc.
	 * return false if failed
	 * Called only by Servlet initialization
	 *
	 * @param storage the storage
	 * @return true, if successful
	 */
	boolean setStorage(String storage) {
		if(storage == null) {
			return false;
		}
		this.storage = storage;
		root = new File(storage);
		//create root folder for this worker
		boolean success = IOUtils.createFolder(root);
		if(!success) {
			return false;
		}

		spoolIn = new File(storage, spoolInPath);
		spoolOut = new File(storage, spoolOutPath);
//		input = new File(storage, inputPath);
//		output = new File(storage, outputPath);
		//create 4 subfolders. The behavior is that the folder is created only if it didn't exists
		//it doesn't delete any existing files in the folders. But it will delete
		//the file with the same name as a folder we need to create
		success &= IOUtils.createFolder(spoolIn);
		success &= IOUtils.createFolder(spoolOut);
//		success &= MapReduceUtils.createFolder(input);
//		success &= MapReduceUtils.createFolder(output);
		if(!success) {
			return false;
		}
		//delete all files in the three folders, but not the input folder
//		success &= MapReduceUtils.clearFolder(output);
//		success &= clearSpool();
		return success;
	}
	
	/**
	 * clear spool in and spool out folder for next task.
	 *
	 * @return true, if successful
	 */
	boolean clearSpool() {
		System.out.println("Worker.clearSpool");
		boolean success = IOUtils.clearFolder(spoolIn);
		success &= IOUtils.clearFolder(spoolOut);
		return success;
//		return true;
	}

	/**
	 * Gets the keys read.
	 *
	 * @return the keys read
	 */
	long getKeysRead() {
		return jobRunner.keysRead.get();
	}

	/**
	 * Gets the keys written.
	 *
	 * @return the keys written
	 */
	long getKeysWritten() {
		return jobRunner.keysWritten.get();
	}

	/**
	 * Gets the job class.
	 *
	 * @return the job class
	 */
	String getJobClass() {
		return jobRunner.jobClass;
	}

	/**
	 * Gets the status.
	 *
	 * @return the status
	 */
	String getStatus() {
		return jobRunner.status;
	}

	/**
	 * Gets the spool in file.
	 *
	 * @param request the request
	 * @param response the response
	 * @return the spool in file
	 */
	boolean getSpoolInFile(HttpServletRequest request, HttpServletResponse response) {
		BufferedReader reader = null;
		try {
			reader = request.getReader();
		} catch (IOException e) {
			MapReduceUtils.sendFailedMsg(response, 400);
			return false; //can't read body, bad request
		}
		
		if(!IOUtils.dirExists(spoolIn)) {//spoolIn error
			MapReduceUtils.sendFailedMsg(response, 500);
			return false;
		}
		File file = new File(spoolIn, request.getRemoteHost() + request.getRemotePort());
		for (int i = 1; file.exists(); i++) {
			file = new File(spoolIn, request.getRemoteHost() + request.getRemotePort() + i);
		}
		PrintWriter writer = null;
		try {
			file.createNewFile();
			writer = new PrintWriter(file);
			String line = null;
			while((line = reader.readLine()) != null) {
				writer.println(line);
			}
			writer.close();
			reader.close();
			MapReduceUtils.sendSuccesMsg(response);
			return true;
		} catch (IOException e) {
			MapReduceUtils.sendFailedMsg(response, 500);
			return false;
		}
	}

}
