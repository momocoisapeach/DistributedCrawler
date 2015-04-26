/**
 * 
 */
package edu.upenn.cis455.storage;

/**
 * @author dichenli
 *
 */
public class Config {

	public static int machine = 0;
	public static String[] DocID_Files = {"/Users/peach/Documents/upenn/2015spring/cis555/db/db39/input/id", "/Users/dichenli/Downloads/tryCrawl/id.txt"};

	public static String[] DocLinks_Files = {"/Users/peach/Documents/upenn/2015spring/cis555/db/db39/links.txt", "/Users/dichenli/Downloads/tryCrawl/links.txt"};

	public static String[] DocContent_Files = {"/Users/peach/Documents/upenn/2015spring/cis555/db/db39/content/", "/Users/dichenli/Downloads/tryCrawl/content/"};

	public static String[] MapReduce_Outputs = {"/Users/peach/Documents/upenn/2015spring/cis555/db/db39/output/",""};
	
	public static String DocID_File = DocID_Files[machine];
	public static String DocLinks_File = DocLinks_Files[machine];
	public static String DocContent_File = DocContent_Files[machine];
	public static String MapReduce_Output = MapReduce_Outputs[machine];
}
