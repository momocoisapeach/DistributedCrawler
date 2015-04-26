package edu.upenn.cis455.crawler;


import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.Socket;
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
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.tidy.Tidy;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import edu.upenn.cis455.crawler.info.URLInfo;


public class HttpClient {
	
	String header;
	static Socket mySocket;
	static String CRLF = "\r\n";
	static DataOutputStream output;
	static DataInputStream input;
	static PrintWriter writer;
	
	BufferedReader reader;
	String request;
	String path;
	String hostname;
	String protocol;
	long lastModified;
	
	String redirect =null;
	int port;
	int statusCode;
	HttpsURLConnection connection;
	int content_len;
	String content_type;
	URLInfo u;
	String url, finalString;
	URL https;
	String redirect_location =null;
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
	public HttpClient(String url){
		this.url = url;
		statusCode = -1;
		if(url.startsWith("https")){
			try {
				https = new URL(url);
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			hostname = https.getHost();
			protocol = "https";
			port = https.getPort();
			path = https.getPath();
			
				

		}
		
		else if(url.startsWith("http")){
			u = new URLInfo(url);
			protocol = "http";
			hostname = u.getHostName();
			port = u.getPortNo();
			path = u.getFilePath();
	//		System.out.println("hostname = "+hostname+"and the port number is"+port);

			
			
		}
		
		else{
			statusCode = 1000;
		}
		
	}

	/*
	 * 
	 * 
	 * */
	public int executeMethod() throws IOException {
		System.out.println("status code before executing method is "+statusCode);
		if(statusCode == 1000){
			return statusCode;
		}
		else if(statusCode == -1 && url.startsWith("https")){
			statusCode = connection.getResponseCode();
			if(statusCode == 200){
				statusCode = connection.getResponseCode();
				content_type = connection.getContentType().split(";")[0];
				content_len = connection.getContentLength();
				lastModified = connection.getLastModified();
				
				
//				System.out.println("(https)content type is "+content_type);
				reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
				return statusCode;
			}
			else if(statusCode == 301 || statusCode == 302){
				redirect_location = connection.getHeaderField("Location");
			}
			else return statusCode;
			
		}
		else if(statusCode == -1 && url.startsWith("http")){
			try{
				System.out.println("new socket establishing...");
				mySocket = new Socket(hostname,port);
			}
			catch( UnknownHostException e){
				statusCode = 1000;
			}
			writer = new PrintWriter(mySocket.getOutputStream());
			writer.write(request);
			writer.flush();
			reader = new BufferedReader(new InputStreamReader(mySocket.getInputStream()));
			String lines = null;
			lines = reader.readLine();
			System.out.println("initialLine is"+lines);
			if(lines!=null){
				
				String[] line = lines.split("\\s");
				statusCode = Integer.parseInt(line[1]);
			
			}
			setHeaders();
		}
		return statusCode;
		
	}
	
	public String getRedirectLocation(){
		return redirect_location;
	}
	
	

	private void setHeaders() throws IOException {
		String headerLine = null;
//		System.out.println("header is "+headerLine);
		while ((headerLine = reader.readLine()).length()!=0) {
//			System.out.println("headeri is "+headerLine);
		    String[] temp = headerLine.split(": ");
		    if(temp[0].length()!=0 && headerLine.contains(": ")){
		    	headers.put(temp[0], temp[1]);
		    }
		}
		if(headers.containsKey("Content-Length")){
			content_len = Integer.parseInt(headers.get("Content-Length"));
		}		
		if(headers.containsKey("Content-Type")){
			content_type = headers.get("Content-Type").split(";")[0];
//			System.out.println("content type is "+content_type);
		}
		if(headers.containsKey("Location")){
			redirect_location = headers.get("Location");
			System.out.println("location is "+redirect_location);
		}
		
		if(headers.containsKey("Last-Modified")){
			String temp = headers.get("Last-Modified");
			System.out.println(temp);
			Date date = null;
			try {
				SimpleDateFormat sdf1 = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss");
				sdf1.setTimeZone(TimeZone.getTimeZone("GMT"));
				date = sdf1.parse(temp);
			} catch (ParseException e) {
				try {
					SimpleDateFormat sdf2 = new SimpleDateFormat("EEEE, dd-MMM-yy HH:mm:ss");
					sdf2.setTimeZone(TimeZone.getTimeZone("GMT"));
					date = sdf2.parse(temp);
				} catch (ParseException e2) {
					try {
						date = new SimpleDateFormat("EEE MMM dd HH:mm:ss yyyy").parse(temp);
					} catch (ParseException e3) {
						
					}
				}
			}
			
			lastModified = date.getTime();
			
			//TODO
		}
	}
	
	public long getLastModified(){
		return lastModified;
	}
	
	public String getBodyString(){
		return finalString;
	}

	public Document getResponseBody(){
		System.out.println("in the method of getResponseBody...");
		Document doc = null;
		try{
			String bodyLines = null;
			StringBuilder body = new StringBuilder();
			DocumentBuilderFactory fact = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = fact.newDocumentBuilder();
			
			InputStream stream;
			if(!url.startsWith("https")){
				System.out.println("url starts with http....");
				while ((bodyLines = reader.readLine())!=null) {
//					System.out.println("lines are"+bodyLines);
				    body.append(bodyLines);
				    if(path.equals("/robots.txt")){
				    	body.append(CRLF);
				    }
				}
				finalString = body.toString();
//				System.out.println("flag3 string = "+finalString);
				
			}
			else{
				stream = connection.getInputStream();
				BufferedReader httpsreader = new BufferedReader(new InputStreamReader(stream));
		        StringBuilder out = new StringBuilder();
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
//			if(content_type.contains("text/html")){
//				System.out.println("type is a text/html..parsing....");
//				stream = new ByteArrayInputStream(finalString.getBytes("UTF-8"));
//				Tidy tidy = new Tidy();
//			    tidy.setInputEncoding("UTF-8");
//			    tidy.setOutputEncoding("UTF-8");
//			    tidy.setWraplen(Integer.MAX_VALUE);
//			    tidy.setXmlTags(true);
//			    tidy.setXmlOut(true);
//			    tidy.setSmartIndent(true);
//			    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
//			    tidy.parse(stream, outputStream);
//			    finalString = outputStream.toString();
//			    System.out.println("final String is "+finalString+"......");
//				stream = new ByteArrayInputStream(outputStream.toString("UTF-8").getBytes());
//				doc = db.parse(stream);
//	//			doSomething(D);
//	//			return D;
//			}
			if(content_type.contains("xml") || content_type.equals("application/xml") || content_type.endsWith("+xml")){
				StringReader strreader = new StringReader(finalString);
				BufferedReader bodyreader = new BufferedReader(strreader);
				InputSource is = new InputSource(bodyreader);
				doc = db.parse(is);
	//			System.out.println("parse finished");
			}
			else{
				doc = null;
			}
	//		doSomething(doc);
			
		//	System.out.println("body is"+body.toString());
			
		}catch(Exception e){
			
		}
		return doc;
	}
	
	public static void doSomething(Node node) {
	    // do something with the current node instead of System.out
//	    System.out.println("current node is "+node.getNodeName());

	    NodeList nodeList = node.getChildNodes();
	    for (int i = 0; i < nodeList.getLength(); i++) {
	        Node currentNode = nodeList.item(i);
	        if (currentNode.getNodeType() == Node.ELEMENT_NODE) {
	            //calls this method for all the children which is Element
//	        	System.out.println("children node"+i+currentNode.getNodeName());
	        	doSomething(currentNode);
	        }
	    }
	}

	public void releaseConnection() throws IOException {
		if(!url.startsWith("https")){
			mySocket.close();
		}
		else{
			connection.disconnect();
		}
		
		
	}

	public void setRequest(String method){
        //Construct and send the HTTP request
		if(url.startsWith("https")){
			try {
				connection = (HttpsURLConnection) https.openConnection();
				connection.setInstanceFollowRedirects(false);
				connection.setRequestProperty("User-Agent", "cis455crawler");	
				connection.setRequestMethod(method);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else{
	        request = method+" " + path + " HTTP/1.0"+CRLF;
	        request += "Host: " + hostname+CRLF;
	        request += "User-Agent: cis455crawler";
	        request += CRLF+CRLF;
	        System.out.println("the request is "+request);
		}
		
	}


}

