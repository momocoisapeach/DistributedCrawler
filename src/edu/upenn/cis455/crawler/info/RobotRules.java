package edu.upenn.cis455.crawler.info;

import java.util.ArrayList;
import java.util.HashMap;

// TODO: Auto-generated Javadoc
/**
 * The Class RobotRules.
 */
public class RobotRules {
	
	/** The rules. */
	public HashMap<String, Boolean> rules;
	
	/** The all. */
	public ArrayList<String> all;
	
	/** The User agent. */
	public String UserAgent;
	
	/**
	 * Instantiates a new robot rules.
	 */
	public RobotRules(){
		rules = new HashMap<String, Boolean>();
		all = new ArrayList<String>();
		rules.put("/robots.txt", true);
		all.add("/robots.txt");
	}

	/**
	 * Adds the rule.
	 *
	 * @param prefix the prefix
	 * @param allow the allow
	 */
	public void addRule(String prefix, boolean allow){
		all.add(prefix);
		rules.put(prefix, allow);
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
	 * The main method.
	 *
	 * @param args the arguments
	 */
	public static void main(String[] args){
		RobotRules a = new RobotRules();
		a.addRule("/org/plans.html", false);
		a.addRule("/org/", true);
		a.addRule("/serv",true);
		a.addRule("/",false);
		System.out.println(a.isAllowed("/index.html"));
		System.out.println(a.isAllowed("/robots.txt"));
		System.out.println(a.isAllowed("/services/fast.html"));
		System.out.println(a.isAllowed("/orgo.gif"));
		System.out.println(a.isAllowed("/org/about.html"));
		System.out.println(a.isAllowed("/org/plans.html"));
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
