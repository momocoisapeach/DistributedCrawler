package edu.upenn.cis455.crawler.info;

import java.util.ArrayList;
import java.util.HashMap;

public class RobotRules {
	public HashMap<String, Boolean> rules;
	public ArrayList<String> all;
	public String UserAgent;
	
	public RobotRules(){
		rules = new HashMap<String, Boolean>();
		all = new ArrayList<String>();
		rules.put("/robots.txt", true);
		all.add("/robots.txt");
	}

	public void addRule(String prefix, boolean allow){
		all.add(prefix);
		rules.put(prefix, allow);
	}
	
	public HashMap<String, Boolean> getAllRules(){
		return rules;
	}
	
	public void sortRules(){
		MyComparator comp = new MyComparator("");
		java.util.Collections.sort(all, comp);
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
