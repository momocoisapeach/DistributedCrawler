package edu.upenn.cis455.storage;

import java.io.File;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Formatter;

import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import com.sleepycat.persist.EntityCursor;
import com.sleepycat.persist.EntityStore;
import com.sleepycat.persist.PrimaryIndex;
import com.sleepycat.persist.StoreConfig;

// TODO: Auto-generated Javadoc
/**
 * The Class DBWrapper.
 */
public class DBWrapper {
	
	/** The env directory. */
	private static String envDirectory = null;
	
	/** The my env. */
	private static Environment myEnv;
	
	/** The store. */
	private static EntityStore store;
	
	/** The user by name. */
	public PrimaryIndex<String,User> userByName;
	
	/** The channel by name. */
	public PrimaryIndex<String, Channel> channelByName;
	
	/** The file by url. */
	public PrimaryIndex<String, RawFile> fileByUrl;
	
	/** The url frontier. */
	public PrimaryIndex<String, URLFrontier> urlFrontier;
	
	/** The url by doc id. */
	public PrimaryIndex<String, DocID> urlByDocID;
	
	/** The links by doc id. */
	public PrimaryIndex<String, DocLinks> linksByDocID;
	
	/** The content by doc id. */
	public PrimaryIndex<String, DocContent> contentByDocID;
	
	/** The robots by host. */
	public PrimaryIndex<String, Robots> robotsByHost;
	
	/** The frontier. */
	public String frontier = "URLFrontier";
	
	/**
	 * Instantiates a new DB wrapper.
	 *
	 * @param envDirectory the env directory
	 */
	public DBWrapper(String envDirectory){
		if(!envDirectory.endsWith("/")) envDirectory += "/";
		
		EnvironmentConfig envConfig = new EnvironmentConfig();
		envConfig.setAllowCreate(true);
		StoreConfig stConfig = new StoreConfig();
		stConfig.setAllowCreate(true); 
		this.envDirectory = envDirectory;
		File f = new File(envDirectory);
		if(!f.exists()) f.mkdir();
		
		File content = new File(Config.DocContent_File);
		if(!content.exists()) {
			content.mkdirs();
		}
		
		File input = new File(Config.MapReduce_Input);
		if(!input.exists()) {
			input.mkdirs();
		}
		
		File output = new File(Config.MapReduce_Output);
		if(!output.exists()) {
			output.mkdirs();
		}
		
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
		robotsByHost = store.getPrimaryIndex(String.class, Robots.class);
		
		
	}
	
	/**
	 * Gets the store.
	 *
	 * @return the store
	 */
	public EntityStore getStore(){
		return store;
	}
	
	/**
	 * Gets the env.
	 *
	 * @return the env
	 */
	public Environment getEnv(){
		return myEnv;
	}
	
	/**
	 * Checks for url in frontier.
	 *
	 * @param url the url
	 * @return true, if successful
	 */
	public boolean hasUrlInFrontier(String url){
		return urlFrontier.get(frontier).containsUrl(url);
	}
	
	/**
	 * Checks for url in db.
	 *
	 * @param url the url
	 * @return true, if successful
	 */
	public boolean hasUrlInDB(String url){
		return fileByUrl.contains(url);
	}
	
	/**
	 * Initial lize url q.
	 */
	public void initialLizeUrlQ(){
		if(urlFrontier.contains(frontier)){
		}
		else{
			URLFrontier url_frontier = new URLFrontier(frontier);
			urlFrontier.put(url_frontier);
		}
	}
	
	/**
	 * Adds the url to queue.
	 *
	 * @param url the url
	 * @return true, if successful
	 */
	public boolean addUrlToQueue(String url){
		if(urlFrontier.contains(frontier)){
			URLFrontier temp = urlFrontier.get(frontier);
			temp.addUrlToLast(url);
			urlFrontier.put(temp);
		}
		else{
			URLFrontier url_frontier = new URLFrontier(frontier);
			url_frontier.addUrlToLast(url);
			urlFrontier.put(url_frontier);
			
		}
		
		return true;
	}
	
	/**
	 * Gets the fst url from q.
	 *
	 * @return the fst url from q
	 */
	public String getFstUrlFromQ(){
//		System.out.println("q size is "+urlFrontier.get(frontier).frontier.size()+"\n"
//				+"the first element in q is"+urlFrontier.get(frontier).frontier.remove());
		URLFrontier temp = urlFrontier.get(frontier);
		String url = temp.getFstUrl();
		urlFrontier.put(temp);
		
//		System.out.println("after polling, the size is"+urlFrontier.get(frontier).frontier.size()+"\n");
		return url;
	}
	
	/**
	 * Peek fst url from q.
	 *
	 * @return the string
	 */
	public String peekFstUrlFromQ(){
		return urlFrontier.get(frontier).peekFstUrl();
	}
	
	
	/**
	 * Checks if is q empty.
	 *
	 * @return true, if is q empty
	 */
	public boolean isQEmpty(){
		
		return urlFrontier.get(frontier).isEmpty();
	}
	
	// add the raw file into db
	/**
	 * Adds the raw file.
	 *
	 * @param file the file
	 * @return true, if successful
	 */
	public boolean addRawFile(RawFile file) {
		fileByUrl.put(file);
		return true;
	}
	
	/**
	 * Gets the file.
	 *
	 * @param url the url
	 * @return the file
	 */
	public String getFile(String url){
		if(fileByUrl.get(url)!=null)
			return fileByUrl.get(url).getFile();
		return null;
	}
	
	/**
	 * Update raw file.
	 *
	 * @param url the url
	 * @param file the file
	 * @return true, if successful
	 */
	public boolean updateRawFile(String url, RawFile file) {
		
		if(!fileByUrl.contains(url)){
			return false;
		}
		else{
			fileByUrl.put(file);
			return true;
		}
	}
	
	/**
	 * Need download.
	 *
	 * @param url the url
	 * @param lastModified the last modified
	 * @return true, if successful
	 */
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
	
	
	/**
	 * Gets the lst crawled.
	 *
	 * @param url the url
	 * @return the lst crawled
	 */
	public Date getLstCrawled(String url){
		return fileByUrl.get(url).getTime();
	}
	
	
	/**
	 * given a url, and returns a boolean
	 * 
	 * the boolean is whether this url has saved
	 * in our db yet (as a doc id).
	 *
	 * @param docid the docid
	 * @return true, if successful
	 */
	public boolean containsUrl(String docid){
		return urlByDocID.contains(docid);
	}
	
	/**
	 * Content is empty.
	 *
	 * @param docid the docid
	 * @return true, if successful
	 */
	public boolean contentIsEmpty(String docid){
		return contentByDocID.get(docid).contentIsEmpty();
	}
	
	/**
	 * when a new url comes, hash it into a document id
	 * and put this primary key in docid->url table,
	 * docid->links(docid) table, and docid->content table.
	 *
	 * @param docid the docid
	 */
	public void addDocID(String docid){
		DocID id = new DocID(docid);
		urlByDocID.put(id);
		
		DocLinks idLinks = new DocLinks(docid);
		linksByDocID.put(idLinks);
		
		DocContent idContent = new DocContent(docid);
		contentByDocID.put(idContent);
		
	}
	
	/**
	 * Checks for sent head.
	 *
	 * @param docid the docid
	 * @return true, if successful
	 */
	public boolean hasSentHead(String docid){
		DocID id = urlByDocID.get(docid);
		return id.hasSentHead();
	}
	
	
	/**
	 * Sent head.
	 *
	 * @param docid the docid
	 */
	public void sentHead(String docid){
		DocID id = urlByDocID.get(docid);
		id.sentHead();
		urlByDocID.put(id);
	}
	
	/**
	 * Adds the doc url.
	 *
	 * @param docid the docid
	 * @param url the url
	 */
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
	 * @param docid the docid
	 * @param linkid the linkid
	 */
	public void  addDocLink(String docid, String linkid){
		DocLinks idlinks = linksByDocID.get(docid);
		idlinks.addLink(linkid);
		linksByDocID.put(idlinks);
	}
	
	/**
	 * Adds the doc content.
	 *
	 * @param docid the docid
	 * @param content the content
	 * @param url the url
	 */
	public void addDocContent(String docid, String content, String url){
		DocContent idContent = contentByDocID.get(docid);
		idContent.setUrl(url);
		idContent.setContent(content);
		int len = content.length();
		idContent.setContentLength(len);
		
		contentByDocID.put(idContent);
	}
	
	
	// create a new account in the db
	/**
	 * Adds the user.
	 *
	 * @param user the user
	 * @return true, if successful
	 */
	public boolean addUser(User user) {
		if(userByName.contains(user.userName)){
			return false;
		}
		else{
			userByName.put(user);
			return true;
		}
	}
	
	/**
	 * Adds the channel.
	 *
	 * @param channel the channel
	 * @return true, if successful
	 */
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
	/**
	 * Checks for user.
	 *
	 * @param userName the user name
	 * @return true, if successful
	 */
	public boolean hasUser(String userName){
		return userByName.contains(userName);

	}
	
	/**
	 * Checks for channel.
	 *
	 * @param channelName the channel name
	 * @return true, if successful
	 */
	public boolean hasChannel(String channelName){
		if(channelName!=null){
		return channelByName.contains(channelName);
		}
		return false;
	}
	
	/**
	 * Gets the channel owner.
	 *
	 * @param channelName the channel name
	 * @return the channel owner
	 */
	public String getChannelOwner(String channelName){
		if(channelByName.contains(channelName)){
			return channelByName.get(channelName).getUserName();
		}
		return null;
	}
	
	//check if the user name has already existed in the db
	//if existed, return the password
	/**
	 * Check password.
	 *
	 * @param userName the user name
	 * @param input the input
	 * @return true, if successful
	 */
	public boolean checkPassword(String userName, String input){
		if(userByName.contains(userName)){
			return userByName.get(userName).correctPassword(input);
		}
		return false;
	}
	
	
	//return all the channels that belong to a specific user
	/**
	 * All my channels.
	 *
	 * @param userName the user name
	 * @return the array list
	 */
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
	/**
	 * Gets the all channels.
	 *
	 * @return the all channels
	 */
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
	
	/**
	 * Gets the xpaths.
	 *
	 * @param channelName the channel name
	 * @return the xpaths
	 */
	public ArrayList<String> getXpaths(String channelName){
		return channelByName.get(channelName).getXpaths();
	}
	
	/**
	 * Gets the xslt.
	 *
	 * @param channelName the channel name
	 * @return the xslt
	 */
	public String getXslt(String channelName){
		return channelByName.get(channelName).getXsltUrl();
	}
	
	// create a new channel and add it into the user's entity 

	
	/**
	 * Adds the channel to user.
	 *
	 * @param channelName the channel name
	 * @param userName the user name
	 * @return true, if successful
	 */
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
	/**
	 * Delete channel.
	 *
	 * @param chnl the chnl
	 * @param userName the user name
	 * @return true, if successful
	 */
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
	/**
	 * Adds the xpath.
	 *
	 * @param channelName the channel name
	 * @param xpath the xpath
	 * @return true, if successful
	 */
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
	
	/**
	 * Sets the xslt of channel.
	 *
	 * @param url the url
	 * @param channelName the channel name
	 * @return true, if successful
	 */
	public boolean setXsltOfChannel(String url, String channelName){
		Channel channel = channelByName.get(channelName);
		channel.setXsltUrl(url);
		channelByName.put(channel);
		return true;
	}
	
	/**
	 * Adds the url to channel.
	 *
	 * @param url the url
	 * @param channelName the channel name
	 * @return true, if successful
	 */
	public boolean addUrlToChannel(String url, String channelName){
		Channel chnl = channelByName.get(channelName);
		if(!chnl.getFileUrls().contains(url)){
			chnl.addFileUrl(url);
			channelByName.put(chnl);
		}
		return true;
	}
	
	/**
	 * Gets the matched urls.
	 *
	 * @param channelName the channel name
	 * @return the matched urls
	 */
	public ArrayList<String> getMatchedUrls(String channelName){
		Channel chnl = channelByName.get(channelName);
		return chnl.getFileUrls();
	}

	
	
	/**
	 * Removes the url from channel.
	 *
	 * @param url the url
	 * @param channelName the channel name
	 * @return true, if successful
	 */
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
	
//	/**
//	 * add a new host's info of robots.txt to db
//	 * */
//	public void addRobotHost(String host){
//		Robots robot = new Robots(host);
//		robotsByHost.put(robot);
//	}
//	
//	/**
//	 * @param host
//	 * 		  prefix
//	 * 	 	  allow
//	 * add a new robot rule to the host and whether it is 
//	 * an allow or a disallow
//	 * */
//	public void addRobotRule(String host, String prefix, boolean allow){
//		Robots robot = robotsByHost.get(host);
//		robot.addRuleLink(prefix, allow);
//		robotsByHost.put(robot);
//	}
//	
//	public void addRobotDelay(String host, int delay){
//		Robots robot = robotsByHost.get(host);
//		robot.setCrawlDelay(delay);
//		robotsByHost.put(robot);
//	}
//	
//	public boolean containsRobotHost(String host){
//		return robotsByHost.contains(host);
//	}
//	
//	public void updateLstCrawled(String host, long now){
//		Robots robot = robotsByHost.get(host);
//		robot.updateLstCrawled(now);
//		robotsByHost.put(robot);
//	}
//	
//	public boolean robotIsAllowed(String host, String path){
//		Robots robot = robotsByHost.get(host);
//		return robot.isAllowed(path);
//	}
//	
//	public long robotGetLstCrawled(String host){
//		Robots robot = robotsByHost.get(host);
//		return robot.getLstCrawled();
//	}
//	
//	public int robotGetCrawlDelay(String host){
//		Robots robot = robotsByHost.get(host);
//		return robot.getCrawlDelay();
//	}
	
	
	/**
 * Gets the users.
 *
 * @return the users
 */
public PrimaryIndex<String, User> getUsers(){
		return userByName;
	}
	
	/**
	 * Gets the channels.
	 *
	 * @return the channels
	 */
	public PrimaryIndex<String, Channel> getChannels(){
		return channelByName;
	}
	
	/**
	 * Gets the files.
	 *
	 * @return the files
	 */
	public PrimaryIndex<String, RawFile> getFiles(){
		return fileByUrl;
	}
	

	/**
	 * Gets the frontier.
	 *
	 * @return the frontier
	 */
	public PrimaryIndex<String, URLFrontier> getFrontier(){
		return urlFrontier;
	}
	
	// Close the store and the environment
	/**
	 * Close env.
	 */
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
	
	/**
	 * To big integer.
	 *
	 * @param key the key
	 * @return the big integer
	 */
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
