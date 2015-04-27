/**
 * 
 */
package edu.upenn.cis455.mapreduce.worker;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicLong;

import javax.servlet.http.HttpServletRequest;

import Utils.IOUtils;
import edu.upenn.cis455.mapreduce.Job;
import edu.upenn.cis455.mapreduce.MapReduceUtils;
import edu.upenn.cis455.mapreduce.WebClient.WebHost;

/**
 * @author dichenli
 * everything needed to run a specific job. It should almost be in the same
 * class as the Worker class, but this class holds fields subject to change 
 * for each individual map and reduce request, while Worker holds fields 
 * independent of each job.
 */
public class JobRunner {
	Integer numThreads;
	Job job;
	String jobClass;
	Worker worker;
	
	String inputPath;
	File input;
	String outputPath;
	File output;
	File sorted; //input file for reduce phase, keys sorted
	
	Integer numWorkers;//number of workers on the same job, including myself
	int myNumber; //the serial number of myself among peer workers in this task.
	HashMap<Integer, PeerWorker> peers;//other workers, include myself
	
	AtomicLong keysRead; /*the number of keys that have been read so far (if the status is mapping or
reducing), the number of keys that were read by the last map (if the status is waiting) or
zero if the status is idle*/
	AtomicLong keysWritten; //number of keys read or written in the last job (map or reduce)
	String status;
	
	JobRunner(Worker worker) {
		this.worker = worker;
		keysRead = new AtomicLong(0);
		keysWritten = new AtomicLong(0);
		status ="idle";
		jobClass = "none";
	}
	
	/**
	 * set keysRead = 0, status = "idle", jobClass="none", worker send report to master
	 */
	void setIdle() {
		keysRead.set(0); //not keysWritten, it's supposed to show keys written for last reduce
		status = "idle";
		jobClass = "none";
		sorted = null;
		worker.sendStatusRequest();
	}
	
	private boolean parseJobClass(HttpServletRequest request) {
		jobClass = request.getParameter("job");
		if(jobClass == null) {
			System.err.println("JobRunner.parseRunmapRequest: jobClass null");
			return false;
		}
		job = MapReduceUtils.getJob(jobClass);
		if(job == null) {
			System.err.println("JobRunner.parseRunmapRequest: can't find class");
			return false;
		}
		System.out.println("JobRunner.parseRunmapRequest: class found");
		return true;
	}
	
	private boolean parseNumThreads(HttpServletRequest request) {
		this.numThreads = MapReduceUtils.parseInt(request.getParameter("numThreads"));
		if(numThreads == null || numThreads <= 0) {
			System.err.println("JobRunner.parseNumThread: numThread error: " + request.getParameter("numThreads"));
			return false;
		}
		return true;
	}
	
	private boolean parseNumWorkers(HttpServletRequest request) {
		this.numWorkers = MapReduceUtils.parseInt(request.getParameter("numWorkers"));
		if(numWorkers == null || numWorkers <= 0) {
			System.err.println("JobRunner.parseNumWorkers: workers error : " + request.getParameter("numWorkers"));
			return false;
		}
		return true;
	}
	
	/**
	 * either input or output: check if the dir exists and is a directory and has no collision with spoolIn or spoolOut
	 * @param dir
	 * @return
	 */
	private boolean checkDirectory(File dir) {
		if(dir.equals(worker.spoolIn) || dir.equals(worker.spoolOut)) {
			System.err.println("JobRunner.parseRunmapRequest: Directory name collision!");
			return false;
		}
		boolean rv = IOUtils.dirExists(dir);
		if(!rv) {
			System.out.println("JobRunner.checkDirectory: dir don't exists! " + dir.getAbsolutePath());
			return false;
		}
		return true;
	}
	
	/**
	 * parse a runmap request to get the info about the task
	 * @param runmap
	 * @return
	 */
	boolean parseRunmapRequest(HttpServletRequest runmap) {
		if(!this.status.equals("idle")) {
			System.err.println("JobRunner.parseRunmapRequest: not idle");
			return false;
		}
		boolean rv = parseJobClass(runmap);
		if(!rv) {
			return false;
		}
		
		this.inputPath = runmap.getParameter("input");
		System.out.println("JobRunner.parseRunmapRequest: inputPath: " + inputPath);
		if(inputPath == null) {
			return false;
		}
		input = new File(worker.root, inputPath);
		rv = checkDirectory(input); // && IOUtils.containsFile(input);
		if(!rv) {
			System.err.println("JobRunner.parseRunmapRequest: input dir error");
			return false;
		}
		
		rv = parseNumThreads(runmap) && parseNumWorkers(runmap);
		if(!rv) {
			System.out.println("JobRunner.parseRunmapRequest: parsenumthreads/workers error");
			return false;
		}
		
		rv = worker.clearSpool();
		if(!rv) {
			System.err.println("JobRunner.runMap: clearSoopl error");
			return false;
		}
		
		String localIP = runmap.getLocalAddr();
		String localHost = runmap.getLocalName();
		int port = worker.listenPort;
		String ipPort = localIP + ":" + port;
		System.out.println("JobRunner.parseRunmapRequest: localIP: " + ipPort);
		String hostPort = localHost + ":" + port;
		System.out.println("JobRunner.parseRunmapRequest: localHost: " + hostPort);
		myNumber = -1;
		
		peers = new HashMap<Integer, PeerWorker>();
		for(int i = 1; i <= numWorkers; i++) {
			String workerName = "worker" + i;
			String peerHostPort = runmap.getParameter(workerName);
			if(peerHostPort == null) {
				System.err.println("JobRunner.parseRunmapRequest: can't find worker" + i);
				return false;
			}
			if(peerHostPort.equals(hostPort) || peerHostPort.equals(ipPort)) {
				System.out.println("JobRunner.parseRunmapRequest: worker match myself " + peerHostPort);
				if(myNumber > 0) {
					System.err.println("JobRunner.parseRunmapRequest: myself apears twice");
					return false; 
				}
				myNumber = i;
			}
			try {
				WebHost peerHost = WebHost.parseHostPort(peerHostPort);
				System.out.println("JobRunner.parseRunmapRequest: parse hostPort: " + peerHost);
				File peerOut = new File(worker.spoolOut, "" + i); //file name is a simple number
				rv = peerOut.createNewFile();
				if (!rv) {
					System.err.println("peerOut.createNewFile failed");
					return false;
				}
				PeerWorker peer = new PeerWorker(i, peerHost, peerOut, (i == myNumber));
				peers.put((Integer) i, peer);
			} catch (Exception e) {
				System.err.println("JobRunner.runMap: malformatted worker host port: " + hostPort);
				return false;
			}
		}
		System.out.println("JobRunner.parseRunmapRequest: success!");
		return true;
	}

	/**
	 * parse incoming request from master to run reduce
	 * @param request
	 * @return
	 */
	boolean parseRunreduceRequest(HttpServletRequest runreduce) {
		if(!status.equals("waiting")) {
			System.err.println("JobRunner.parseRunreduceRequest: not waiting");
			return false;
		}
		this.status = "reduce_prep";
		boolean rv = parseJobClass(runreduce);
		if(!rv) {
			return false;
		}
		
		this.outputPath = runreduce.getParameter("output");
		if(outputPath == null) {
			return false;
		}
		output = new File(worker.root, outputPath);
		rv = checkDirectory(output);
		if(!rv) {
			System.err.println("JobRunner.parseReduceRequest: can't find output");
			return false;
		}
//		System.out.println("JobRunner.parseReduceRequest: clear output... " + output.getAbsolutePath());
//		IOUtils.clearFolder(output);
		rv = parseNumThreads(runreduce);
		if(!rv) {
			System.err.println("JobRunner.parseReduceRequest: can't parse numthreads");
			return false;
		}
		System.out.println("JobRunner.parseReduceRequest: done");
		return true;
	}

	/**
	 * run map
	 */
	void runmap() {
		if(!status.equals("idle")) {
			System.err.println("JobRunner.runmap: not idle");
			throw new IllegalStateException();
		}
		
		System.out.println("JobRunner.runmap: start");
		this.status = "mapping";
		keysRead.set(0);
		keysWritten.set(0);
		
		
		
		Thread[] threads = new Thread[numThreads];
		for (int i = 0; i < numThreads; i++) {
			threads[i] = new Thread(new MapRunner(this, 
					i, numThreads, peers, job, numWorkers));
		}
		for(int i = 0; i < numThreads; i++) {
			threads[i].start();
			System.out.println("JobRunner.runmap: start thread" + i);
		}
		for(int i = 0; i < numThreads; i++) { //join threads
			try {
				threads[i].join();
				System.out.println("JobRunner.runmap: join thread" + i);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		for(PeerWorker p : peers.values()) { //send files to peers
			p.closeOutput();
			System.out.println("JobRunner.runmap: peerWorker send file ....");
			p.sendFile();
			System.out.println("JobRunner.runmap: peerWorker send file done");
		}
		setWaiting();
	}

	/**
	 * set status to waiting
	 */
	private void setWaiting() {
		this.status = "waiting";
	}

	/**
	 * run reduce
	 */
	void runreduce() {
		System.out.println("JobRunner.runreduce: start");
		if(!status.equals("reduce_prep")) {
			System.err.println("JobRunner.runreduce: not reduce_prep!");
			throw new IllegalStateException();
		}
		this.status = "reducing";
		keysRead.set(0);
		keysWritten.set(0);
		
		System.out.println("JobRunner.runreduce: sorting...");
		boolean rv = sort(); //sort the lines in spool-in
		if(!rv) {
			System.err.println("JobRunner.runreduce: sort error");
			setIdle();
			return;
		}

		Thread[] threads = new Thread[numThreads];
		System.out.println("JobRunner.runreduce: new threads #: " + threads.length);
		for (int i = 0; i < numThreads; i++) {
			try {
				threads[i] = new Thread(
						new ReduceRunner(job, numThreads, i, sorted, output, this));
			} catch (IOException e) {
				e.printStackTrace();
				setIdle();
				return;
			}
		}
		for(int i = 0; i < numThreads; i++) {
			System.out.println("JobRunner.runreduce: start thread: " + i);
			threads[i].start();
		}
		for(int i = 0; i < numThreads; i++) { //join threads
			try {
				System.out.println("JobRunner.runreduce: join thread: " + i);
				threads[i].join();
			} catch (InterruptedException e) {
				System.err.println("JobRunner.runreduce: InterruptedException");
			}
		}
		setIdle();
	}

	/**
	 * sort the key-value pairs in spool-in
	 * return false if abnormal happens
	 */
	private boolean sort() {
		File spoolIn = worker.spoolIn;
		File[] files = spoolIn.listFiles();
		if(files == null || files.length == 0) { //no files in the directory
			return false;
		}
		//merge all files into a single file
		File singleFile = new File(spoolIn, "/single");
		try {
			singleFile.createNewFile();
			PrintWriter writer = IOUtils.getWriter(singleFile);
			for(File f : files) {
				Scanner sc = new Scanner(f);
				while(sc.hasNextLine()) {
					writer.println(sc.nextLine());
				}
				sc.close();
			}
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		
		sorted = getSorted();
		boolean rv = WorkerUtils.sortFile(singleFile, sorted);
		if(!rv) {
			return false;
		}
		return rv;
	}
	
	/**
	 * get the file of sorted data to run reduce
	 * @return
	 */
	private File getSorted() {
		return new File(worker.spoolIn, "/sorted");
	}
	

	/**
	 * if unlucky, server shutdown may be issued while a job is still running by another
	 * thread
	 */
	void shutdown() {
		while(!this.status.equals("idle")) {
			System.out.println("JobRunner is not idle. Waiting to shutdown...");
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}


	
}
