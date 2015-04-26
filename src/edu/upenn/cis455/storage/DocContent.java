package edu.upenn.cis455.storage;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;


@Entity
public class DocContent {
	
	@PrimaryKey
	String docID;
	
	private String content;
	private String url;
	private int contentLength;
	
	private String path = "/Users/peach/Documents/upenn/2015spring/cis555/db/db38/content/";
	
	public DocContent(String docid){
		this.docID = docid;
		this.content = null;
		this.url = null;
		this.contentLength = 0;
	}
	
	private DocContent(){}
	
	public void setContent(String content){
		this.content = content;
		try {
			writeDocId(content);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void setUrl(String url){
		this.url = url;
	}
	
	public boolean contentIsEmpty(){
		return content == null;
	}
	
	public void setContentLength(int n){
		this.contentLength = n;
	}

	private void writeDocId(String content) throws IOException {
		File docContentFile = new File(path+docID);
		FileWriter fileWriter = new FileWriter(docContentFile, true);
		fileWriter.write(url+"\n"+content+"\n");
		fileWriter.close();
		
	}
	

}
