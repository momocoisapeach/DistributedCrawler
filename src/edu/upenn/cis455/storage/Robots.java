package edu.upenn.cis455.storage;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;

import edu.upenn.cis455.crawler.info.RobotRules;
import edu.upenn.cis455.crawler.info.RobotRules.MyComparator;

// TODO: Auto-generated Javadoc
/**
 * The Class Robots.
 */
@Entity
public class Robots {
	
	/** The host. */
	@PrimaryKey 
	String host;
	

	/** The crawl delays. */
	private int crawlDelays = 0;
	
	/** The lst crawled. */
	private long lstCrawled;
	
	/** The rules. */
	private HashMap<String, Boolean> rules;
	
	/** The all. */
	private ArrayList<String> all;
	
	/** The User agent. */
	private String UserAgent;
	
	
	
	/**
	 * Instantiates a new robots.
	 *
	 * @param host the host
	 */
	public Robots(String host){
		this.host = host;
		rules = new HashMap<String, Boolean>();
		all = new ArrayList<String>();
		rules.put("/robots.txt", true);
		all.add("/robots.txt");
		
	}
	
	/**
	 * Instantiates a new robots.
	 */
	public Robots(){}
	

	
	/**
	 * Update lst crawled.
	 *
	 * @param now the now
	 */
	public void updateLstCrawled(long now){
		lstCrawled = now;
	}
	
	/**
	 * Gets the lst crawled.
	 *
	 * @return the lst crawled
	 */
	public long getLstCrawled(){
		return lstCrawled;
	}
	
	/**
	 * Adds the rule link.
	 *
	 * @param prefix the prefix
	 * @param allow the allow
	 */
	public void addRuleLink(String prefix, boolean allow){
		
		all.add(prefix);
		rules.put(prefix, allow);

	}
	
	
	/**
	 * Sets the crawl delay.
	 *
	 * @param delaySec the new crawl delay
	 */
	public void setCrawlDelay(int delaySec){
		crawlDelays = delaySec;
	}
	
	
	/**
	 * Checks if is allowed.
	 *
	 * @param url the url
	 * @return true, if is allowed
	 */
	public boolean isAllowed(String url){
		sortRules();
		for(String prefix:all){
			if(url.startsWith(prefix)){
				return rules.get(prefix);
			}
		}		
		return true;
	}
	
	/**
	 * Gets the crawl delay.
	 *
	 * @return the crawl delay
	 */
	public int getCrawlDelay(){
		return crawlDelays;
	}
	
	

	
	/**
	 * Gets the all rules.
	 *
	 * @return the all rules
	 */
	public HashMap<String, Boolean> getAllRules(){
		return rules;
	}
	
	/**
	 * Sort rules.
	 */
	public void sortRules(){
		MyComparator comp = new MyComparator("");
		java.util.Collections.sort(all, comp);
	}
	

	
	/**
	 * The Class MyComparator.
	 */
	public class MyComparator implements java.util.Comparator<String> {

	    /** The reference length. */
    	private int referenceLength;

	    /**
    	 * Instantiates a new my comparator.
    	 *
    	 * @param reference the reference
    	 */
    	public MyComparator(String reference) {
	        super();
	        this.referenceLength = reference.length();
	    }

	    /* (non-Javadoc)
    	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
    	 */
    	public int compare(String s1, String s2) {
	        int dist1 = Math.abs(s1.length() - referenceLength);
	        int dist2 = Math.abs(s2.length() - referenceLength);

	        return dist2 - dist1;
	    }
	}
	


}
