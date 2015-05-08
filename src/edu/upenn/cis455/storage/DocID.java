package edu.upenn.cis455.storage;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigInteger;

import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;

// TODO: Auto-generated Javadoc
/**
 * The Class DocID.
 */
@Entity
public class DocID {


	/** The doc id. */
	@PrimaryKey
	String docID;
	
	/** The url. */
	private String url;
	
	/** The head. */
	private boolean head;
	
	/** The doc id file. */
	private static File docIDFile = new File(Config.DocID_File);
	
	
	/**
	 * constructs a DocID with a doc id String.
	 *
	 * @param docid the docid
	 */
	public DocID(String docid) {
		docID = docid;
		head = false;
	}
	
	/**
	 * Instantiates a new doc id.
	 */
	private DocID(){}
	
	/**
	 * point the docid to the corresponding url.
	 *
	 * @param url the new url
	 */
	public void setUrl(String url){
		this.url = url;
		try {
			writeDocId(url);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	/**
	 * Checks for sent head.
	 *
	 * @return true, if successful
	 */
	public boolean hasSentHead(){
		return head;
	}
	
	/**
	 * Sent head.
	 */
	public void sentHead(){
		head = true;
	}

	/**
	 * return the corresponding url based on the docid.
	 *
	 * @return the url
	 */
	public String getUrl(){
		return url;
	}
	
	/**
	 * Write doc id.
	 *
	 * @param to_doc the to_doc
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	private void writeDocId(String to_doc) throws IOException {
		FileWriter fileWriter = new FileWriter(docIDFile, true);
//		System.out.println(docID+"\t"+to_doc+"\n");
		fileWriter.write(docID+"\t"+to_doc+"\n");
		fileWriter.close();
		
	}
}
