package edu.upenn.cis455.crawler;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Formatter;
import java.util.HashMap;
import java.util.Scanner;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.validator.UrlValidator;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.w3c.tidy.Tidy;

import DynamoDB.CrawlFront;
import DynamoDB.DocURL;
import DynamoDB.DynamoTable;

import com.cybozu.labs.langdetect.Detector;
import com.cybozu.labs.langdetect.DetectorFactory;
import com.cybozu.labs.langdetect.LangDetectException;
import com.cybozu.labs.langdetect.Language;

import edu.upenn.cis455.crawler.info.RobotsTxtInfo;
import edu.upenn.cis455.crawler.info.URLInfo;
import edu.upenn.cis455.storage.Channel;
import edu.upenn.cis455.storage.Config;
import edu.upenn.cis455.storage.DBWrapper;
import edu.upenn.cis455.storage.RawFile;
import edu.upenn.cis455.storage.User;
import edu.upenn.cis455.xpathengine.XPathEngineFactory;
import edu.upenn.cis455.xpathengine.XPathEngineImpl;



public class XPathCrawler {
	
	static int crawler = 1;
	int numCrawlers = 1;
	static String directory = null;
	double maxSize;
	String startUrl;
	int maxFileN;
	DBWrapper db;
//	RobotsTxtInfo robots;
	HashMap<Integer, BigInteger> range = new HashMap<Integer, BigInteger>();
	private final static Charset ENCODING = StandardCharsets.UTF_8;
	private String MapReduceInput;
	private String inputFileName;
	int count = 0;
	int line = 0;

	
	public static void main(String[] args){
//		args[0] = "https://dbappserv.cis.upenn.edu/crawltest.html";
//		args[1] = berkeleyDB root directory
//		args[2] = max size of file (in the unit of MB)
//		args[3] = max file #
//		args[4] = crawler #
//		args[5] = total crawler #
		
		if(args.length <3 || args.length>7){
			usage();
		}
		else{
			XPathCrawler crawler = new XPathCrawler();
			crawler.initialize(args);
			
			
			
		}
		
	}
	
	public void initialize(String[] args){
		startUrl = args[0];
		directory = args[1];
		maxSize = Double.parseDouble(args[2]);
		if(args.length >= 4){
			maxFileN = Integer.parseInt(args[3]);
			crawler = Integer.parseInt(args[4]);
			numCrawlers = Integer.parseInt(args[5]);
			setCrawlerRoot();
			
		}
		db = new DBWrapper(directory);
//		robots = new RobotsTxtInfo();
		MapReduceInput = Config.MapReduce_Input;
		
		setHashRange(numCrawlers);
		File dir = new File(directory+"/profiles/");
		try {
			DetectorFactory.loadProfile(dir);
		} catch (LangDetectException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		Channel rss = new Channel("RSS aggregator1");
		User admin = new User("admin");
		admin.setPassword("admin");
		rss.setUserName("admin");
		rss.addXpath("/rss/channel/item/title[contains(text(),\"war\")]");
		rss.addXpath("/rss/channel/item/title[contains(text(),\"peace\")]");
		rss.addXpath("/rss/channel/item/description[contains(text(),\"war\")]");
		rss.addXpath("/rss/channel/item/description[contains(text(),\"peace\")]");
		db.addUser(admin);
		
		db.addChannel(rss);
		db.addChannelToUser("RSS aggregator", "admin");

		db.initialLizeUrlQ();
//		db.initialLizeUrlQ(args[0]);
//		db.addUrlToQueue("https://www.yahoo.com/");
//		db.addUrlToQueue("http://www.msn.com/");
//		db.addUrlToQueue("http://www.aol.com/");
		
		String seed = args[0];
		String seedid = String.valueOf(toBigInteger(seed));
		System.out.println("before inserting seed into db\nand the seed is "+seed+"\nand id is "+seedid);
		
		CrawlFront.creatTable();
		DocURL.insert(seed, seedid, true);
		URL url;
		try {
			url = new URL(seed);
			int writeTo = hash(toBigInteger(url.getHost()));
			System.out.println("hash to crawler "+writeTo);
			CrawlFront.insert(seed, writeTo, true);
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
		System.out.println("after inserting seed...");
		
//		String url = CrawlFront.popUrl(crawler);
//		System.out.println("pop url: "+url);
		int i = 0;
		while(count<maxFileN){
			if(db.isQEmpty()){
				
				System.out.println("empty!!!");
				readFromDynamoDB();
			}
			else{
				String currentUrl = db.getFstUrlFromQ();
	
				System.out.println("\n"+i+"th url: "+currentUrl+"\n");
				run(currentUrl);
				i++;
			}

		}
		db.closeEnv();
	}


	
	

	private void readFromDynamoDB() {
//		System.out.println("reading from dynamo db...");
		int count = 0;
		while(count <=100){
//			System.out.println("count = "+count);
			String url = CrawlFront.popUrl(crawler);

//			System.out.println("after pop url... and the url is"+url+"@@@");
			if(url == null && count == 0){
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			if (url == null && count >= 1){
				break;
			}
			else if(url!=null){
//				System.out.println("url is not null");
				String linkid = String.valueOf(toBigInteger(url));
		  	  	if(!db.containsUrl(linkid)){
//		  	  		System.out.println("db does not contain this url");
  					db.addDocID(linkid);
  					db.addDocUrl(linkid, url);
  					db.addUrlToQueue(url);
  					count++;
//  					System.out.println("count is "+count);
	  					
	  			}
		  	  	else{
//		  	  		System.out.println("db contains this url");
		  	  	}
			}
		}
		
	}

	public void run(String currentUrl) {
		NewHttpClient client = new NewHttpClient(currentUrl);
		NewHttpClient getClient = new NewHttpClient(currentUrl);
		String host = client.getHostName();
		String path = client.getPath();
		String protocol = client.getProtocol();
		String type = "", body = null;
		String docid = null;
		int len = -1;
		int port = client.getPort();
		int statusCode = -1;
		long lastModified;
		long lastCrawled;
		boolean english = true;
		//construct a doc id for this url
		docid = String.valueOf(toBigInteger(currentUrl));
//		System.out.println("doc id is "+docid);
		if(!db.containsUrl(docid)){
//			System.out.println("db does not contain this doc id");
			
			db.addDocID(docid);
			db.addDocUrl(docid, currentUrl);
			
			
		}
		else{
//			System.out.println("*****************\ndb contains this doc id!\n**************");
		}
		
		if(!db.containsRobotHost(host)){
			
//			System.out.println("no robots info about this host:"+host);
			String robot_url = protocol+"://"+host+"/robots.txt";
			NewHttpClient robotClient = new NewHttpClient(robot_url);
			int robot_sc = -1;
			robotClient.setRequestMethod("GET");
			robot_sc = robotClient.executeMethod();
			

			
				if(robot_sc!=200){
//					System.out.println("failed when retrieving robots.txt!");
				}
				
				else{
					db.addRobotHost(host);
					long now = Calendar.getInstance().getTimeInMillis();
					db.updateLstCrawled(host, now);

					String robotBody = null;
					try {
						robotBody = robotClient.getResponseBody();
					} catch (Exception e) {
					
						e.printStackTrace();
					}
					processRobotTxt(host, robotBody);
				} 
			
				if(robot_sc != 1000){
		    		try {
						robotClient.releaseConnection();
//						System.out.println("close robots client connection!");
					} catch (IOException e) {
						
						e.printStackTrace();
					}
		    	}
			
			
			
			
		}
		
		if(db.robotIsAllowed(host, path)){
//			System.out.println("robots allowed url!");
			if(!db.hasSentHead(docid)){
//				System.out.println("head not being sent yet");
				try {
					client.setRequestMethod("HEAD");
					statusCode = client.executeMethod();
					long now = Calendar.getInstance().getTimeInMillis();
					db.updateLstCrawled(host, now);
//					System.out.println("head request code is "+statusCode);
					
					
					if(statusCode == 200){
						db.sentHead(docid);
						type = client.getContentType();
						len = client.getContentLen();
//						System.out.println("head request success: content_type is "+type
//								+"\ncontent_length is "+len/1024.0/1024.0+"MB");
					}
	//				else if(statusCode == 301 || statusCode == 302){
	//					System.out.println("moved to "+client.getRedirectLocation());
	//					
	//					db.addUrlToQueue(client.getRedirectLocation());
	//				}
					if(statusCode != 1000){
//						System.out.println("head request closed");
							client.releaseConnection();
			    	}
				} catch (IOException e) {
					
					e.printStackTrace();
	//			} catch (InterruptedException e) {
	//				
	//				e.printStackTrace();
				}
			}
			else{
//				System.out.println("has sent head...go ahead to get request..");
				statusCode = 1216;
			}
	    	
			if((statusCode >= 200 && statusCode < 400) || statusCode == 1216){
				
				boolean needDelay = false;
				if(statusCode == 1216 || checkSizeType(type, len) || (statusCode >200 && statusCode <400)){
					int statusCode_get = -1;
					try {	
						lastModified = client.lastModified;
						if(db.needDownload(currentUrl, lastModified)){
//							System.out.println("need download...");
							//TODO delay put it at the end of the url Q
							if(db.containsRobotHost(host)){
								long lst = db.robotGetLstCrawled(host)/1000;
								long now = Calendar.getInstance().getTimeInMillis()/1000;
								int delay = db.robotGetCrawlDelay(host);
								long diff = now - lst;
//								System.out.println("difference between last and now is "+diff+" seconds");
								if(diff < delay){
//									System.out.println("need a delay, adding url to queue..");
									db.addUrlToQueue(currentUrl);
									needDelay = true;
									
								}
								
							}
							
							//TODO
							/**
							 * if no delay is needed, then get request for this url
							 * */
							if(!needDelay){
								getClient.setRequestMethod("GET");
								statusCode_get = getClient.executeMethod();
								long now = Calendar.getInstance().getTimeInMillis();
								db.updateLstCrawled(host, now);
								
//								System.out.println("get request code is "+statusCode_get);
								if(statusCode_get == 200){
//									System.out.println("get request success");
									
									body = getClient.getResponseBody();
									type = getClient.getContentType();
									RawFile tmp = new RawFile("");
									tmp.setFileUrl(currentUrl);
									tmp.setFile(body);
									tmp.updateTime();
									db.addRawFile(tmp);
									
									if(isEnglish(body)){
										english = true;
									}
									else{
										english = false;
									}
									
									
									if(db.contentIsEmpty(docid)){
										if(english){
											count++;
											System.out.println("\n****************\n"+count+" url: "+currentUrl+"downloading...\n");
//										System.out.println("this doc id contains an empty content\nputting content now...");
											db.addDocContent(docid, body, currentUrl);
										}
									}
									
								}
								
	
								
								if(english && (statusCode_get == 200 || (statusCode_get == -1 && db.hasUrlInDB(currentUrl)))){
									
									if(type.equals("text/html")){
//										System.out.println("prepares to extract links...and the type is"+type);
										extractLinks(currentUrl);
									}
								}
							}
						
						
						}
						else{
//							System.out.println("no need to download");
						}
						
					}catch (Exception e) {
						
						e.printStackTrace();
					}
					if(statusCode_get != 1000 && !needDelay){
						try {
							getClient.releaseConnection();
						} catch (IOException e) {
							
							e.printStackTrace();
						}
					}
				}
				else{
//					System.out.println("illegal content or type ");
				}
			}
			else{
//				System.out.println("head request failed");
			}
		}
		else{
//			System.out.println("not allowed url or duplicate url!");
		}
		
		
			
		
	}
	
	public void setDB(DBWrapper db){
		this.db = db;
	}

	public void extractLinks(String currentUrl) {
//		System.out.println("\nin the method of extrac links...\n");
		String docid = String.valueOf(toBigInteger(currentUrl));
//		System.out.println("the current url is"+currentUrl+" and its doc id is"+docid);
		int count = 1;
		String docString = db.getFile(currentUrl);
		String baseUrl = currentUrl.substring(0,currentUrl.lastIndexOf("/")+1);
		Document doc = null;
		doc = Jsoup.parse(docString, baseUrl);
		Elements links = doc.select("a[href]");
		
		for (Element link : links) {
			String url = link.attr("abs:href");
//            System.out.println(" "+url+"  "+link.text());
			String[] schemes = {"http","https"}; // DEFAULT schemes = "http", "https", "ftp"
			UrlValidator urlValidator = new UrlValidator(schemes);
			if (urlValidator.isValid(url)) {
			   
			   if(url.contains("#")){
					url = url.substring(0,url.indexOf("#"));
				}
//			   System.out.println("url is "+url);
	            String linkid = String.valueOf(toBigInteger(url));

	            
//	            System.out.println("the current link is"+url+" and its doc id is"+linkid);
	            
//	            System.out.println("writing "+count +"th link into db..");
				writeToDynamoDB(linkid, url);
				count++;
	            
	            
//				if(!db.containsUrl(linkid)){
////					System.out.println("db does not contain this doc id");
//					db.addDocID(linkid);
//					db.addDocUrl(linkid, url);
//					db.addUrlToQueue(url);
//					
//				}
				db.addDocLink(docid, linkid);
			} else {
//			   System.out.println("url is invalid");
			}
			
			
            
        }
		
		CrawlFront.flush();
		DocURL.flush();
		
		
		
		
	}
	
	
	public int hash(BigInteger num) {
		int res = -1;
		for (int i = 0; i < range.size(); i++) {
			if (num.compareTo(range.get(i)) < 0)
				return i;
		}
		return res;
	}
	
	private void setHashRange(int numCrawlers) {
//		  System.out.println("in the set hash range method");
		StringBuilder max = new StringBuilder("");
		String num = String.valueOf(numCrawlers);
		HashMap<Integer, BigInteger> range = new HashMap<Integer, BigInteger>();
		for(int i = 0; i <40; i++){
			max.append("F");
		}
		BigInteger maxB = new BigInteger(max.toString(),16);
		BigInteger interval = maxB.divide(new BigInteger(num,16));
		BigInteger current = new BigInteger("0", 16);
		for(int i = 0; i < numCrawlers; i++){
			current = current.add(interval);
			range.put(i, current);
		}
		this.range = range;
		
	}
	
	


	private void writeToDynamoDB(String linkid, String url) {
//		System.out.println("before inserting doc id and url into dynamo db...");
//		System.out.println("url is "+url+"\n and the docid is"+linkid+"\nand the crawler # is"+crawler);

		DocURL.insert(url, linkid, false);
		try {
			URL u = new URL(url);
			int writeTo = hash(toBigInteger(u.getHost()));

//			System.out.println("hash to crawler "+writeTo);

			CrawlFront.insert(url, writeTo, false);
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
//		CrawlFront.insert(url, crawler, true);
//		System.out.println("after inserting..");

		
	}

	public boolean isEnglish(String text) throws Exception{

		String after = html2text(text);
//		System.out.println(after);
		Detector detector = DetectorFactory.create();
        detector.append(after);
        System.out.println(detector.detect());
        if(detector.detect().equals("en")){
        	return true;
        }
        return false;
	}

	private String html2text(String content) {
		return Jsoup.parse(content).text();
	}

	public void setMaxSize(Double size){
		maxSize = size;
	}
	
	public boolean checkSizeType(String type, int len) {
		String content_type = type;
		int content_length = len;
		boolean tp = false;
		if(content_type == null) return false;
		else if(content_type.equals("text/html") || content_type.contains("xml")){
			tp = true;
		}
		if(content_length/1024.0/1024.0 > maxSize) return false;
		return tp;
	}

	public void processRobotTxt(String host, String body) {
//		System.out.println("processing robots info....");
		int cisCrawlerIdx = body.indexOf("User-agent: cis455crawler");
		int starIdx = body.indexOf("User-agent: *");
		StringBuilder sb = new StringBuilder();
		if(cisCrawlerIdx!= -1){
			body = body.substring(cisCrawlerIdx);
			processHelper(body, host);
		}
		else if(starIdx != -1){
//			System.out.println("star index!");
			body = body.substring(starIdx);
			processHelper(body, host);
		}
		
		
	}

	public void processHelper(String body, String host) {
//		System.out.println("in the method of process helper");
//		System.out.println(body);
		String[] allLines = body.split("\n");
		for(int i = 0; i <allLines.length; i++){
//			System.out.println("line is "+allLines[i]);
			if(allLines[i].length() == 0) break;
			String[] temp = allLines[i].split(": ");
			if(temp[0].equals("Allow")){
				if(temp.length >1){
					db.addRobotRule(host, temp[1], true);
				}
				
			}
			else if(temp[0].equals("Disallow")){
				if(temp.length>1){
					db.addRobotRule(host, temp[1], false);
				}
				
			}
			else if(temp[0].equals("Crawl-delay")){
//				System.out.println("crawl-delay is@"+temp[1]+"@");
				if(temp[1] != null){
					db.addRobotDelay(host, Integer.parseInt(temp[1].trim()));
				}
			}
		}
		
	}
	
//	private void writeToInput(String linkid, String url) throws IOException {
//
//		if(line == 0){
//			Calendar now = Calendar.getInstance();
//			long millsec = now.getTimeInMillis();
//			inputFileName = String.valueOf(millsec);
//			MapReduceInput = MapReduceInput+inputFileName;			
//		}
//		if(line > 100){
//			File input = new File(MapReduceInput);
//			File newFile = new File(MapReduceInput+".txt");
//			input.renameTo(newFile);
//			Calendar now = Calendar.getInstance();
//			long millsec = now.getTimeInMillis();
//			inputFileName = String.valueOf(millsec);
//			MapReduceInput = MapReduceInput+inputFileName;
//			line = 0;
//		}
//		FileWriter fileWriter = new FileWriter(MapReduceInput, true);
//		fileWriter.write(linkid+"\t"+url+"\n");
//		fileWriter.close();
//		
//		line++;
//	}
//	
//	private void readFromOutput() {
//		while(db.isQEmpty()){
//			File output = new File(Config.MapReduce_Output);
//			ArrayList<String> files = listFilesForFolder(output);
//			if(files.isEmpty()){
//				try {
//					Thread.sleep(1000);
//				} catch (InterruptedException e) {
//					e.printStackTrace();
//				}
//				
//			}
//			else{
//				int size = files.size();
//				for(int i = 0; i < size; i++){
//					Path path = Paths.get(files.get(i));
//					try {
//						writeToDB(path);
//						Files.delete(path);
//					} catch (Exception e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					}
//					
//					
//					
//				}
//				
//			}
//			
//		}
//		
//	}
//	
//
//	
//	private void writeToDB(Path path) throws Exception {
//		
//		String lines[];
//		String url, linkid;
//	    try (Scanner scanner =  new Scanner(path, ENCODING.name())){
//	      while (scanner.hasNextLine()){
//	        //process each line in some way
//	    	  lines=scanner.nextLine().split("\t");
//	    	  linkid = lines[0];
//	    	  url = lines[1];
//	    	 
//	    	  if(!db.containsUrl(linkid)){
//		//			System.out.println("db does not contain this doc id");
//					db.addDocID(linkid);
//					db.addDocUrl(linkid, url);
//					db.addUrlToQueue(url);
//					
//				}
//	    	  
//	    	  
//	      }      
//	    }
//	    
//	}
	
	public ArrayList<String> listFilesForFolder(File folder) {
		ArrayList<String> files = new ArrayList<String>();
	    for (File fileEntry : folder.listFiles()) {
	        if (fileEntry.isDirectory()) {
//	            listFilesForFolder(fileEntry);
	        } else {
	        	if(fileEntry.getName().endsWith(".txt")){
	        		files.add(fileEntry.getName());
	        	}
	        }
	    }
	    return files;
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
	
	public static void setCrawlerRoot(){
		Config.init(directory);
	}

	private static void usage() {
		System.out.println("Please input the following arguments:\n"
				+ "the starting url for crawling\n"
				+ "the directory that holds your db store\n"
				+ "The maximum size, in megabytes, of a document to be retrieved from a Web server\n"
				+ "(optional)the number of files (HTML and XML) to retrieve before stopping\n");
		
	}
}
