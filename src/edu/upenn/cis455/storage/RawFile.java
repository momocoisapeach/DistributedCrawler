package edu.upenn.cis455.storage;

import java.io.File;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;

@Entity
public class RawFile {
	
	@PrimaryKey
	String fileUrl;
	
	private String file;
	private Date lstTime;
	
	public RawFile(String url){
		fileUrl = url;
	}
	private RawFile(){}
	

	
	public void setFileUrl(String url){
		fileUrl = url;
	}
	
	public void updateTime(){
//		DateFormat dateFormat = new SimpleDateFormat("YYYY-MM-ddThh:mm:ss");
		Date now = new Date();
		lstTime = now;
	}
	
	public void setFile(String file){
		this.file = file;
	}
	
	public Date getTime(){
		return lstTime;
	}
	
	public String getFile(){
		return file;
	}

}
