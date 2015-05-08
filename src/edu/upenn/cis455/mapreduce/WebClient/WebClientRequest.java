/**
 * 
 */
package edu.upenn.cis455.mapreduce.WebClient;

import java.io.StringWriter;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;


// TODO: Auto-generated Javadoc
/**
 * The Class WebClientRequest.
 *
 * @author dichenli
 * an object to provide interface for webclient to generate request text
 * this class is only for request string building, not responsible for any IO
 */
public class WebClientRequest {
	/*
		GET /favicon.ico HTTP/1.1
		Host: docs.oracle.com
		Accept: * /*
		Accept-Encoding: gzip, deflate, sdch
		Accept-Language: en-US,en;q=0.8,zh-CN;q=0.6,zh;q=0.4
		Cookie: ...
		User-Agent: Mozilla/5.0 (Macintosh; Intel Mac OS X 10_9_5) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/40.0.2214.115 Safari/537.36
	 */

//	public static final String defaultAgent = "cis455crawler";
	/** The default protocol. */
	private static String defaultProtocol = "HTTP/1.1";
	
	/** The xml types. */
	private static String[] xmlTypes = 
		{"text/html", 
		"application/xml", 
		"application/xhtml+xml",
		"text/xml"};
	//I don't care about language now, so language is built by a simple string
	//I may need to switch to array just like xmlTypes if necessary
	/** The default accept language. */
	private static String defaultAcceptLanguage = "en-US,en;q=0.8, */*;q=0.1";
	
	/** The url. */
	private URL url;
	
	/** The method. */
	private String method; //GET POST HEAD
	
	/** The protocol version. */
	private String protocolVersion; //"HTTP/1.1"
	
	/** The request url. */
	private String requestUrl; //requested URL, for http://www.google.com/abc, url = "/abc"
	
	/** The host. */
	private WebHost host;
	
	/** The accept types. */
	private ArrayList<String> acceptTypes; //Accept: text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8
	
	/** The accept lang. */
	private String acceptLang; //accept language
	
	/** The user agent. */
	private String userAgent;
	
	/** The content length. */
	private long contentLength;
//	private String requestHead;
	/** The request body. */
private StringWriter requestBody;
	
	/** The content type. */
	private String contentType; //useful for POST request
//	private boolean ready;

	/**
 * Gets the method.
 *
 * @return the method
 */
public String getMethod() {
		return method;
	}
	
	/**
	 * Checks if is head method.
	 *
	 * @return true, if is head method
	 */
	public boolean isHeadMethod() {
		return getMethod().equalsIgnoreCase("HEAD");
	}
	
	/**
	 * Checks if is gets the method.
	 *
	 * @return true, if is gets the method
	 */
	public boolean isGetMethod() {
		return getMethod().equalsIgnoreCase("GET");
	}
	
	/**
	 * Gets the user agent.
	 *
	 * @return the user agent
	 */
	public String getUserAgent() {
		return userAgent;
	}
	
	/**
	 * Gets the url.
	 *
	 * @return the url
	 */
	public URL getUrl() {
		return url;
	}
	
	/**
	 * Gets the url str.
	 *
	 * @return the url str
	 */
	public String getUrlStr() {
		return url.toString();
	}
	
	/**
	 * get base url: for http://www.google.com/abc/foo?c=d, base url
	 * is http://www.google.com/abc/foo
	 *
	 * @return the base url
	 */
	public String getBaseUrl() {
//		String protocol = url.getProtocol();
//		if(protocol == null || host == null) {
//			System.err.println("request protocol or host == null??? debug it");
//		}
		
//		return host.getBaseUrl(protocol);
		if(url == null) {
			System.err.println("WebClientRequest.getBaseUrl(): url == null?");
		}
		return WebUtils.getBaseUrl(url);
	}
	
	/**
	 * Gets the host.
	 *
	 * @return the host
	 */
	public WebHost getHost() {
		return host;
	}
	
	/**
	 * Gets the protocol.
	 *
	 * @return the protocol
	 */
	public String getProtocol() {
		return url.getProtocol();
	}
	
	/**
	 * Gets the accept.
	 *
	 * @return the accept
	 */
	public String getAccept() {
		return RequestHelper.constructAcceptHead(acceptTypes);
	}
	
	/**
	 * initiate an object.
	 *
	 * @param url the url
	 * @param host the host
	 * @param port the port
	 * @param method the method
	 * @param requestUrl the request url
	 * @param userAgent the user agent
	 */
	private WebClientRequest(URL url, String host, 
			int port, String method, String requestUrl, String userAgent) {

		if(url == null || host == null || port < 0 
				|| method == null || requestUrl == null) {
			throw new IllegalArgumentException();
		}

		//get the WebHost object representing the host this request heads for
		this.host = new WebHost(host, port);
		this.url = url;
		this.userAgent = userAgent;
		protocolVersion = defaultProtocol;
		acceptLang = defaultAcceptLanguage;
		contentLength = -1;
		setAcceptType();
		setMethod(method);
		setRequestUrl(requestUrl);
//		updateReady();
	}

	/**
	 * Factory method
	 * generate a WebClientReuqest object by a complete url
	 * returns null if failed.
	 *
	 * @param url the url
	 * @param method the method
	 * @param userAgent the user agent
	 * @return the web client request
	 */
	public static WebClientRequest getWebClientRequest(String url, String method, String userAgent) {
		URL urlObj = WebUtils.getURL(url);
		if(urlObj == null) {
			return null;
		}
		return getWebClientRequest(urlObj, method, userAgent);
	}
	
	/**
	 * Factory method
	 * generate a WebClientReuqest object by a url object
	 * returns null if failed. null parameter is not allowed
	 *
	 * @param urlObj the url obj
	 * @param method the method
	 * @param userAgent the user agent
	 * @return the web client request
	 */
	public static WebClientRequest getWebClientRequest(URL urlObj, String method, String userAgent) {
		if(urlObj == null || method == null) {
			throw new NullPointerException();
		}
		String host = WebUtils.getHostName(urlObj);
		int port = WebUtils.getPort(urlObj);
		String requestUrl = WebUtils.getRequestPath(urlObj);
		try{
			return new WebClientRequest(urlObj, host, port, method, requestUrl, userAgent);
		} catch (IllegalArgumentException e) {
			return null;
		}
	}

	/**
	 * requestUrl is the portion of url after the http://host:port part.
	 *
	 * @param requestUrl the new request url
	 */
	public void setRequestUrl(String requestUrl) {
		if(requestUrl == null || requestUrl.equals("")) {
			throw new IllegalArgumentException();
		}
		if(requestUrl.charAt(0) != '/') {
			throw new IllegalArgumentException();
		}
		this.requestUrl = requestUrl;
	}

	/**
	 * Sets the accept type.
	 */
	private void setAcceptType() {
		acceptTypes = new ArrayList<String>();
		for(int i = 0; i < xmlTypes.length; i++) {
			acceptTypes.add(xmlTypes[i]);
		}
	}

//	private boolean updateReady() {
//		ready = isReady();
//		return ready;
//	}
	
	/**
 * Checks if is ready.
 *
 * @return true, if is ready
 */
public boolean isReady() {
		return method != null && protocolVersion != null 
				&& host != null && userAgent != null
				&& requestUrl != null;
	}

	/**
	 * Set request method, it must be get, post or head.
	 *
	 * @param method the new method
	 */
	public void setMethod(String method) {
		if(method == null) {
			throw new IllegalArgumentException();
		}
		method = method.trim().toUpperCase();
		if(!method.equals("GET") 
				&& !method.equals("POST") 
				&& !method.equals("HEAD")) {
			throw new IllegalArgumentException();
		}

		this.method = method;
	}

	/**
	 * Gets the body writer.
	 *
	 * @return the body writer
	 */
	public Writer getBodyWriter() {
		if(requestBody == null) {
			requestBody = new StringWriter();
		}
		return requestBody;
	}
	
	/**
	 * generate request head compliant to HTTP standard, 
	 * from the fields of this Request object.
	 *
	 * @return the request head
	 */
	public String getRequestHead() {
		if(!isReady()) {
//			requestHead = null;
			return null;
		}
		
		StringBuilder head = 
			RequestHelper.writeFirstLine(method, requestUrl, protocolVersion);
		head = RequestHelper.setHost(head, host);
		head = RequestHelper.setAccept(head, acceptTypes);
		head = RequestHelper.setAcceptLanguage(head, acceptLang);
		head = RequestHelper.setUserAgent(head, userAgent);
		
		if(method.equalsIgnoreCase("POST")) {
			if(contentLength < 0) {
				contentLength = requestBody.toString().length();				
			}
			head = RequestHelper.setContentLength(head, contentLength);
			head = RequestHelper.setContentType(head, this.contentType);
		}
		String requestHead = head.append("\r\n").toString();
		return requestHead;
	}
	
	/**
	 * Sets the content length.
	 *
	 * @param length the new content length
	 */
	public void setContentLength(long length) {
		this.contentLength = length;
	}
	
	/**
	 * Sets the content type.
	 *
	 * @param type the new content type
	 */
	public void setContentType(String type) {
		this.contentType = type;
	}
	
	/**
	 * get the full request including request head and body
	 * The body will be included only if the request method is POST!
	 * If the request is not ready to be sent yet, the method will return null.
	 *
	 * @return the full request
	 */
	public String getFullRequest() {
		if(!isReady()) {
			return null;
		}
//		if(requestHead == null) {
//			getRequestHead();
//		}
		String requestHead = getRequestHead();
		if(requestHead == null) {
			return null;
		}
		
		String body = null;
		if(method.equals("POST")) {
			 body = getRequestBody();	
		}
		
		if(body == null) {
			return requestHead;
		}
		return requestHead + body;
	}

	/**
	 * get request body string from body stringWriter. Append \r\n
	 *
	 * @return the request body
	 */
	String getRequestBody() {
		if(requestBody == null) {
			return null;
		}
		
		String body = requestBody.toString();
		if(body.equals("")) {
			return null;
		}
		
		return body + "\r\n\r\n";
	}

	/**
	 * nested called to setLastCrawl of host.
	 *
	 * @param date the new last crawl
	 */
	public void setLastCrawl(Date date) {
		this.host.setLastCrawl(date);
		
	}
	
	
}
