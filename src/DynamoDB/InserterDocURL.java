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
public class InserterDocURL {
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
	public void insert(DocURL item, boolean insertNow) {
		if(items == null) {
			items = new ArrayList<CrawlFront>();
		}
		System.out.println("insert to local buffer: " + item.toString());
		items.add(item);
	
		if(insertNow || items.size() >= 24) {
			flush();
		}
	}
	
	public void flush() {
		if(items == null) {
			return;
		}
		List<FailedBatch> failed = batchInsert(items); //if insert failed, print error message
		System.out.println("insert to DB # of items: " + items.size());
		if(failed != null && !failed.isEmpty()) {
			System.out.println("insert error, number of failed: " + failed.size());
			failed.get(0).getException().printStackTrace();
		}
		items = null;
	}
	
	/**
	 * equal to insert(item, false);
	 * must call init() before calling this method
	 * @param item
	 */
	public void insert(DocURL item) {
		insert(item, false);
	}
	
	/**
	 * must call init() before calling this method
	 * @param items
	 * @return
	 */
	public List<FailedBatch> batchInsert(List<DocURL> items) {
		return DynamoTable.mapper.batchSave(items);
	}
	
	/**
	 * upload a single item to DB
	 * @param item: an item of a persistent model with @DynamoDBTable(tableName=XX)
	 * @throws Exception when DB init() failed
	 */
	public void save(DocURL item) throws Exception {
		if(DynamoTable.mapper == null) {
			DynamoTable.init();
		}
		
		DynamoTable.mapper.save(item);
	}
}
