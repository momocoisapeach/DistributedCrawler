package edu.upenn.cis455.mapreduce.job;

import java.util.HashMap;
import java.util.Map;

import edu.upenn.cis455.mapreduce.Context;
import edu.upenn.cis455.mapreduce.Job;

// TODO: Auto-generated Javadoc
/**
 * The Class WordCount.
 */
public class WordCount implements Job {

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
		
//		HashMap<String, Integer> counts = new HashMap<String, Integer>();
		String[] words = value.split("[ |\t]+"); //one or more times of a space
		for(int i = 0; i < words.length; i++) {
			if(words[i].trim().equals("")) {
				continue;
			}
			context.write(words[i], "" + 1);
//			Integer count = counts.get(words[i]);
//			if(count == null) {
//				count = new Integer(0);
//			}
//			count++;
//			counts.put(words[i], count);
		}
//		for(Map.Entry<String, Integer> entry : counts.entrySet()) {
//			context.write(entry.getKey(), "" + entry.getValue());	
//		}
	}

	/* (non-Javadoc)
	 * @see edu.upenn.cis455.mapreduce.Job#reduce(java.lang.String, java.lang.String[], edu.upenn.cis455.mapreduce.Context)
	 */
	public void reduce(String key, String[] values, Context context) {
		long sum = 0;
		for(int i = 0; i < values.length; i++) {
			try {
				sum += Long.parseLong(values[i]);
			} catch(NumberFormatException e) {
				System.out.println("reduce: invalid value" + values[i]);
				//do nothing
			}
		}
		context.write(key, "" + sum);
	}

}
