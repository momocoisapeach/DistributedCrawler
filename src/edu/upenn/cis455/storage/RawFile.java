package edu.upenn.cis455.storage;


import java.util.Date;

import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;

// TODO: Auto-generated Javadoc
/**
 * The Class RawFile.
 */
@Entity
public class RawFile {
	
	/** The file url. */
	@PrimaryKey
	String fileUrl;
	
	/** The file. */
	private String file;
	
	/** The lst time. */
	private Date lstTime;
	
	/**
	 * Instantiates a new raw file.
	 *
	 * @param url the url
	 */
	public RawFile(String url){
		fileUrl = url;
	}
	
	/**
	 * Instantiates a new raw file.
	 */
	private RawFile(){}
	

	
	/**
	 * Sets the file url.
	 *
	 * @param url the new file url
	 */
	public void setFileUrl(String url){
		fileUrl = url;
	}
	
	/**
	 * Update time.
	 */
	public void updateTime(){
//		DateFormat dateFormat = new SimpleDateFormat("YYYY-MM-ddThh:mm:ss");
		Date now = new Date();
		lstTime = now;
	}
	
	/**
	 * Sets the file.
	 *
	 * @param file the new file
	 */
	public void setFile(String file){
		this.file = file;
	}
	
	/**
	 * Gets the time.
	 *
	 * @return the time
	 */
	public Date getTime(){
		return lstTime;
	}
	
	/**
	 * Gets the file.
	 *
	 * @return the file
	 */
	public String getFile(){
		return file;
	}

}
