package edu.upenn.cis455.storage;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;

import edu.upenn.cis455.crawler.info.RobotRules;
import edu.upenn.cis455.crawler.info.RobotRules.MyComparator;

@Entity
public class Robots {
	@PrimaryKey 
	String host;
	

	private int crawlDelays = 0;
	private long lstCrawled;
	
	private HashMap<String, Boolean> rules;
	private ArrayList<String> all;
	private String UserAgent;
	
	
	
	public Robots(String host){
		this.host = host;
		rules = new HashMap<String, Boolean>();
		all = new ArrayList<String>();
		rules.put("/robots.txt", true);
		all.add("/robots.txt");
		
	}
	
	public Robots(){}
	

	
	public void updateLstCrawled(long now){
		lstCrawled = now;
	}
	
	public long getLstCrawled(){
		return lstCrawled;
	}
	
	public void addRuleLink(String prefix, boolean allow){
		
		all.add(prefix);
		rules.put(prefix, allow);

	}
	
	
	public void setCrawlDelay(int delaySec){
		crawlDelays = delaySec;
	}
	
	
	public boolean isAllowed(String url){
		sortRules();
		for(String prefix:all){
			if(url.startsWith(prefix)){
				return rules.get(prefix);
			}
		}		
		return true;
	}
	
	public int getCrawlDelay(){
		return crawlDelays;
	}
	
	

	
	public HashMap<String, Boolean> getAllRules(){
		return rules;
	}
	
	public void sortRules(){
		MyComparator comp = new MyComparator("");
		java.util.Collections.sort(all, comp);
	}
	

	
	public class MyComparator implements java.util.Comparator<String> {

	    private int referenceLength;

	    public MyComparator(String reference) {
	        super();
	        this.referenceLength = reference.length();
	    }

	    public int compare(String s1, String s2) {
	        int dist1 = Math.abs(s1.length() - referenceLength);
	        int dist2 = Math.abs(s2.length() - referenceLength);

	        return dist2 - dist1;
	    }
	}
	


}
