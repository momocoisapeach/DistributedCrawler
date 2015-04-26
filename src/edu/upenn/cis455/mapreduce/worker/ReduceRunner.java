/**
 * 
 */
package edu.upenn.cis455.mapreduce.worker;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicLong;

import edu.upenn.cis455.mapreduce.Job;
import edu.upenn.cis455.mapreduce.MapReduceUtils;
import edu.upenn.cis455.mapreduce.WebClient.CollectionUtils;
import edu.upenn.cis455.mapreduce.WebClient.IOUtils;

/**
 * @author dichenli
 * a thread to run reduce
 */
class ReduceRunner implements Runnable {

	
	Job job;
	int numThreads;
	int id;
	File input;
//	File output; //file for this thread to write out result
	ReduceContext context;
	AtomicLong keysRead;
	AtomicLong keysWritten;
	
	ReduceRunner(Job job, int numThreads, int id, File input, File outputDir, JobRunner jobRunner) throws IOException {
		if(job == null || numThreads <= 0 || id < 0 
				|| numThreads <= id || input == null || !IOUtils.isValidFile(input) 
				|| outputDir == null || !IOUtils.dirExists(outputDir)) {
			System.err.println("ReduceRunner: invalid input");
			throw new IllegalArgumentException();
		}
		System.out.println("ReduceRunner input file: " + input.getAbsolutePath() + " outputDir: " + outputDir.getAbsolutePath());
		this.input = input;
		this.job = job;
		this.numThreads = numThreads;
		this.id = id;
		File output = new File(outputDir, "" + id + "_" + new Date().getTime());
		boolean rv = IOUtils.createFile(output);
		System.out.println("ReduceRunner create output file success? " + rv);
		if(!rv) {
			throw new IOException();
		}
		this.keysRead = jobRunner.keysRead;
		this.keysWritten = jobRunner.keysWritten;
		this.context = new ReduceContext(output, keysWritten, id);
	}
	
	
	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		System.out.println("ReduceRunner.run: start. Input file is: " + input.getAbsolutePath());
		Scanner sc = IOUtils.getScanner(input);
		if(sc == null || !sc.hasNextLine()) {
			System.out.println("ReduceRunner.run: no scanner");
			return;
		}
		
		String currKey = ""; //the current key
		ArrayList<String> values = new ArrayList<String>();
		for(int i = -1; sc.hasNextLine(); ) {
			String line = sc.nextLine();
			System.out.println("reduceRunner.run: read line: " + line);
			String[] splited = WorkerUtils.splitKeyValue(line);
			if(splited == null) {
				System.err.println("reduceRunner.run: splited is null!");
				continue;
			}
			System.out.println("reduceRunner.run: splited.length: " + splited.length);
			String key = splited[0];
			if(!key.equals(currKey)) {
				System.out.println("reduceRunner.run: new key found: " + key);
				if (i % numThreads == id && values.size() > 0) {
					System.out.println("reduceRunner.run: send to reduce: " + key + " with # values: " + values.size());
					job.reduce(currKey, CollectionUtils.toArray(values), context);
				}
				i++;
				currKey = key;
				if (i % numThreads == id) {
					System.out.println("reduceRunner.run: found key responsible for: " + key);
					values = new ArrayList<String>();
				}
			}
			if (i % numThreads == id) {
				System.out.println("reduceRunner.run: values add: " + splited[1]);
				values.add(splited[1]); //add value
				keysRead.incrementAndGet();
				if(!sc.hasNextLine()) {
					System.out.println("reduceRunner.run: last line, send to reduce: " + key + " with # values: " + values.size());
					job.reduce(currKey, CollectionUtils.toArray(values), context);
				}
			}
		}
		sc.close();
	}

}
