/**
 * 
 */
package edu.upenn.cis455.storage;

import java.io.File;

import edu.upenn.cis455.crawler.XPathCrawler;

/**
 * @author dichenli
 *
 */
public class Config {

	public static int machine = 0;
	public static String[] Roots = {
		"/Users/peach/Documents/upenn/2015spring/cis555/db/db39/", 
		"/Users/dichenli/Downloads/tryCrawl/"
	};

	
	public static String Root = null;
	public static String DocLinks_File = Root + "links.txt";
	public static String DocContent_File = Root + "content/";
	public static String MapReduce_Output = Root + "output/";
	public static String MapReduce_Input = Root + "input/";
	
	public static String MR_Input_Name = Root + "mr_input/";
	public static String MR_Output_Name = Root + "mr_output/";
	
//	public static File MR_Input
	public static String DocID_File = MapReduce_Input + "id";
}
