package edu.upenn.cis455.storage;

import java.util.ArrayList;

import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;


// TODO: Auto-generated Javadoc
/**
 * The Class Channel.
 */
@Entity
public class Channel {

	/** The channel name. */
	@PrimaryKey
	String channelName;
	

	/** The user name. */
	private String userName;
	
	/** The xslt url. */
	private String xsltUrl;
	
	/** The xpaths. */
	private ArrayList<String> xpaths;
	
	/** The file urls. */
	private ArrayList<String> fileUrls;
	
	/**
	 * Instantiates a new channel.
	 *
	 * @param name the name
	 */
	public Channel(String name){
		this.channelName = name;
		xpaths = new ArrayList<String>();
		fileUrls = new ArrayList<String>();
	}
	
	/**
	 * Instantiates a new channel.
	 */
	private Channel(){}

	/**
	 * Sets the channel name.
	 *
	 * @param name the new channel name
	 */
	public void setChannelName(String name){
		channelName = name;
	}
	
	/**
	 * Sets the user name.
	 *
	 * @param name the new user name
	 */
	public void setUserName(String name){
		userName = name;
	}
	
	/**
	 * Gets the user name.
	 *
	 * @return the user name
	 */
	public String getUserName(){
		return userName;
	}
	
	/**
	 * Sets the xslt url.
	 *
	 * @param url the new xslt url
	 */
	public void setXsltUrl(String url){
		xsltUrl = url;
	}
	
	/**
	 * Adds the xpath.
	 *
	 * @param xpath the xpath
	 */
	public void addXpath(String xpath){
		if(!xpaths.contains(xpath)){
			xpaths.add(xpath);
		}
	}
	

	/**
	 * Gets the name.
	 *
	 * @return the name
	 */
	public String getName(){
		return channelName;
	}

	/**
	 * Adds the file url.
	 *
	 * @param url the url
	 */
	public void addFileUrl(String url){
		fileUrls.add(url);
	}
	
	/**
	 * Delete file url.
	 *
	 * @param url the url
	 */
	public void deleteFileUrl(String url){
		fileUrls.remove(url);
	}
	
	/**
	 * Gets the xslt url.
	 *
	 * @return the xslt url
	 */
	public String getXsltUrl(){
		return xsltUrl;
	}
	
	/**
	 * Gets the file urls.
	 *
	 * @return the file urls
	 */
	public ArrayList<String> getFileUrls(){
		return fileUrls;
	}
	
	/**
	 * Gets the xpaths.
	 *
	 * @return the xpaths
	 */
	public ArrayList<String> getXpaths(){
		return xpaths;
	}
}
