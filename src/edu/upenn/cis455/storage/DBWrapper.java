package edu.upenn.cis455.storage;

import java.io.File;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Formatter;

import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import com.sleepycat.persist.EntityCursor;
import com.sleepycat.persist.EntityStore;
import com.sleepycat.persist.PrimaryIndex;
import com.sleepycat.persist.StoreConfig;

public class DBWrapper {
	
	private static String envDirectory = null;
	
	private static Environment myEnv;
	private static EntityStore store;
	public PrimaryIndex<String,User> userByName;
	public PrimaryIndex<String, Channel> channelByName;
	public PrimaryIndex<String, RawFile> fileByUrl;
	public PrimaryIndex<String, URLFrontier> urlFrontier;
	
	public PrimaryIndex<String, DocID> urlByDocID;
	public PrimaryIndex<String, DocLinks> linksByDocID;
	public PrimaryIndex<String, DocContent> contentByDocID;
	
	public String frontier = "URLFrontier";
	
	public DBWrapper(String envDirectory){
		if(!envDirectory.endsWith("/")) envDirectory += "/";
		
		EnvironmentConfig envConfig = new EnvironmentConfig();
		envConfig.setAllowCreate(true);
		StoreConfig stConfig = new StoreConfig();
		stConfig.setAllowCreate(true); 
		this.envDirectory = envDirectory;
		File f = new File(envDirectory);
		if(!f.exists()) f.mkdir();
		if(!f.isDirectory()) throw new IllegalArgumentException();
		myEnv = new Environment(f, envConfig);
		store = new EntityStore(myEnv, "EntityStore", stConfig);

		contentByDocID = store.getPrimaryIndex(String.class, DocContent.class);
		linksByDocID = store.getPrimaryIndex(String.class, DocLinks.class);
		urlByDocID = store.getPrimaryIndex(String.class, DocID.class);
		userByName = store.getPrimaryIndex(String.class, User.class);
		channelByName = store.getPrimaryIndex(String.class, Channel.class);
		fileByUrl = store.getPrimaryIndex(String.class, RawFile.class);
		urlFrontier = store.getPrimaryIndex(String.class, URLFrontier.class);
		
		
	}
	
	public EntityStore getStore(){
		return store;
	}
	
	public Environment getEnv(){
		return myEnv;
	}
	
	public boolean hasUrlInFrontier(String url){
		return urlFrontier.get(frontier).containsUrl(url);
	}
	
	public boolean hasUrlInDB(String url){
		return fileByUrl.contains(url);
	}
	
	public void initialLizeUrlQ(String startUrl){
		URLFrontier url_frontier = new URLFrontier(frontier);
		url_frontier.addUrlToLast(startUrl);
		urlFrontier.put(url_frontier);
	}
	
	public boolean addUrlToQueue(String url){
		URLFrontier temp = urlFrontier.get(frontier);
		temp.addUrlToLast(url);
		urlFrontier.put(temp);
		
		return true;
	}
	
	public String getFstUrlFromQ(){
//		System.out.println("q size is "+urlFrontier.get(frontier).frontier.size()+"\n"
//				+"the first element in q is"+urlFrontier.get(frontier).frontier.remove());
		URLFrontier temp = urlFrontier.get(frontier);
		String url = temp.getFstUrl();
		urlFrontier.put(temp);
		
//		System.out.println("after polling, the size is"+urlFrontier.get(frontier).frontier.size()+"\n");
		return url;
	}
	
	public String peekFstUrlFromQ(){
		return urlFrontier.get(frontier).peekFstUrl();
	}
	
	
	public boolean isQEmpty(){
		
		return urlFrontier.get(frontier).isEmpty();
	}
	
	// add the raw file into db
	public boolean addRawFile(RawFile file) {
		fileByUrl.put(file);
		return true;
	}
	
	public String getFile(String url){
		if(fileByUrl.get(url)!=null)
			return fileByUrl.get(url).getFile();
		return null;
	}
	
	public boolean updateRawFile(String url, RawFile file) {
		
		if(!fileByUrl.contains(url)){
			return false;
		}
		else{
			fileByUrl.put(file);
			return true;
		}
	}
	
	public boolean needDownload(String url, long lastModified){
		if(fileByUrl.contains(url)){
			Date lstModified = new Date(lastModified);
			if(fileByUrl.get(url).getTime().before(lstModified)){
				return true;
			}
			return false;
		}
		return true;
	}
	
	
	public Date getLstCrawled(String url){
		return fileByUrl.get(url).getTime();
	}
	
	
	/**
	 * given a url, and returns a boolean
	 * 
	 * the boolean is whether this url has saved
	 * in our db yet (as a doc id).
	 * 
	 * */
	public boolean containsUrl(String docid){
		return urlByDocID.contains(docid);
	}
	
	public boolean contentIsEmpty(String docid){
		return contentByDocID.get(docid).contentIsEmpty();
	}
	
	/**
	 * when a new url comes, hash it into a document id
	 * and put this primary key in docid->url table,
	 * docid->links(docid) table, and docid->content table.
	 * */
	public void addDocID(String docid){
		DocID id = new DocID(docid);
		urlByDocID.put(id);
		
		DocLinks idLinks = new DocLinks(docid);
		linksByDocID.put(idLinks);
		
		DocContent idContent = new DocContent(docid);
		contentByDocID.put(idContent);
		
	}
	
	public boolean hasSentHead(String docid){
		DocID id = urlByDocID.get(docid);
		return id.hasSentHead();
	}
	
	
	public void sentHead(String docid){
		DocID id = urlByDocID.get(docid);
		id.sentHead();
		urlByDocID.put(id);
	}
	
	public void addDocUrl(String docid, String url){
		DocID id = urlByDocID.get(docid);
		id.setUrl(url);
		urlByDocID.put(id);
	}
	
	/**
	 * add one point-to link's doc id to the original page's
	 * doc id, saved in the DocLinks table
	 * 
	 * in the database, the links are saved as an arraylist
	 * while it also put the data in a txt file
	 * formatted as "docid\tdocid1
	 * 				 docid\tdocid2
	 * 				 ......"
	 * 
	 * */
	public void  addDocLink(String docid, String linkid){
		DocLinks idlinks = linksByDocID.get(docid);
		idlinks.addLink(linkid);
		linksByDocID.put(idlinks);
	}
	
	public void addDocContent(String docid, String content, String url){
		DocContent idContent = contentByDocID.get(docid);
		idContent.setUrl(url);
		idContent.setContent(content);
		int len = content.length();
		idContent.setContentLength(len);
		
		contentByDocID.put(idContent);
	}
	
	
	// create a new account in the db
	public boolean addUser(User user) {
		if(userByName.contains(user.userName)){
			return false;
		}
		else{
			userByName.put(user);
			return true;
		}
	}
	
	public boolean addChannel(Channel channel) {	
		if(channelByName.contains(channel.channelName)){
			return false;
		}
		else{
			channelByName.put(channel);
			return true;
		}
	}
	
	//check if the user name has already existed in the db
	public boolean hasUser(String userName){
		return userByName.contains(userName);

	}
	
	public boolean hasChannel(String channelName){
		if(channelName!=null){
		return channelByName.contains(channelName);
		}
		return false;
	}
	
	public String getChannelOwner(String channelName){
		if(channelByName.contains(channelName)){
			return channelByName.get(channelName).getUserName();
		}
		return null;
	}
	
	//check if the user name has already existed in the db
	//if existed, return the password
	public boolean checkPassword(String userName, String input){
		if(userByName.contains(userName)){
			return userByName.get(userName).correctPassword(input);
		}
		return false;
	}
	
	
	//return all the channels that belong to a specific user
	public ArrayList<Channel> allMyChannels(String userName){
		ArrayList<String> myChannelNames = null;
		if(!userByName.contains(userName)){
			return null;
		}
		else{
			System.out.println("db contains a user called "+userName);
			myChannelNames = userByName.get(userName).getChannels();
			System.out.println("the user has "+myChannelNames.size()+" channels");
		}
		
		ArrayList<Channel> myChannels = new ArrayList<Channel>();
		EntityCursor<Channel> channelCursor = channelByName.entities();
		for(Channel w:channelCursor){
			if(myChannelNames.contains(w.channelName)){
				System.out.println("found one channel!");
				myChannels.add(w);
			}
		}
		channelCursor.close();
		return myChannels;
	}
	
	//return all the available channels in the db
	public ArrayList<Channel> getAllChannels(){
		ArrayList<Channel> channels = new ArrayList<Channel>();
		EntityCursor<Channel> channelCursor = channelByName.entities();
		System.out.println(channelByName.contains("ligoudan"));
		for(Channel w:channelCursor){
			channels.add(w);
			System.out.println("in the loop!");
		}
		channelCursor.close();
		return channels;
	}
	
	public ArrayList<String> getXpaths(String channelName){
		return channelByName.get(channelName).getXpaths();
	}
	
	public String getXslt(String channelName){
		return channelByName.get(channelName).getXsltUrl();
	}
	
	// create a new channel and add it into the user's entity 

	
	public boolean addChannelToUser(String channelName, String userName){
		if(!userByName.contains(userName)){
			return false;
		}
		else{
			User user = userByName.get(userName);
			user.addChannel(channelName);
			userByName.put(user);
			return true;
		}
	}
	
	
	
	//delete an existing channel and also delete the channel in the user's entity
	public boolean deleteChannel(String chnl, String userName){
		if(!channelByName.contains(chnl) || !userByName.contains(userName)){
			return false;
		}
		else if(!channelByName.get(chnl).getUserName().equals(userName)){
			return false;
		}
		channelByName.delete(chnl);
		User user = userByName.get(userName);
		user.deleteChannel(chnl);
		userByName.put(user);
		return true;
	}
	
	// add a xpath to a specific channel
	public boolean addXpath(String channelName, String xpath){
		Channel chnl = channelByName.get(channelName);
		if(chnl != null){
			if(chnl.getXpaths().contains(xpath)){
				return false;
			}
			
			chnl.addXpath(xpath);
			channelByName.put(chnl);
			return true;
		}
		return false;
		
	}
	
	public boolean setXsltOfChannel(String url, String channelName){
		Channel channel = channelByName.get(channelName);
		channel.setXsltUrl(url);
		channelByName.put(channel);
		return true;
	}
	
	public boolean addUrlToChannel(String url, String channelName){
		Channel chnl = channelByName.get(channelName);
		if(!chnl.getFileUrls().contains(url)){
			chnl.addFileUrl(url);
			channelByName.put(chnl);
		}
		return true;
	}
	
	public ArrayList<String> getMatchedUrls(String channelName){
		Channel chnl = channelByName.get(channelName);
		return chnl.getFileUrls();
	}

	
	
	public boolean removeUrlFromChannel(String url, String channelName){
		Channel chnl = channelByName.get(channelName);
		System.out.println("channelName is "+channelName+"\nand the url is@"+url+"@");
		ArrayList<String> matchedUrls = chnl.getFileUrls();
		String html = null;
		for(int i = 0; i <matchedUrls.size(); i++){
			System.out.println("matched is@"+ matchedUrls.get(i).length()+"@");
			if(matchedUrls.get(i).contentEquals(url)){
				System.out.println("match!!!!");
			}
		}
		if(chnl.getFileUrls().contains(url)){
			System.out.println("channel contains this url!");
			chnl.deleteFileUrl(url);
			channelByName.put(chnl);
			return true;
		}
		return false;
	}
	
	public PrimaryIndex<String, User> getUsers(){
		return userByName;
	}
	
	public PrimaryIndex<String, Channel> getChannels(){
		return channelByName;
	}
	
	public PrimaryIndex<String, RawFile> getFiles(){
		return fileByUrl;
	}
	

	public PrimaryIndex<String, URLFrontier> getFrontier(){
		return urlFrontier;
	}
	
	// Close the store and the environment
	public void closeEnv() {
		if(store != null){
			try{
				store.close();
			} catch(DatabaseException dbe){
				
			}
		}
		
		
		if (myEnv != null) {
			try {
				myEnv.close();
			} catch(DatabaseException dbe) {
				System.err.println("Error closing environment" +
				dbe.toString());
			}
		}
	}
	
	public BigInteger toBigInteger(String key) {
		try {
			MessageDigest messageDigest = MessageDigest.getInstance("SHA-1");
			messageDigest.update(key.getBytes());
			byte[] bytes = messageDigest.digest();
			Formatter formatter = new Formatter();
			for (int i = 0; i < bytes.length; i++) {
				formatter.format("%02x", bytes[i]);
			}
			String resString = formatter.toString();
			formatter.close();
			return new BigInteger(resString, 16);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return new BigInteger("0", 16);
	}
	
	/* TODO: write object store wrapper for BerkeleyDB */
	
}
