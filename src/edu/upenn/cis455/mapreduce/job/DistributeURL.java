/**
 * 
 */
package edu.upenn.cis455.mapreduce.job;

import java.net.MalformedURLException;
import java.net.URL;

import edu.upenn.cis455.mapreduce.Context;
import edu.upenn.cis455.mapreduce.Job;

/**
 * @author dichenli
 * mapreduce job to distribute URLs among crawlers
 */
public class DistributeURL implements Job {
	/**
	 * for wordCount, key can be anything (don't care), value should be
	 * a line composed of many words
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
