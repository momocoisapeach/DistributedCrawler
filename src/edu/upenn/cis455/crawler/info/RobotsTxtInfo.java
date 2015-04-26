package edu.upenn.cis455.crawler.info;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class RobotsTxtInfo {
	
	private HashMap<String,RobotRules> ruleLinks;
	
	private HashMap<String,Integer> crawlDelays;
	private HashMap<String, Calendar> lstCrawled;
	
	public RobotsTxtInfo(){
		ruleLinks = new HashMap<String,RobotRules>();
		crawlDelays = new HashMap<String,Integer>();
		lstCrawled = new HashMap<String, Calendar>();
	}
	
	public void updateLstCrawled(String host, Calendar now){
		lstCrawled.put(host, now);
	}
	
	public Calendar getLstCrawled(String host){
		return lstCrawled.get(host);
	}
	
	public void addRuleLink(String host, String prefix, boolean allow){
		if(!ruleLinks.containsKey(host)){
//			System.out.println("new robot rule");
			RobotRules newRule = new RobotRules();
			newRule.addRule(prefix, allow);
			ruleLinks.put(host, newRule);
		}
		else{
//			System.out.println("has this host");
			RobotRules rule = ruleLinks.get(host);
			if(rule == null)
				rule = new RobotRules();
			rule.addRule(prefix, allow);
			ruleLinks.put(host, rule);
		}
	}
	
	
	public void addCrawlDelay(String host, Integer delaySec){
		crawlDelays.put(host, delaySec);
	}
	
	public boolean containsHost(String host){
		return ruleLinks.containsKey(host);
	}
	
	public boolean isAllowed(String host, String url){
		if(ruleLinks.containsKey(host)){
			return ruleLinks.get(host).isAllowed(url);
		}
		return true;
	}
	
	public int getCrawlDelay(String key){
		return crawlDelays.get(key);
	}
	
	public void print(){
		for (Map.Entry<String, RobotRules> entry : ruleLinks.entrySet())
		{
			String host = entry.getKey();
			System.out.println("Host: "+host);
			RobotRules rule = entry.getValue();
			HashMap<String, Boolean> rules = rule.getAllRules();
			for (Map.Entry<String, Boolean> entry1 : rules.entrySet())
			{
				if(entry1.getValue() == true){
					System.out.println("Disallow: "+entry1.getKey());
				}
				else{
					System.out.println("Allow: "+entry1.getKey());
				}
			}
			if(crawlDelays.containsKey(host))
				System.out.println("Crawl-Delay: "+crawlDelays.get(host));
			System.out.println();
		    
		}
		
	}
	
	public boolean delayContainHost(String host){
		return crawlDelays.containsKey(host);
	}
}
