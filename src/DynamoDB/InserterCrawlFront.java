/**
 * 
 */
package DynamoDB;

import java.util.ArrayList;
import java.util.List;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper.FailedBatch;

/**
 * @author dichenli
 * binded to a class T to insert items in batch manner to DynamoDB, typically a static instance
 * of the class T
 */
public class InserterCrawlFront {
	private ArrayList items = null;
	/**
	 * insert data to DB. It will batch the insert task, and not send 
	 * the DB request until a total of 25 items
	 * has been sent to insert queue. However, if insertNow is set to true,
	 * it will insert immediately all currently available items in the batch. 
	 * Must call init() before calling this method.
	 * @param item
	 * @param insertNow
	 */
	public void insert(CrawlFront item, boolean insertNow) {
		try {
			save(item);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
//		if(items == null) {
//			items = new ArrayList<CrawlFront>();
//		}
//		System.out.println("insert to local buffer: " + item.toString());
//		items.add(item);
//	
//		if(insertNow || items.size() >= 24) {
//			List<FailedBatch> failed = batchInsert(items); //if insert failed, print error message
//			System.out.println("insert to DB # of items: " + items.size());
//			if(failed != null && !failed.isEmpty()) {
//				System.out.println("insert error, number of failed: " + failed.size());
//				failed.get(0).getException().printStackTrace();
//			}
//			items = null;
//		}
	}
	
	/**
	 * equal to insert(item, false);
	 * must call init() before calling this method
	 * @param item
	 */
	public void insert(CrawlFront item) {
		insert(item, false);
	}
	
	/**
	 * must call init() before calling this method
	 * @param items
	 * @return
	 */
	public List<FailedBatch> batchInsert(List<CrawlFront> items) {
		return DynamoTable.mapper.batchSave(items);
	}
	
	/**
	 * upload a single item to DB
	 * @param item: an item of a persistent model with @DynamoDBTable(tableName=XX)
	 * @throws Exception when DB init() failed
	 */
	public void save(CrawlFront item) throws Exception {
		if(DynamoTable.mapper == null) {
			DynamoTable.init();
		}
		System.out.println("mapper.save: " +item);
		DynamoTable.mapper.save(item);
	}
}