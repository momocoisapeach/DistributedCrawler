/**
 * 
 */
package DynamoDB;

import java.util.ArrayList;
import java.util.List;

/**
 * @author dichenli
 * binded to a class T to insert items in batch manner to DynamoDB, typically a static instance
 * of the class T
 */
public class Inserter<T> {
	private ArrayList<T> items = null;
	/**
	 * insert data to DB. It will batch the insert task, and not send 
	 * the DB request until a total of 25 items
	 * has been sent to insert queue. However, if insertNow is set to true,
	 * it will insert immediately all currently available items in the batch. 
	 * Must call init() before calling this method.
	 * @param item
	 * @param insertNow
	 */
	public void insert(T item, boolean insertNow) {
		if(items == null) {
			items = new ArrayList<T>();
		}
		items.add(item);
		if(insertNow || items.size() >= 25) {
			batchInsert(items);
		}
		items = null;
	}
	
	/**
	 * equal to insert(item, false);
	 * must call init() before calling this method
	 * @param item
	 */
	public void insert(T item) {
		insert(item, false);
	}
	
	/**
	 * must call init() before calling this method
	 * @param items
	 * @return
	 */
	public int batchInsert(List<T> items) {
		return DynamoTable.mapper.batchSave(items).size();
	}
	
	/**
	 * upload a single item to DB
	 * @param item: an item of a persistent model with @DynamoDBTable(tableName=XX)
	 * @throws Exception when DB init() failed
	 */
	public void save(T item) throws Exception {
		if(DynamoTable.mapper == null) {
			DynamoTable.init();
		}
		
		DynamoTable.mapper.save(item);
	}
}
