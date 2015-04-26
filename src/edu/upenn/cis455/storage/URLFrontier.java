package edu.upenn.cis455.storage;

import java.util.LinkedList;
import java.util.Queue;

import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;


@Entity
public class URLFrontier {
	
	@PrimaryKey
	String name;
	
	Queue<String> frontier;
	
	public URLFrontier(String name){
		this.name = name;
		frontier = new LinkedList<String>();
	}
	private URLFrontier(){
		
	}
	
	public void addUrlToLast(String url){
		frontier.offer(url);
	}
	
	public boolean containsUrl(String url){
		if(frontier.size()==0){
			return false;
		}
		return frontier.contains(url);
	}
	
	public boolean isEmpty(){
		return frontier.isEmpty();
	}
	
	public String getFstUrl(){
		return frontier.poll();
	}
	
	public String peekFstUrl(){
		return frontier.peek();
	}
	
	public Queue<String> getFrontier(){
		return frontier;
	}

}
