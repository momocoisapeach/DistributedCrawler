package edu.upenn.cis455.storage;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;


@Entity
public class DocLinks {
	
	@PrimaryKey
	String docID;
	
	private ArrayList<String> docLinks;
	
	private static File docLinksFile = new File("/Users/peach/Documents/upenn/2015spring/cis555/db/db38/links.txt");
	
	
	private DocLinks(){}
	public DocLinks(String docid){
		this.docID = docid;
		docLinks = new ArrayList<String>();
	}
	
	public void addLink(String docid){
		docLinks.add(docid);
		try {
			writeDocId(docid);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void writeDocId(String to_doc) throws IOException {
		FileWriter fileWriter = new FileWriter(docLinksFile, true);
		fileWriter.write(docID+"\t"+to_doc+"\n");
		fileWriter.close();
		
	}

}
