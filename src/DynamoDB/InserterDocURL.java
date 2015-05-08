/**
 * 
 */
package DynamoDB;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper.FailedBatch;

// TODO: Auto-generated Javadoc
/**
 * The Class InserterDocURL.
 *
 * @author dichenli
 * binded to a class T to insert items in batch manner to DynamoDB, typically a static instance
 * of the class T
 */
public class InserterDocURL {
	
	/** The items. */
	private Set<DocURL> items = null;
	
	/**
	 * insert data to DB. It will batch the insert task, and not send 
	 * the DB request until a total of 25 items
	 * has been sent to insert queue. However, if insertNow is set to true,
	 * it will insert immediately all currently available items in the batch. 
	 * Must call init() before calling this method.
	 *
	 * @param item the item
	 * @param insertNow the insert now
	 */
	public void insert(DocURL item, boolean insertNow) {
		if(items == null) {
			items = new HashSet<DocURL>();
		}
//		System.out.println("insert to local buffer: " + item.toString());
		items.add(item);
	
		if(insertNow || items.size() >= 24) {
			flush();
		}
	}
	
	/**
	 * Flush.
	 */
	public void flush() {
		if (items == null) {
			return;
		}
		ArrayList<DocURL> list = new ArrayList<DocURL>();
		list.addAll(items);
		
		List<FailedBatch> failed = batchInsert(list); //if insert failed, print error message
//		System.out.println("insert to DB # of items: " + list.size());
		if(failed != null && !failed.isEmpty()) {
//			System.out.println("insert error, number of failed: " + failed.size());
			failed.get(0).getException().printStackTrace();
//			for(FailedBatch f : failed) {
//				save(f.);
//			}
		}
		items = null;
	}
	
	/**
	 * equal to insert(item, false);
	 * must call init() before calling this method.
	 *
	 * @param item the item
	 */
	public void insert(DocURL item) {
		insert(item, false);
	}
	
	/**
	 * must call init() before calling this method.
	 *
	 * @param items the items
	 * @return the list
	 */
	public List<FailedBatch> batchInsert(List<DocURL> items) {
		return DynamoTable.mapper.batchSave(items);
	}
	
	/**
	 * upload a single item to DB.
	 *
	 * @param item the item
	 * @throws Exception when DB init() failed
	 */
	public void save(DocURL item) throws Exception {
		if(DynamoTable.mapper == null) {
			DynamoTable.init();
		}
		
		DynamoTable.mapper.save(item);
	}
}
