/**
 * 
 */
package edu.upenn.cis455.mapreduce.worker;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
import java.util.concurrent.atomic.AtomicLong;

import Utils.IOUtils;

import com.sleepycat.je.rep.impl.networkRestore.Protocol.Done;
import com.sun.org.apache.xml.internal.serialize.LineSeparator;

import edu.upenn.cis455.mapreduce.Context;

/**
 * @author dichenli
 * Context used for reduce
 */
class ReduceContext implements Context {

	File output;
	PrintWriter writer;
	AtomicLong keysWritten;
	int id;
	
	static long maxLines = 20;//max number of lines for each output file
	
	ReduceContext(File output, AtomicLong keysWritten, int id) {
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
		this.id = id;
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
		File done = new File(output.getAbsolutePath() + "_done.txt");
		boolean rv = output.renameTo(done);
		if(!rv || !IOUtils.fileExists(done) || done.length() <= 0) {
			System.err.println("rename file failed! " + output.getAbsolutePath());
		}
	}
	
	
	static long lines = 0;
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
		
		lines++;
		if(lines >= maxLines) { //more granular output
			closeWriter();
			output = new File(output.getParentFile(), "" + id + "_" + new Date().getTime());
			boolean rv = (IOUtils.createFile(output) && openWriter());
			if(!rv) {
				System.err.println("ReduceContext.write: create new file failed");
			}
		}
		
		System.out.println("ReduceContext.writer: output exists?" + output.exists());
	}

}
