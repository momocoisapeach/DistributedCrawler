/**
 * 
 */
package edu.upenn.cis455.mapreduce.job;

import java.net.MalformedURLException;
import java.net.URL;

import edu.upenn.cis455.mapreduce.Context;
import edu.upenn.cis455.mapreduce.Job;

// TODO: Auto-generated Javadoc
/**
 * The Class DistributeURL.
 *
 * @author dichenli
 * mapreduce job to distribute URLs among crawlers
 */
public class DistributeURL implements Job {
	
	/**
	 * for wordCount, key can be anything (don't care), value should be
	 * a line composed of many words.
	 *
	 * @param key the key
	 * @param value the value
	 * @param context the context
	 */
	public void map(String key, String value, Context context) {
		if(key == null || value == null || context == null) {
			return;
		}
		
		URL url = null;
		try {
			url = new URL(value);
		} catch (MalformedURLException e) {
			System.err.println("Illegal URL: " + value);
			return;
		}
		
		String key2 = url.getHost();
		context.write(key2, key + "\t" + value);
	}

	/* (non-Javadoc)
	 * @see edu.upenn.cis455.mapreduce.Job#reduce(java.lang.String, java.lang.String[], edu.upenn.cis455.mapreduce.Context)
	 */
	public void reduce(String key, String[] values, Context context) {
		for(String v : values) {
			String[] splited = v.split("\t");
			if(splited.length != 2) {
				System.err.println("bad reduce value: " + v);
			}
			context.write(splited[0], splited[1]);
		}
	}
}
