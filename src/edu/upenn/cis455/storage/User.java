package edu.upenn.cis455.storage;

import java.util.ArrayList;

import org.jasypt.util.password.StrongPasswordEncryptor;

import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;

// TODO: Auto-generated Javadoc
/**
 * The Class User.
 */
@Entity
public class User {
	
	/** The user name. */
	@PrimaryKey
	String userName;
	
	
	/** The password. */
	private String password;
	
	/** The channels. */
	private ArrayList<String> channels;
	
	/**
	 * Instantiates a new user.
	 *
	 * @param userName the user name
	 */
	public User(String userName){
		this.userName = userName;
		channels = new ArrayList<String>();
	}
	
	/**
	 * Instantiates a new user.
	 */
	private User(){}
	
	


	
	/**
	 * Sets the user name.
	 *
	 * @param name the new user name
	 */
	public void setUserName(String name){
		userName = name;
	}
	
	/**
	 * Sets the password.
	 *
	 * @param password the new password
	 */
	public void setPassword(String password){
		StrongPasswordEncryptor passwordEncryptor = new StrongPasswordEncryptor();
		String encryptedPassword = passwordEncryptor.encryptPassword(password);
		this.password = encryptedPassword;
	}
	
//	public void setChannels(){
//		channels = new ArrayList<String>();
//	}
	

	
	/**
 * Adds the channel.
 *
 * @param channelName the channel name
 */
public void addChannel(String channelName){
		channels.add(channelName);
	}
	
	/**
	 * Delete channel.
	 *
	 * @param channelName the channel name
	 */
	public void deleteChannel(String channelName){
		channels.remove(channelName);
	}
	
	/**
	 * Gets the channels.
	 *
	 * @return the channels
	 */
	public ArrayList<String> getChannels(){
		return channels;
	}
	
	
	/**
	 * Correct password.
	 *
	 * @param input the input
	 * @return true, if successful
	 */
	public boolean correctPassword(String input){
		StrongPasswordEncryptor passwordEncryptor = new StrongPasswordEncryptor();
		return passwordEncryptor.checkPassword(input, password);
	}
}
