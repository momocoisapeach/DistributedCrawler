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


/**
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
	private static String defaultProtocol = "HTTP/1.1";
	private static String[] xmlTypes = 
		{"text/html", 
		"application/xml", 
		"application/xhtml+xml",
		"text/xml"};
	//I don't care about language now, so language is built by a simple string
	//I may need to switch to array just like xmlTypes if necessary
	private static String defaultAcceptLanguage = "en-US,en;q=0.8, */*;q=0.1";
	private URL url;
	private String method; //GET POST HEAD
	private String protocolVersion; //"HTTP/1.1"
	private String requestUrl; //requested URL, for http://www.google.com/abc, url = "/abc"
	private WebHost host;
	private ArrayList<String> acceptTypes; //Accept: text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8
	private String acceptLang; //accept language
	private String userAgent;
	private long contentLength;
//	private String requestHead;
	private StringWriter requestBody;
	private String contentType; //useful for POST request
//	private boolean ready;

	public String getMethod() {
		return method;
	}
	
	public boolean isHeadMethod() {
		return getMethod().equalsIgnoreCase("HEAD");
	}
	
	public boolean isGetMethod() {
		return getMethod().equalsIgnoreCase("GET");
	}
	
	public String getUserAgent() {
		return userAgent;
	}
	
	public URL getUrl() {
		return url;
	}
	
	public String getUrlStr() {
		return url.toString();
	}
	
	/**
	 * get base url: for http://www.google.com/abc/foo?c=d, base url
	 * is http://www.google.com/abc/foo
	 * @return
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
	
	public WebHost getHost() {
		return host;
	}
	
	public String getProtocol() {
		return url.getProtocol();
	}
	
	public String getAccept() {
		return RequestHelper.constructAcceptHead(acceptTypes);
	}
	
	/**
	 * initiate an object
	 * @param host: e.g. localhost
	 * @param port: e.g. 8080
	 * @param method: e.g. GET, POST
	 * @param requestUrl: e.g. /abc?foo=bar
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
	 * returns null if failed
	 * @param url: a complete url such as "http://www.google.com/abc"
	 * @param method: get, post, head
	 * @return
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
	 * @param url: a complete url such as "http://www.google.com/abc"
	 * @param method: get, post, head
	 * @return
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
	 * requestUrl is the portion of url after the http://host:port part
	 * @param requestUrl
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
	
	public boolean isReady() {
		return method != null && protocolVersion != null 
				&& host != null && userAgent != null
				&& requestUrl != null;
	}

	/**
	 * Set request method, it must be get, post or head
	 * @param method
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

	public Writer getBodyWriter() {
		if(requestBody == null) {
			requestBody = new StringWriter();
		}
		return requestBody;
	}
	
	/**
	 * generate request head compliant to HTTP standard, 
	 * from the fields of this Request object
	 * @return
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
	
	public void setContentLength(long length) {
		this.contentLength = length;
	}
	
	public void setContentType(String type) {
		this.contentType = type;
	}
	
	/**
	 * get the full request including request head and body
	 * The body will be included only if the request method is POST!
	 * If the request is not ready to be sent yet, the method will return null
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
	 * @return
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
	 * nested called to setLastCrawl of host
	 * @param date
	 */
	public void setLastCrawl(Date date) {
		this.host.setLastCrawl(date);
		
	}
	
	
}
