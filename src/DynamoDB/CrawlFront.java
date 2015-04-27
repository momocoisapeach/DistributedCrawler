package DynamoDB;

import java.util.HashMap;
import java.util.List;

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
	
	String url;
	Integer crawler;
	
	public CrawlFront() {} //called by DynamoDB
	
	public CrawlFront(String url, Integer crawler) {
		this.url = url;
		this.crawler = crawler;
	}
	
	@DynamoDBHashKey(attributeName="crawler")
	public Integer getCrawler() {
		return crawler;
	}
	public void setCrawler(Integer crawler) {
		this.crawler = crawler;
	}
	
	@DynamoDBRangeKey(attributeName="url")
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	
	public static void insert(CrawlFront item, boolean insertNow) {
		DynamoTable.insert(item, insertNow);
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
		if (DynamoTable.mapper == null) {
			try {
				DynamoTable.init();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		if (!Tables.doesTableExist(DynamoTable.dynamoDB, tableName)) {
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
				ScalarAttributeType.S, 10, 10);
		
		try {
			DynamoTable.createTable(tableName, createTableRequest);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
}
