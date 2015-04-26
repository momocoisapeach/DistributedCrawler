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

import edu.upenn.cis455.mapreduce.MapReduceUtils;
import edu.upenn.cis455.mapreduce.WebClient.IOUtils;
import edu.upenn.cis455.mapreduce.WebClient.WebHost;

/**
 * @author dichenli
 * The worker (that is, "myself" for the worker servlet)
 * Each worker owns one thread for sending status report to master,
 * and a job runner which holds information for each job to be run.
 */
class Worker {
	
	static Worker instance;

	/**
	 * singleton
	 * @return
	 */
	static Worker getInstance() {
		if(instance == null) {
			instance = new Worker();
		}
		return instance;
	}
	
	static String spoolInPath = "/spool-in";
	static String spoolOutPath = "/spool-out";
//	static String inputPath = "/input";
//	static String outputPath = "/output";
	
	Boolean running; //always true, false only when server is turning off
	WebHost master; //IP and port number of master, example: 158.138.53.72:3000
	int listenPort; //the listening port number of the worker itself
//	String jobClass; //class name of the job being run. "None" if in idle
	
	String userAgent; //useful when sending request. 
	Thread statusReporter; /*a independent thread which constantly sleep for 10 seconds, 
	then wake up to send status report to master, then sleep again. Each time we call
	statusReporter.interrupt(), it will be waken up and send a report*/ 
	//the four folders are subfolders inside root folder
	String storage; //specified by servlet.initParameter, root path
	File root;
//	File input;
	File spoolIn;
	File spoolOut;
//	String outputPath; //output path is specified by user job submission
//	File output;
	
//	int numThread; //number of threads to run the next task
	JobRunner jobRunner;
	
	
	private Worker() {
		userAgent = "Worker";
		running = true;
		statusReporter = new Thread(new StatusReporter(this)); //let them know each other
		jobRunner = new JobRunner(this);
	}

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
	 * @param request
	 * @return
	 */
	boolean parseRunmapRequest(HttpServletRequest request) {
		boolean success = jobRunner.parseRunmapRequest(request);
		if(!success) {
			return false;
		}
		setStorage(storage);
		return success;
	}
	
	boolean parseRunreduceRequest(HttpServletRequest request) {
		return jobRunner.parseRunreduceRequest(request);
	}
	
	/**
	 * start to run reduce, called after parseRunreduceRequest
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
	 * start the status reporter thread if it is not alive, otherwise do nothing
	 */
	void startStatusReporter() {
		if(!statusReporter.isAlive()) {			
			statusReporter.start();
		}
	}
	
	/**
	 * wake up sleeping status reporter and let it send a status report
	 */
	void sendStatusRequest() {
		statusReporter.interrupt();
	}

	/**
	 * set storage directory environment, creates subfolders, etc.
	 * return false if failed
	 * Called only by Servlet initialization
	 * @param storage
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
	 * clear spool in and spool out folder for next task
	 * @return
	 */
	boolean clearSpool() {
		System.out.println("Worker.clearSpool");
		boolean success = IOUtils.clearFolder(spoolIn);
		success &= IOUtils.clearFolder(spoolOut);
		return success;
//		return true;
	}

	long getKeysRead() {
		return jobRunner.keysRead.get();
	}

	long getKeysWritten() {
		return jobRunner.keysWritten.get();
	}

	String getJobClass() {
		return jobRunner.jobClass;
	}

	String getStatus() {
		return jobRunner.status;
	}

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
