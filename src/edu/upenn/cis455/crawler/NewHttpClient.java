package edu.upenn.cis455.crawler;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.TimeZone;

import javax.net.ssl.HttpsURLConnection;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import edu.upenn.cis455.crawler.info.URLInfo;

// TODO: Auto-generated Javadoc
/**
 * The Class NewHttpClient.
 */
public class NewHttpClient {
	
	/** The header. */
	String header;
	
	/** The crlf. */
	static String CRLF = "\r\n";
	
	/** The output. */
	static DataOutputStream output;
	
	/** The input. */
	static DataInputStream input;
	
	/** The writer. */
	static PrintWriter writer;
	
	/** The reader. */
	BufferedReader reader;
	
	/** The request. */
	String request;
	
	/** The path. */
	String path;
	
	/** The hostname. */
	String hostname;
	
	/** The protocol. */
	String protocol;
	
	/** The last modified. */
	long lastModified;
	
	/** The redirect. */
	String redirect =null;
	
	/** The port. */
	int port;
	
	/** The status code. */
	int statusCode;
	
	/** The connection. */
	HttpURLConnection connection;
	
	/** The https connection. */
	HttpsURLConnection httpsConnection;
	
	/** The content_len. */
	int content_len;
	
	/** The content_type. */
	String content_type;
	
	/** The u. */
	URLInfo u;
	
	/** The final string. */
	String url, finalString;
	
	/** The https. */
	URL https;
	
	/** The redirect_location. */
	String redirect_location =null;
	
	/** The headers. */
	HashMap<String,String> headers = new HashMap<String,String>();
	/*
	 * two cases
	 * 
	 * 1) if it is a https url, then open a httpurlconnection, and cast it to 
	 * httpsurlconnection, set the request method as "GET"; get the response
	 * status code and the content type
	 * 
	 * 2) if it is a http url, then use the urlinfo class to analyze the url
	 * and get the hostname, port number and the file path. 
	 *    then open a new socket using the host name and the port number
	 *    partitioned from the url
	 * 
	 * */
	/**
	 * Instantiates a new new http client.
	 *
	 * @param url the url
	 */
	public NewHttpClient(String url){
		this.url = url;
		statusCode = -1;
		try {
			https = new URL(url);
			hostname = https.getHost();
			protocol = https.getProtocol();
			port = https.getPort();
			path = https.getPath();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		
		
	}
	
	/**
	 * Sets the request method.
	 *
	 * @param method the new request method
	 */
	public void setRequestMethod(String method){
		if(protocol.equals("http")){
			try {
				connection = (HttpURLConnection) https.openConnection();
				connection.setConnectTimeout(3000);
				connection.setReadTimeout(5000);
				connection.setInstanceFollowRedirects(true);
				connection.setRequestProperty("User-Agent", "cis455crawler");	
				connection.setRequestProperty("Accept-Language", "en-US");
				connection.setRequestProperty("Content-Language", "en");
				connection.setRequestMethod(method);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		else if(protocol.equals("https")){
			try {
				httpsConnection = (HttpsURLConnection) https.openConnection();
				httpsConnection.setInstanceFollowRedirects(true);
				httpsConnection.setRequestMethod(method);
				httpsConnection.setRequestProperty("User-Agent", "cis455crawler");
				httpsConnection.setRequestProperty("Accept-Language", "en-US");
				httpsConnection.setRequestProperty("Content-Language", "en");
			} catch (IOException e) {
				e.printStackTrace();
			}
				
			
		}
		else{
			statusCode = 1000;
		}
	}
	
	/**
	 * Execute method.
	 *
	 * @return the int
	 */
	public int executeMethod(){
//		System.out.println("status code before executing method is "+statusCode);
		if(statusCode == 1000){
			return statusCode;
		}
		else if(statusCode == -1 && url.startsWith("https")){
			try {
				statusCode = httpsConnection.getResponseCode();
				if(statusCode >200 && statusCode < 400){
					redirect = httpsConnection.getHeaderField("Location");
				}
				else if(statusCode == 200){
					if(httpsConnection.getContentType() != null){
						content_type = httpsConnection.getContentType().split(";")[0];
					}
					content_len = httpsConnection.getContentLength();
					lastModified = httpsConnection.getLastModified();
					
					
//					System.out.println("(https)content type is "+content_type);
//					reader = new BufferedReader(new InputStreamReader(httpsConnection.getInputStream()));
//					System.out.println("status code after executing method is "+statusCode);
//					
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			
		}
		else if(statusCode == -1 && url.startsWith("http")){
			try {
				statusCode = connection.getResponseCode();
//				System.out.println("response message is "+connection.getResponseMessage());
//				if(statusCode >200 && statusCode < 400){
//					redirect = httpsConnection.getHeaderField("Location");
//				}
				if(statusCode == 200){
//					System.out.println(connection.getContentType());
					if(connection.getContentType()!=null){
						content_type = connection.getContentType().split(";")[0];
					}
					
					content_len = connection.getContentLength();
					lastModified = connection.getLastModified();
					
					
//					System.out.println("(https)content type is "+content_type);
//					reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
//					System.out.println("status code after executing method is "+statusCode);
					
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}		
		}
		return statusCode;
	}
	
	/**
	 * Gets the response body.
	 *
	 * @return the response body
	 */
	public String getResponseBody(){

		StringBuilder out = new StringBuilder();
//		System.out.println("in the method of getResponseBody...");
		try{
			
			InputStream stream;
			if(!url.startsWith("https")){
//				System.out.println("url starts with http....");
				
				stream = connection.getInputStream();
				
//				System.out.println("after getting the input stream");
				BufferedReader httpsreader = new BufferedReader(new InputStreamReader(stream));
		        
		        String line;
		        while ((line = httpsreader.readLine()) != null) {
//		        	System.out.println(line);
		            out.append(line);
		            if(path.equals("/robots.txt")){
		            	out.append("\n");
		            }
		        }
	//	        System.out.println(out.toString());   //Prints the string content read from input stream
		        httpsreader.close();
		        finalString = out.toString();
	//	        System.out.println("final String before is "+finalString);
			}
			else{
//				System.out.println("url starts with https...");
				stream = httpsConnection.getInputStream();
				BufferedReader httpsreader = new BufferedReader(new InputStreamReader(stream));
		        String line;
		        while ((line = httpsreader.readLine()) != null) {
		            out.append(line);
		            if(path.equals("/robots.txt")){
		            	out.append("\n");
		            }
		        }
	//	        System.out.println(out.toString());   //Prints the string content read from input stream
		        httpsreader.close();
		        finalString = out.toString();
	//	        System.out.println("final String before is "+finalString);
			}
			
		//	System.out.println("body is"+body.toString());
			
		}catch(Exception e){
			
		}
		return finalString;
	
		
	}
	
	/**
	 * Release connection.
	 *
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public void releaseConnection() throws IOException {
		if(!url.startsWith("https")){
			connection.disconnect();
		}
		else{
			httpsConnection.disconnect();
		}
		
		
	}
	
	/**
	 * Gets the content type.
	 *
	 * @return the content type
	 */
	public String getContentType(){
		return content_type;
	}
	
	/**
	 * Gets the content len.
	 *
	 * @return the content len
	 */
	public int getContentLen(){
		return content_len;
	}
	
	/**
	 * Gets the host name.
	 *
	 * @return the host name
	 */
	public String getHostName(){
		return hostname;
	}
	
	/**
	 * Gets the port.
	 *
	 * @return the port
	 */
	public int getPort(){
		return port;
	}
	
	/**
	 * Gets the path.
	 *
	 * @return the path
	 */
	public String getPath(){
		return path;
	}
	
	/**
	 * Gets the protocol.
	 *
	 * @return the protocol
	 */
	public String getProtocol(){
		return protocol;
	}
	
	/**
	 * Gets the last modified.
	 *
	 * @return the last modified
	 */
	public long getLastModified(){
		return lastModified;
	}
	
	/**
	 * Gets the body string.
	 *
	 * @return the body string
	 */
	public String getBodyString(){
		return finalString;
	}



}
