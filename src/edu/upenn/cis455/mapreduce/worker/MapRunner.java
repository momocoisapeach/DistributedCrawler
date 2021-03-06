/**
 * 
 */
package edu.upenn.cis455.mapreduce.worker;

import java.io.File;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicLong;

import Utils.IOUtils;
import edu.upenn.cis455.mapreduce.Job;
import edu.upenn.cis455.mapreduce.job.WordCount;
// TODO: Auto-generated Javadoc

/**
 * The Class MapRunner.
 *
 * @author dichenli
 * a thread to run map task
 */
class MapRunner implements Runnable {

	/** The job runner. */
	JobRunner jobRunner;
	
	/** The id. */
	int id; //distinguish among different threads. Always start from 0 to (# of threads - 1)
	
	/** The num threads. */
	int numThreads;
	
	/** The peers. */
	HashMap<Integer, PeerWorker> peers;
	
	/** The num workers. */
	int numWorkers;
	
	/** The job. */
	Job job;
	
	/** The context. */
	MapContext context;
	
	/** The keys read. */
	AtomicLong keysRead; //references to JobRunner's keysRead/keysWritten
	
	/** The keys written. */
	AtomicLong keysWritten;
	
	/**
	 * Instantiates a new map runner.
	 *
	 * @param jobRunner the job runner
	 * @param id the id
	 * @param numThreads the num threads
	 * @param peers the peers
	 * @param job the job
	 * @param numWorkers the num workers
	 */
	MapRunner(JobRunner jobRunner, int id, int numThreads, 
			HashMap<Integer, PeerWorker> peers, Job job, int numWorkers) {
		if(jobRunner == null || id < 0 || id >= numThreads || numThreads <= 0
				|| peers == null || job == null) {
			throw new IllegalArgumentException();
		}
		
		this.jobRunner = jobRunner;
		this.id = id;
		this.numThreads = numThreads;
		this.peers = peers;
		this.numWorkers = peers.size();
		this.job = job;
		this.keysRead = jobRunner.keysRead;
		this.keysWritten = jobRunner.keysWritten;
		this.context = new MapContext(peers, numWorkers, keysWritten);
	}
	
	
	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		System.out.println("MapRunner.run: start");
		File[] files = jobRunner.input.listFiles();
		if (files == null || files.length == 0) {
			System.err.println("MapRunner has empty input files! debug it");
			throw new IllegalArgumentException();
		}
		System.out.println("MapRunner.run: found number of files: " + files.length);
		int len = files.length;
		for (int i = 0; i < len; i++) { 
			File file = files[i];
			if ( !IOUtils.fileExists(file)) {
				System.err.println("Maprunner.run: !file exists");
				continue;
			}
			if ( !IOUtils.getExtension(file).equalsIgnoreCase("done") ) {
				continue;
			}
			
			Scanner scanner = IOUtils.getScanner(file);
			readInput(scanner);
			scanner.close();
		}
		
	}

	/**
	 * read a input file line by line, and do job.map()
	 *
	 * @param scanner the scanner
	 */
	private void readInput(Scanner scanner) {
		for(int i = 0; scanner.hasNextLine(); i++) {//i: line count
			String line = scanner.nextLine();
			keysRead.incrementAndGet(); //keysRead++ in a concurrent way
			if(i % numThreads == id) { //this thread is responsible for the line
				System.out.println("Thead " + id + " processing the line: " + line);
				String[] split = WorkerUtils.splitKeyValue(line);
				if(split == null) {
					continue;
				}
				job.map(split[0], split[1], context);
			}
		}
	}
	
	
	
	
}
