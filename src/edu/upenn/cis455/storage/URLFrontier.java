package edu.upenn.cis455.storage;

import java.util.LinkedList;
import java.util.Queue;

import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;


// TODO: Auto-generated Javadoc
/**
 * The Class URLFrontier.
 */
@Entity
public class URLFrontier {
	
	/** The name. */
	@PrimaryKey
	String name;
	
	/** The frontier. */
	Queue<String> frontier;
	
	/**
	 * Instantiates a new URL frontier.
	 *
	 * @param name the name
	 */
	public URLFrontier(String name){
		this.name = name;
		frontier = new LinkedList<String>();
	}
	
	/**
	 * Instantiates a new URL frontier.
	 */
	private URLFrontier(){
		
	}
	
	/**
	 * Adds the url to last.
	 *
	 * @param url the url
	 */
	public void addUrlToLast(String url){
		frontier.offer(url);
	}
	
	/**
	 * Contains url.
	 *
	 * @param url the url
	 * @return true, if successful
	 */
	public boolean containsUrl(String url){
		if(frontier.size()==0){
			return false;
		}
		return frontier.contains(url);
	}
	
	/**
	 * Checks if is empty.
	 *
	 * @return true, if is empty
	 */
	public boolean isEmpty(){
		return frontier.isEmpty();
	}
	
	/**
	 * Gets the fst url.
	 *
	 * @return the fst url
	 */
	public String getFstUrl(){
		return frontier.poll();
	}
	
	/**
	 * Peek fst url.
	 *
	 * @return the string
	 */
	public String peekFstUrl(){
		return frontier.peek();
	}
	
	/**
	 * Gets the frontier.
	 *
	 * @return the frontier
	 */
	public Queue<String> getFrontier(){
		return frontier;
	}

}
