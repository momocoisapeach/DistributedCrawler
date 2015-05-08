package edu.upenn.cis455.crawler.info;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

// TODO: Auto-generated Javadoc
/**
 * The Class RobotsTxtInfo.
 */
public class RobotsTxtInfo {
	
	/** The rule links. */
	private HashMap<String,RobotRules> ruleLinks;
	
	/** The crawl delays. */
	private HashMap<String,Integer> crawlDelays;
	
	/** The lst crawled. */
	private HashMap<String, Calendar> lstCrawled;
	
	/**
	 * Instantiates a new robots txt info.
	 */
	public RobotsTxtInfo(){
		ruleLinks = new HashMap<String,RobotRules>();
		crawlDelays = new HashMap<String,Integer>();
		lstCrawled = new HashMap<String, Calendar>();
	}
	
	/**
	 * Update lst crawled.
	 *
	 * @param host the host
	 * @param now the now
	 */
	public void updateLstCrawled(String host, Calendar now){
		lstCrawled.put(host, now);
	}
	
	/**
	 * Gets the lst crawled.
	 *
	 * @param host the host
	 * @return the lst crawled
	 */
	public Calendar getLstCrawled(String host){
		return lstCrawled.get(host);
	}
	
	/**
	 * Adds the rule link.
	 *
	 * @param host the host
	 * @param prefix the prefix
	 * @param allow the allow
	 */
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
	
	
	/**
	 * Adds the crawl delay.
	 *
	 * @param host the host
	 * @param delaySec the delay sec
	 */
	public void addCrawlDelay(String host, Integer delaySec){
		crawlDelays.put(host, delaySec);
	}
	
	/**
	 * Contains host.
	 *
	 * @param host the host
	 * @return true, if successful
	 */
	public boolean containsHost(String host){
		return ruleLinks.containsKey(host);
	}
	
	/**
	 * Checks if is allowed.
	 *
	 * @param host the host
	 * @param url the url
	 * @return true, if is allowed
	 */
	public boolean isAllowed(String host, String url){
		if(ruleLinks.containsKey(host)){
			return ruleLinks.get(host).isAllowed(url);
		}
		return true;
	}
	
	/**
	 * Gets the crawl delay.
	 *
	 * @param key the key
	 * @return the crawl delay
	 */
	public int getCrawlDelay(String key){
		return crawlDelays.get(key);
	}
	
	/**
	 * Prints the.
	 */
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
	
	/**
	 * Delay contain host.
	 *
	 * @param host the host
	 * @return true, if successful
	 */
	public boolean delayContainHost(String host){
		return crawlDelays.containsKey(host);
	}
}
