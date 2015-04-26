package edu.upenn.cis455.storage;

import java.util.ArrayList;

import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;


@Entity
public class Channel {

	@PrimaryKey
	String channelName;
	

	private String userName;
	private String xsltUrl;
	private ArrayList<String> xpaths;
	private ArrayList<String> fileUrls;
	
	public Channel(String name){
		this.channelName = name;
		xpaths = new ArrayList<String>();
		fileUrls = new ArrayList<String>();
	}
	private Channel(){}

	public void setChannelName(String name){
		channelName = name;
	}
	
	public void setUserName(String name){
		userName = name;
	}
	
	public String getUserName(){
		return userName;
	}
	
	public void setXsltUrl(String url){
		xsltUrl = url;
	}
	
	public void addXpath(String xpath){
		if(!xpaths.contains(xpath)){
			xpaths.add(xpath);
		}
	}
	

	public String getName(){
		return channelName;
	}

	public void addFileUrl(String url){
		fileUrls.add(url);
	}
	
	public void deleteFileUrl(String url){
		fileUrls.remove(url);
	}
	
	public String getXsltUrl(){
		return xsltUrl;
	}
	
	public ArrayList<String> getFileUrls(){
		return fileUrls;
	}
	
	public ArrayList<String> getXpaths(){
		return xpaths;
	}
}
