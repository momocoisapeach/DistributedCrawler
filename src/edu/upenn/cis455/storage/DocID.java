package edu.upenn.cis455.storage;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigInteger;

import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;

@Entity
public class DocID {


	@PrimaryKey
	String docID;
	
	private String url;
	private boolean head;
	
	private static File docIDFile = new File(Config.DocID_File);
	
	
	/**
	 * constructs a DocID with a doc id String
	 * 
	 * */
	public DocID(String docid) {
		docID = docid;
		head = false;
	}
	
	private DocID(){}
	
	/**
	 * point the docid to the corresponding url
	 * */
	public void setUrl(String url){
		this.url = url;
		try {
			writeDocId(url);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public boolean hasSentHead(){
		return head;
	}
	
	public void sentHead(){
		head = true;
	}

	/**
	 * return the corresponding url based on the docid
	 * */
	public String getUrl(){
		return url;
	}
	
	private void writeDocId(String to_doc) throws IOException {
		FileWriter fileWriter = new FileWriter(docIDFile, true);
		System.out.println(docID+"\t"+to_doc+"\n");
		fileWriter.write(docID+"\t"+to_doc+"\n");
		fileWriter.close();
		
	}
}
