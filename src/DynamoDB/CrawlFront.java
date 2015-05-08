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

// TODO: Auto-generated Javadoc
/**
 * DynamoDB model for a crawlers' front.
 *
 * @author dichenli
 */
@DynamoDBTable(tableName="CrawlFront")
public class CrawlFront {
	
	/** The table name. */
	public static String tableName = "CrawlFront";
	
	/** The inserter. */
	static InserterCrawlFront inserter = new InserterCrawlFront();
	
	/** The rand. */
	static Random rand = new Random();
	
	/** The table exists. */
	static boolean tableExists = false; //if table has been created, set to true
	
	/** The url. */
	String url;
	
	/** The crawler. */
	Integer crawler;
	
	/** The timestamp. */
	Long timestamp; 
	
	/**
	 * Instantiates a new crawl front.
	 */
	public CrawlFront() {} //called by DynamoDB
	
	/**
	 * Gets the crawler.
	 *
	 * @return the crawler
	 */
	@DynamoDBHashKey(attributeName="crawler")
	public Integer getCrawler() {
//		System.out.println("getCrawler: " + crawler);
		return crawler;
	}
	
	/**
	 * Sets the crawler.
	 *
	 * @param crawler the new crawler
	 */
	public void setCrawler(Integer crawler) {
		this.crawler = crawler;
	}
	
	/**
	 * Gets the timestamp.
	 *
	 * @return the timestamp
	 */
	@DynamoDBRangeKey(attributeName="timestamp")
	public Long getTimestamp() {
//		System.out.println("getTimestamp: " + timestamp);
		return timestamp;
	}
	
	/**
	 * Sets the timestamp.
	 *
	 * @param time the new timestamp
	 */
	public void setTimestamp(Long time) {
//		System.out.println("setTimestamp: " + time);
		this.timestamp = time;
	}
	
	/**
	 * Gets the url.
	 *
	 * @return the url
	 */
	@DynamoDBAttribute(attributeName="url")
	public String getUrl() {
//		System.out.println("geturl " + url);
		return url;
	}
	
	/**
	 * Sets the url.
	 *
	 * @param url the new url
	 */
	public void setUrl(String url) {
		this.url = url;
	}
	
	
	/**
	 * Time rand.
	 *
	 * @return the long
	 */
	public static long timeRand() {
		return new Date().getTime() * 10000 + rand.nextInt(10000);
	}
	
	/**
	 * Instantiates a new crawl front.
	 *
	 * @param url the url
	 * @param crawler the crawler
	 */
	public CrawlFront(String url, Integer crawler) {
		this.url = url;
		this.crawler = crawler;
		this.timestamp = timeRand(); 
	}
	
	/**
	 * Sets the timestamp rand.
	 */
	public void setTimestampRand() {
		this.timestamp = timeRand();
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object other) {
		if(other == null || !(this.getClass().equals(other.getClass()))) {
			return false;
		}
		CrawlFront other2 = (CrawlFront) other;
		return this.timestamp == other2.timestamp && this.crawler == other2.crawler;
	}
	
	
	/**
	 * Insert.
	 *
	 * @param url the url
	 * @param crawler the crawler
	 * @param insertNow the insert now
	 */
	public static void insert(String url, int crawler, boolean insertNow) {
		inserter.insert(new CrawlFront(url, crawler), insertNow);
	}
	
	/**
	 * Insert.
	 *
	 * @param item the item
	 * @param insertNow the insert now
	 */
	public static void insert(CrawlFront item, boolean insertNow) {
		inserter.insert(item, insertNow);
	}
	
	/**
	 * Flush.
	 */
	public static void flush() {
		inserter.flush();
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return crawler + "\t" + timestamp.toString() + "\t" +  url;
	}
	
	/** The query results. */
	private static HashMap<Integer, List<CrawlFront>> queryResults = new HashMap<Integer, List<CrawlFront>>();
	
	/**
	 * get one item from DB with given crawler. returns null if no results were found
	 * (in which case, the crawler can sleep for a little while, then wake up and try again)
	 *
	 * @param crawler the crawler
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
	 *
	 * @param crawler the crawler
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
	 * delete given item from DB.
	 *
	 * @param item the item
	 */
	public static void delete(CrawlFront item) {
		DynamoTable.mapper.delete(item);
	}
	
	/**
	 * get a page (100 results at most) of crawled results.
	 * if no result is available, it returns an empty list
	 *
	 * @param crawler the crawler
	 * @return the list
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

	/**
	 * Creat table.
	 */
	public static void creatTable() {
		DynamoTable.checkTableExists(tableName);
		
		CreateTableRequest createTableRequest 
		= DynamoUtils.createTableHashRange(
				tableName, "crawler", ScalarAttributeType.N, "timestamp", 
				ScalarAttributeType.N, 50, 500);
		
		try {
			DynamoTable.createTable(tableName, createTableRequest);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		tableExists = true;
	}

	
}
