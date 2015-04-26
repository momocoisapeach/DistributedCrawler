/**
 * 
 */
package edu.upenn.cis455.mapreduce.worker;

import java.io.*;

import javax.servlet.http.HttpServlet;

import edu.upenn.cis455.mapreduce.Job;
import edu.upenn.cis455.mapreduce.MapReduceUtils;
import edu.upenn.cis455.mapreduce.WebClient.IOUtils;

/**
 * @author dichenli
 *
 */
class WorkerUtils {

	static Job getJob(String className) throws InstantiationException, IllegalAccessException, ClassNotFoundException {
		return MapReduceUtils.getJob(className);
	}

	/**
	 * split key value pair
	 * @param line
	 * @return two elements, a key and a value. or returns null if otherwise. Two elements
	 * in the string[] is promised
	 */
	public static String[] splitKeyValue(String line) {
		String[] split = line.split("\t", 2);
		if(split.length != 2) {
			System.err.println("Can't split this line! " + line);
			return null;
		}
		return split;
	}

	/**
	 * sort the lines of a file by Runtime.exec(sort file). The File sorted
	 * will be deleted if exists and new file created given its path, so 
	 * don't send in existing file! 
	 * @param origin
	 * @return true if success
	 */
	public static boolean sortFile(File origin, File sorted) {
		String command = "sort " + origin.getAbsolutePath();
		Process process;
		try {
			process = Runtime.getRuntime().exec(command);
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		BufferedReader stdIn 
		= new BufferedReader(new InputStreamReader(process.getInputStream()));
		
		
		try {
			if(sorted.exists()) {
				sorted.delete();
			}
			sorted.createNewFile();
			PrintWriter writer = IOUtils.getWriter(sorted);
			
			// read the output from the command
			String line = null;
			while ((line = stdIn.readLine()) != null) {
				writer.println(line);
			}
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	public static String getLine(String key, String value) {
		return key + "\t" + value;
	}
}
