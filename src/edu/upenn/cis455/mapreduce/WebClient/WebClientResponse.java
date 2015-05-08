/**
 * 
 */
package edu.upenn.cis455.mapreduce.WebClient;

import java.util.Date;
import java.util.HashMap;


// TODO: Auto-generated Javadoc
/**
 * The Class WebClientResponse.
 *
 * @author dichenli
 * parse and stores response
 */
public class WebClientResponse {
	

	/** The headers. */
	private HashMap<String, String> headers;
	
	/** The head. */
	private String head;
//	private String method;
	/** The body. */
private String body;
	
	/** The protocol. */
	private String protocol;
	
	/** The status code. */
	private int statusCode = -1;
	
	/** The last modified. */
	private Date lastModified;
	
	/** The date. */
	private Date date;
	
	/** The content length. */
	private long contentLength = (long) -1;
	
	/** The content type. */
	private String contentType; //raw info about content type, don't parse info like "text/html; charset=utf-8"
	
	/** The char set. */
	private String charSet;
	
	/** The request. */
	private WebClientRequest request; //the request associated with this response
	
	
	/**
	 * Instantiates a new web client response.
	 *
	 * @param request the request
	 */
	public WebClientResponse(WebClientRequest request) {
		this.request = request;
		this.headers = new HashMap<String, String>();
	}
	
	/**
	 * Sets the body.
	 *
	 * @param body the new body
	 */
	public void setBody(String body) {
		this.body = body;
	}

	/**
	 * Gets the body.
	 *
	 * @return the body
	 */
	public String getBody() {
		return body;
	}
	
	/**
	 * Gets the request.
	 *
	 * @return the request
	 */
	public WebClientRequest getRequest() {
		return request;
	}

	/**
	 * Sets the status code.
	 *
	 * @param statusCode the new status code
	 */
	public void setStatusCode(int statusCode) {
		this.statusCode = statusCode;
		
	}

	/**
	 * Sets the protocol.
	 *
	 * @param protocol the new protocol
	 */
	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}

	/**
	 * Put header.
	 *
	 * @param key the key
	 * @param value the value
	 */
	public void putHeader(String key, String value) {
//		System.out.println("head: " + key + ", value: " + value);
		headers.put(key, value);
	}

	/**
	 * Sets the content type.
	 *
	 * @param value the new content type
	 */
	public void setContentType(String value) {
		this.contentType = value;
		if(WebUtils.isSubString(contentType, "charset")) {
			setCharSet();
		}
	}
	
	/**
	 * Sets the char set.
	 */
	private void setCharSet() {
		String[] result = contentType.split("charset *=");
		if(result.length > 1) {
			this.charSet = result[1].trim().toUpperCase();	
		}
	}

	/**
	 * Sets the content length.
	 *
	 * @param parseInt the new content length
	 */
	public void setContentLength(int parseInt) {
		this.contentLength = (long) parseInt;
	}
	
	/**
	 * Sets the content length.
	 *
	 * @param length the new content length
	 */
	public void setContentLength(Long length) {
		this.contentLength = length;
	}

	/**
	 * Sets the date.
	 *
	 * @param convertDate the new date
	 */
	public void setDate(Date convertDate) {
		this.date = convertDate;
	}
	
	/**
	 * Sets the date.
	 *
	 * @param longDate the new date
	 */
	public void setDate(long longDate) {
		this.date = new Date(longDate);
	}

	/**
	 * Sets the last modified.
	 *
	 * @param convertDate the new last modified
	 */
	public void setLastModified(Date convertDate) {
		this.lastModified = convertDate;
	}
	
	/**
	 * Sets the last modified.
	 *
	 * @param longDate the new last modified
	 */
	public void setLastModified(long longDate) {
		setLastModified(new Date(longDate));
	}
	
//	public void setMethod(String method) {
//		this.method = method;
//	}
	
	/**
 * Gets the method.
 *
 * @return the method
 */
public String getMethod() {
		return this.request.getMethod();
	}
	
	/**
	 * Checks if is head method.
	 *
	 * @return true, if is head method
	 */
	public boolean isHeadMethod() {
		return request.isHeadMethod();
	}
	
	/**
	 * Checks if is gets the method.
	 *
	 * @return true, if is gets the method
	 */
	public boolean isGetMethod() {
		return request.isGetMethod();
	}
	
	/**
	 * Checks for content type.
	 *
	 * @return true, if successful
	 */
	public boolean hasContentType() {
		return contentType != null;
	}
	
	/**
	 * return raw info about content type returned from server, 
	 * don't parse info like "text/html; charset=utf-8".
	 *
	 * @return the content type
	 */
	public String getContentType() {
		return contentType;
	}
	
	/**
	 * Gets the char set.
	 *
	 * @return the char set
	 */
	public String getCharSet() {
		return charSet;
	}
	
	/**
	 * Gets the headers.
	 *
	 * @return the headers
	 */
	public HashMap<String, String> getHeaders() {
		return headers;
	}

	/**
	 * Gets the head.
	 *
	 * @return the head
	 */
	public String getHead() {
		return head;
	}

	/**
	 * Gets the protocol.
	 *
	 * @return the protocol
	 */
	public String getProtocol() {
		return protocol;
	}

	/**
	 * Gets the status code.
	 *
	 * @return the status code
	 */
	public int getStatusCode() {
		return statusCode;
	}

	/**
	 * Gets the last modified.
	 *
	 * @return the last modified
	 */
	public Date getLastModified() {
		return lastModified;
	}

	/**
	 * Gets the date.
	 *
	 * @return the date
	 */
	public Date getDate() {
		return date;
	}

	/**
	 * return content length specified in response header, return -1 
	 * if no content length is specified.
	 *
	 * @return the content length
	 */
	public long getContentLength() {
		return contentLength;
	}

	/**
	 * Checks if is xml.
	 *
	 * @return true, if is xml
	 */
	public boolean isXML() {
		return WebUtils.isXML(contentType);
	}
	
	/**
	 * Checks if is html.
	 *
	 * @return true, if is html
	 */
	public boolean isHTML() {
		return WebUtils.isHTML(contentType);
	}
	
	/**
	 * Gets the html xml type.
	 *
	 * @return the html xml type
	 */
	public String getHtmlXmlType() {
		return WebUtils.getHtmlXmlType(contentType);
	}
	
	/**
	 * Gets the host.
	 *
	 * @return the host
	 */
	public WebHost getHost() {
		if(request == null) {
			return null;
		}
		return request.getHost();
	}
	
	/**
	 * call request.getBaseUrl(), 
	 * return example http://www.google.com/abc/
	 * The base URL removes query and section information
	 * for http://www.google.com/abc/foo?c=d, base url
	 * is http://www.google.com/abc/foo
	 *
	 * @return the base url
	 */
	public String getBaseUrl() {
		if(request == null) {
//			System.err.println("Response.getBaseUrl(): request is null!!");
			return null;
		}
		return request.getBaseUrl();
	}
	
	/**
	 * return the string representation of the request url.
	 *
	 * @return the url str
	 */
	public String getUrlStr() {
		return request.getUrlStr();
	}

}
