package edu.upenn.cis455.storage;

import java.util.ArrayList;

import org.jasypt.util.password.StrongPasswordEncryptor;

import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;

@Entity
public class User {
	
	@PrimaryKey
	String userName;
	
	
	private String password;
	private ArrayList<String> channels;
	
	public User(String userName){
		this.userName = userName;
		channels = new ArrayList<String>();
	}
	private User(){}
	
	


	
	public void setUserName(String name){
		userName = name;
	}
	
	public void setPassword(String password){
		StrongPasswordEncryptor passwordEncryptor = new StrongPasswordEncryptor();
		String encryptedPassword = passwordEncryptor.encryptPassword(password);
		this.password = encryptedPassword;
	}
	
//	public void setChannels(){
//		channels = new ArrayList<String>();
//	}
	

	
	public void addChannel(String channelName){
		channels.add(channelName);
	}
	
	public void deleteChannel(String channelName){
		channels.remove(channelName);
	}
	
	public ArrayList<String> getChannels(){
		return channels;
	}
	
	
	public boolean correctPassword(String input){
		StrongPasswordEncryptor passwordEncryptor = new StrongPasswordEncryptor();
		return passwordEncryptor.checkPassword(input, password);
	}
}
