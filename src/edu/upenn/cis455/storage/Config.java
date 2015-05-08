/**
 * 
 */
package edu.upenn.cis455.storage;

import java.io.File;

import edu.upenn.cis455.crawler.XPathCrawler;

// TODO: Auto-generated Javadoc
/**
 * The Class Config.
 *
 * @author dichenli
 */
public class Config {

	/** The machine. */
	public static int machine = 0;
	
	/** The Roots. */
	public static String[] Roots = {
		"/Users/peach/Documents/upenn/2015spring/cis555/db/db39/", 
		"/Users/dichenli/Downloads/tryCrawl/"
	};

	
	/** The Root. */
	public static String Root = null;
	
	/** The Doc links_ file. */
	public static String DocLinks_File;
	
	/** The Doc content_ file. */
	public static String DocContent_File;
	
	/** The Map reduce_ output. */
	public static String MapReduce_Output;
	
	/** The Map reduce_ input. */
	public static String MapReduce_Input;
	
	/** The M r_ input_ name. */
	public static String MR_Input_Name;
	
	/** The M r_ output_ name. */
	public static String MR_Output_Name;
	
//	public static File MR_Input
	/** The Doc i d_ file. */
public static String DocID_File;
	
	/**
	 * Inits the.
	 *
	 * @param root the root
	 */
	public static void init(String root) {
		Root = root;
		DocLinks_File = Root + "links.txt";
		DocContent_File = Root + "content/";
		MapReduce_Output = Root + "output/";
		MapReduce_Input = Root + "input/";
		
		MR_Input_Name = Root + "mr_input/";
		MR_Output_Name = Root + "mr_output/";
		
//		public static File MR_Input
		DocID_File = MapReduce_Input + "id";
	}
}
