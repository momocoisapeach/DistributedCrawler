/**
 * 
 */
package edu.upenn.cis455.mapreduce.worker;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.concurrent.atomic.AtomicLong;

import edu.upenn.cis455.mapreduce.Context;
import edu.upenn.cis455.mapreduce.WebClient.IOUtils;

/**
 * @author dichenli
 * Context used for reduce
 */
class ReduceContext implements Context {

	File output;
	PrintWriter writer;
	AtomicLong keysWritten;
	
	ReduceContext(File output, AtomicLong keysWritten) {
		if(!IOUtils.fileExists(output)) {
			System.err.println("ReduceContext: output file don't exists!");
			throw new IllegalArgumentException();
		}
		if(keysWritten == null) {
			throw new IllegalArgumentException();
		}
		this.output = output;
		System.out.println("ReduceContext: output file is: " + output.getAbsolutePath());
		this.keysWritten = keysWritten;
		boolean rv = openWriter();
		if(!rv) {
			throw new IllegalArgumentException();
		}
	}
	
	/**
	 * called before a context can be used to write
	 * @return
	 */
	boolean openWriter() {
		if(!IOUtils.fileExists(output)) {
			System.err.println("ReduceContext.openWriter: output file don't exists!");
			return false;
		}
		try {
			System.err.println("ReduceContext.openWriter: open writer for file: " + output.getAbsolutePath());
			writer = IOUtils.getWriter(output);
			System.err.println("ReduceContext.openWriter: open writer done.");
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	void closeWriter() {
		System.err.println("ReduceContext.closeWriter");
		if (writer != null) {
			writer.close();
		}
	}
	
	/**
	 * write to output
	 */
	@Override
	public void write(String key, String value) {
		String line = WorkerUtils.getLine(key, value);
		System.out.println("ReduceContext.writer: " + line);
		writer.println(line);
		writer.flush();
		System.out.println("output file length: " + output.length());
		keysWritten.incrementAndGet();
		System.out.println("ReduceContext.writer: output exists?" + output.exists());
	}

}
