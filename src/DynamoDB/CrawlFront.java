package DynamoDB;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import Utils.TimeUtils;

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
	static Inserter<CrawlFront> inserter;
	
	String url;
	Integer crawler;
	Date timestamp; //be aware: on DB, date is stored as Long, not Date!!
	
	public CrawlFront() {} //called by DynamoDB
	
	public CrawlFront(String url, Integer crawler) {
		this.url = url;
		this.crawler = crawler;
		this.timestamp = TimeUtils.randomDate(10000); //a date object, with time +- 10 seconds randomly from now
	}
	
	@DynamoDBHashKey(attributeName="crawler")
	public Integer getCrawler() {
		return crawler;
	}
	public void setCrawler(Integer crawler) {
		this.crawler = crawler;
	}
	
	@DynamoDBRangeKey(attributeName="timestamp")
	public Long getTimestamp() {
		return timestamp.getTime();
	}
	public void setTimestamp(Long time) {
		this.timestamp = new Date(time);
	}
	
	public void setTimestamp(Date time) {
		this.timestamp = time;
	}
	
	@DynamoDBAttribute(attributeName="url")
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	
	public static void insert(String url, int crawler, boolean insertNow) {
		inserter.insert(new CrawlFront(url, crawler), insertNow);
	}
	
	public static void insert(CrawlFront item, boolean insertNow) {
		inserter.insert(item, insertNow);
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
		return pop(crawler).getUrl();
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
		if(!DynamoTable.checkTableExists(tableName)) {
			creatTable();
		}

		CrawlFront crawlFront = new CrawlFront();
		crawlFront.setCrawler(crawler);
		DynamoDBQueryExpression<CrawlFront> queryExpression 
		= new DynamoDBQueryExpression<CrawlFront>().withHashKeyValues(crawlFront);

		List<CrawlFront> collection = DynamoTable.mapper.queryPage(CrawlFront.class, queryExpression).getResults();
		return collection;
	}

	static void creatTable() {
		CreateTableRequest createTableRequest 
		= DynamoUtils.createTableHashRange(
				tableName, "crawler", ScalarAttributeType.N, "url", 
				ScalarAttributeType.S, 25, 25);
		
		try {
			DynamoTable.createTable(tableName, createTableRequest);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
}