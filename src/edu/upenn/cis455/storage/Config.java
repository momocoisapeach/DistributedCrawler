/**
 * 
 */
package edu.upenn.cis455.storage;

/**
 * @author dichenli
 *
 */
public class Config {

	public static int machine = 1;
	public static String[] Roots = {
		"/Users/peach/Documents/upenn/2015spring/cis555/db/db39/", 
		"/Users/dichenli/Downloads/tryCrawl/"
	};

	
	public static String Root = Roots[machine];
	public static String DocLinks_File = Root + "links.txt";
	public static String DocContent_File = Root + "content/";
	public static String MapReduce_Output = Root + "output/";
	public static String MapReduce_Input = Root + "input/";
	public static String DocID_File = MapReduce_Input + "id";
}
