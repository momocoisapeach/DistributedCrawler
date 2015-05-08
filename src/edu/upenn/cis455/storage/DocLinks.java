package edu.upenn.cis455.storage;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;


// TODO: Auto-generated Javadoc
/**
 * The Class DocLinks.
 */
@Entity
public class DocLinks {
	
	/** The doc id. */
	@PrimaryKey
	String docID;
	
	/** The doc links. */
	private ArrayList<String> docLinks;
	
	/** The doc links file. */
	private static File docLinksFile = new File(Config.DocLinks_File);
	
	
	/**
	 * Instantiates a new doc links.
	 */
	private DocLinks(){}
	
	/**
	 * Instantiates a new doc links.
	 *
	 * @param docid the docid
	 */
	public DocLinks(String docid){
		this.docID = docid;
		docLinks = new ArrayList<String>();
	}
	
	/**
	 * Adds the link.
	 *
	 * @param docid the docid
	 */
	public void addLink(String docid){
		docLinks.add(docid);
		try {
			writeDocId(docid);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Write doc id.
	 *
	 * @param to_doc the to_doc
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	private void writeDocId(String to_doc) throws IOException {
		FileWriter fileWriter = new FileWriter(docLinksFile, true);
		fileWriter.write(docID+"\t"+to_doc+"\n");
		fileWriter.close();
		
	}

}
