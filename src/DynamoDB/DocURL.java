package DynamoDB;

import java.nio.ByteBuffer;
import java.util.Arrays;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;

import Utils.BinaryUtils;

// TODO: Auto-generated Javadoc
//import com.sun.org.apache.bcel.internal.util.ByteSequence;
/**
 * Object Persistent model, to populate docID-URL table .
 *
 * @author dichenli
 */
@DynamoDBTable(tableName="DocURL")
public class DocURL {
	
	/** The id. */
	byte[] id; //binary data
	
	/** The url. */
	String url;
	
	/** The inserter. */
	static InserterDocURL inserter = new InserterDocURL();
	
	/**
	 * Instantiates a new doc url.
	 */
	DocURL() {
		id = new byte[20];
	}
	
	/**
	 * Instantiates a new doc url.
	 *
	 * @param url the url
	 * @param decimalDocID the decimal doc id
	 */
	DocURL(String url, String decimalDocID) {
		this.url = url;
		this.id = BinaryUtils.fromDecimal(decimalDocID);
	}
	
	/**
	 * Gets the id.
	 *
	 * @return the id
	 */
	@DynamoDBHashKey(attributeName="id")
    public ByteBuffer getId() { return ByteBuffer.wrap(id); }
	
    /**
     * Sets the id.
     *
     * @param buf the new id
     */
    public void setId(ByteBuffer buf) { 
    	this.id = buf.array(); 
    }
    
    /**
     * Sets the id.
     *
     * @param hexString the new id
     */
    public void setId(String hexString) {
    	id = BinaryUtils.fromDecimal(hexString);
    }
    
    /**
     * Gets the url.
     *
     * @return the url
     */
    @DynamoDBAttribute(attributeName="url")
    public String getURL() { return url; }    
    
    /**
     * Sets the url.
     *
     * @param url the new url
     */
    public void setURL(String url) { this.url = url; }
    
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
       return url;  
    }
    
   
    
    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
	public boolean equals(Object other) {
		if(other == null || !(this.getClass().equals(other.getClass()))) {
			return false;
		}
		DocURL other2 = (DocURL) other;
		return this.url.equals(other2.url);
	}
    
    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
    	return url.hashCode();
    };
    
    /**
     * Parses the input.
     *
     * @param line the line
     * @return the doc url
     */
    public static DocURL parseInput(String line) {
		if(line == null) {
			System.out.println("null line");
			return null;
		}
		
		String[] splited = line.split("\t");
		if(splited == null || splited.length != 2) {
			System.out.println("bad line: " + line);
			return null;
		}
		String docID = splited[0];
		String url = splited[1];
		if(docID.equals("") || url.equals("")) {
			System.out.println("empty content: " + line);
			return null;
		}
		
		DocURL item = new DocURL();
		item.setId(docID);
		item.setURL(url);
		return item;
	}
    
    /**
     * Load from byte buffer.
     *
     * @param bytes the bytes
     * @return the doc url
     * @throws Exception the exception
     */
    public static DocURL loadFromByteBuffer(ByteBuffer bytes) throws Exception {
    	return load(bytes.array());
    }
    
    /**
     * Load from decimal string.
     *
     * @param hexStr the hex str
     * @return the doc url
     * @throws Exception the exception
     */
    public static DocURL loadFromDecimalString(String hexStr) throws Exception {
    	return load(BinaryUtils.fromDecimal(hexStr));
    }
    
    /**
     * Load.
     *
     * @param id the id
     * @return the doc url
     * @throws Exception the exception
     */
    public static DocURL load(byte[] id) throws Exception {
    	if (DynamoTable.mapper == null) {
    		DynamoTable.init();
    	}
    	return DynamoTable.mapper.load(DocURL.class, ByteBuffer.wrap(id));
    }
    
    /**
     * Insert.
     *
     * @param item the item
     * @param insertNow the insert now
     */
    public static void insert(DocURL item, boolean insertNow) {
    	if (DynamoTable.mapper == null) {
    		try {
				DynamoTable.init();
			} catch (Exception e) {
				e.printStackTrace();
			}
    	}
    	inserter.insert(item, insertNow);
    }
    
    /**
     * Flush.
     */
    public static void flush() {
		inserter.flush();
	}
    
    /**
     * insert a docID-url pair to DB, docID is the decimal String representation of it .
     *
     * @param url the url
     * @param decimalDocID the decimal doc id
     * @param insertNow if true, the insertion to DB is conducted immediately.
     * if false, the insertion is batched until 25 items have been accumulated
     */
    public static void insert(String url, String decimalDocID, boolean insertNow) {
    	DocURL item = new DocURL(url, decimalDocID);
    	insert(item, insertNow);
    }
}
