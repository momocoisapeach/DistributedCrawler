package edu.upenn.cis455.storage;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;

import edu.upenn.cis455.crawler.info.RobotRules;

@Entity
public class Robots {
	@PrimaryKey 
	String host;
	
	private RobotRules rules;
	private int crawlDelays;
	private Calendar lstCrawled;
	
	public Robots(String host){
		this.host = host;
		rules = new RobotRules();
	}
	
	private Robots(){}
	
	public void updateLstCrawled(Calendar now){
		lstCrawled = now;
	}
	
	public Calendar getLstCrawled(){
		return lstCrawled;
	}
	
	public void addRuleLink(String prefix, boolean allow){
		
		rules.addRule(prefix, allow);

	}
	
	
	public void addCrawlDelay(int delaySec){
		crawlDelays = delaySec;
	}
	
	
	public boolean isAllowed(String url){
		return rules.isAllowed(url);
	}
	
	public int getCrawlDelay(){
		return crawlDelays;
	}
	


}
