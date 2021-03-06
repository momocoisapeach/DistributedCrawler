/**
 * 
 */
package edu.upenn.cis455.mapreduce;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import edu.upenn.cis455.storage.Config;

// TODO: Auto-generated Javadoc
/**
 * The Class MapReduceUtils.
 *
 * @author dichenli
 */
public class MapReduceUtils {

//	public static final String PROJECT_PATH = PROJECT_PATHS[0];
//	public static final String ROOT_DIR = Config.Root;
	
	/**
 * get a Job object from the given class name. return null if any exception 
 *
 * @param className the class name
 * @return the job
 */
	public static Job getJob(String className) {
		Class jobClass = null;
		try {
			System.out.println("MapReduceUtils.getJob: className: " + className);
			jobClass = Class.forName(className);
			System.out.println("MapReduceUtils.getJob: found class");
			Job job = (Job) jobClass.newInstance();
			System.out.println("MapReduceUtils.getJob: found object");
			return job;
		} catch (Exception e) {
			return null;
		}
	}
	
//	public static String fullPath(String relativePath) {
//		while (relativePath.charAt(0) == '/') {
//			relativePath = relativePath.substring(1);
//		}
//		return ROOT_DIR + relativePath;
//	}
	
	/**
 * parse string to get an integer, return null if the string is not a number.
 *
 * @param intStr the int str
 * @return the integer
 */
	public static Integer parseInt(String intStr) {
		if(intStr == null) {
			return null;
		}
		try {
			return Integer.parseInt(intStr);
		} catch (NumberFormatException e) {
			return null;
		}
	}

	/**
	 * send fail message to server .
	 *
	 * @param response the response
	 * @param code the code
	 * @return false if IOException and msg not sent
	 */
	public static boolean sendFailedMsg(HttpServletResponse response, int code) {
		try {
			response.sendError(code);
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	/**
	 * send success message to server indicating everything normal.
	 *
	 * @param response the response
	 * @return true, if successful
	 */
	public static boolean sendSuccesMsg(HttpServletResponse response) {
		try {
			response.setContentLength(1);
			response.getWriter().write(1);
			response.flushBuffer();
			return true;
		} catch (IOException e) {
			return false;
		}
	}
}
