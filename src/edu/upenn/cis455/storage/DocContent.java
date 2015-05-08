package edu.upenn.cis455.storage;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;


// TODO: Auto-generated Javadoc
/**
 * The Class DocContent.
 */
@Entity
public class DocContent {
	
	/** The doc id. */
	@PrimaryKey
	String docID;
	
	/** The content. */
	private String content;
	
	/** The url. */
	private String url;
	
	/** The content length. */
	private int contentLength;
	
	/** The path. */
	private String path = Config.DocContent_File;
	
	/**
	 * Instantiates a new doc content.
	 *
	 * @param docid the docid
	 */
	public DocContent(String docid){
		this.docID = docid;
		this.content = null;
		this.url = null;
		this.contentLength = 0;
	}
	
	/**
	 * Instantiates a new doc content.
	 */
	private DocContent(){}
	
	/**
	 * Sets the content.
	 *
	 * @param content the new content
	 */
	public void setContent(String content){
		this.content = content;
		try {
			writeDocId(content);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Sets the url.
	 *
	 * @param url the new url
	 */
	public void setUrl(String url){
		this.url = url;
	}
	
	/**
	 * Content is empty.
	 *
	 * @return true, if successful
	 */
	public boolean contentIsEmpty(){
		return content == null;
	}
	
	/**
	 * Sets the content length.
	 *
	 * @param n the new content length
	 */
	public void setContentLength(int n){
		this.contentLength = n;
	}

	/**
	 * Write doc id.
	 *
	 * @param content the content
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	private void writeDocId(String content) throws IOException {
		File docContentFile = new File(path+docID);
		FileWriter fileWriter = new FileWriter(docContentFile, true);
		fileWriter.write(url+"\n"+content+"\n");
		fileWriter.close();
		
	}
	

}
