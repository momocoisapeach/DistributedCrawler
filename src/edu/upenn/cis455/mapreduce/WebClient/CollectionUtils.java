/**
 * 
 */
package edu.upenn.cis455.mapreduce.WebClient;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * @author dichenli
 * utilities to deal with Map, ArrayList, Stack, etc. anything related to collections
 */
public class CollectionUtils {

	/**
	 * convert from arrayList to array of same type
	 * @param list
	 * @return
	 */
	public static String[] toArray(ArrayList<String> list) {
		Object[] array = list.toArray();
		String[] strArr = Arrays.copyOf(array, array.length, String[].class);
		return strArr;
	}
}
