package DynamoDB;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Random;


import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBRangeKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;
import com.amazonaws.services.dynamodbv2.model.CreateTableRequest;
import com.amazonaws.services.dynamodbv2.model.ScalarAttributeType;
import com.amazonaws.services.dynamodbv2.util.Tables;

/**
 * DynamoDB model for a crawlers' front
 * @author dichenli
 *
 */
@DynamoDBTable(tableName="CrawlFront")
public class CrawlFront {
	
	public static String tableName = "CrawlFront";
	static InserterCrawlFront inserter = new InserterCrawlFront();
	static Random rand = new Random();
	static boolean tableExists = false; //if table has been created, set to true
	
	String url;
	Integer crawler;
	Long timestamp; 
	
	public CrawlFront() {} //called by DynamoDB
	
	@DynamoDBHashKey(attributeName="crawler")
	public Integer getCrawler() {
		System.out.println("getCrawler: " + crawler);
		return crawler;
	}
	public void setCrawler(Integer crawler) {
		this.crawler = crawler;
	}
	
	@DynamoDBRangeKey(attributeName="timestamp")
	public Long getTimestamp() {
		System.out.println("getTimestamp: " + timestamp);
		return timestamp;
	}
	public void setTimestamp(Long time) {
		System.out.println("setTimestamp: " + time);
		this.timestamp = time;
	}
	
	@DynamoDBAttribute(attributeName="url")
	public String getUrl() {
		System.out.println("geturl " + url);
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	
	
	public static long timeRand() {
		return new Date().getTime() * 10000 + rand.nextInt(10000);
	}
	
	public CrawlFront(String url, Integer crawler) {
		this.url = url;
		this.crawler = crawler;
		this.timestamp = timeRand(); 
	}
	
	public void setTimestampRand() {
		this.timestamp = timeRand();
	}
	
	@Override
	public boolean equals(Object other) {
		if(other == null || !(this.getClass().equals(other.getClass()))) {
			return false;
		}
		
		CrawlFront other2 = (CrawlFront) other;
		return this.timestamp == other2.timestamp && this.crawler == other2.crawler;
	}
	
	public static void insert(String url, int crawler, boolean insertNow) {
		inserter.insert(new CrawlFront(url, crawler), insertNow);
	}
	
	public static void insert(CrawlFront item, boolean insertNow) {
		inserter.insert(item, insertNow);
	}
	
	public static void flush() {
		inserter.flush();
	}
	
	@Override
	public String toString() {
		return crawler + "\t" + timestamp.toString() + "\t" +  url;
	}
	
	private static HashMap<Integer, List<CrawlFront>> queryResults = new HashMap<Integer, List<CrawlFront>>();
	/**
	 * get one item from DB with given crawler. returns null if no results were found
	 * (in which case, the crawler can sleep for a little while, then wake up and try again)
	 * @param crawler
	 * @return an item of crawl front
	 */
	public static CrawlFront pop(Integer crawler) {
		List<CrawlFront> results = queryResults.get(crawler);
		if(results == null || results.isEmpty()) {
			results = queryPage(crawler);
		}
		if(results == null || results.isEmpty()) { //no more available results
			return null;
		}
		CrawlFront item = results.remove(results.size() - 1);
		delete(item);
		queryResults.put(crawler, results);
		return item;
	}
	
	/**
	 * get one item from DB with given crawler. returns null if no results were found
	 * (in which case, the crawler can sleep for a little while, then wake up and try again)
	 * @param crawler
	 * @return a url to be crawled
	 */
	public static String popUrl(Integer crawler) {
		CrawlFront crawlFront = pop(crawler);
		if(crawlFront == null) {
			return null;
		}
		return crawlFront.getUrl();
	}
	
	/**
	 * delete given item from DB
	 * @param item
	 */
	public static void delete(CrawlFront item) {
		DynamoTable.mapper.delete(item);
	}
	
	/**
	 * get a page (100 results at most) of crawled results.
	 * if no result is available, it returns an empty list
	 * @param crawler
	 * @return
	 */
	public static List<CrawlFront> queryPage(Integer crawler) {
		if(!tableExists && !DynamoTable.checkTableExists(tableName)) {
			creatTable();
		}

		CrawlFront crawlFront = new CrawlFront();
		crawlFront.setCrawler(crawler);
		DynamoDBQueryExpression<CrawlFront> queryExpression 
		= new DynamoDBQueryExpression<CrawlFront>().withHashKeyValues(crawlFront);

		List<CrawlFront> collection = DynamoTable.mapper.queryPage(CrawlFront.class, queryExpression).getResults();
		return collection;
	}

	public static void creatTable() {
		DynamoTable.checkTableExists(tableName);
		
		CreateTableRequest createTableRequest 
		= DynamoUtils.createTableHashRange(
				tableName, "crawler", ScalarAttributeType.N, "timestamp", 
				ScalarAttributeType.N, 25, 25);
		
		try {
			DynamoTable.createTable(tableName, createTableRequest);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		tableExists = true;
	}

	
}
