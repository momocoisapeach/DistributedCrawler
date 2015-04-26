/**
 * 
 */
package edu.upenn.cis455.mapreduce.WebClient;

import java.util.Date;
import java.util.HashMap;


/**
 * @author dichenli
 * parse and stores response
 */
public class WebClientResponse {
	

	private HashMap<String, String> headers;
	private String head;
//	private String method;
	private String body;
	private String protocol;
	private int statusCode = -1;
	private Date lastModified;
	private Date date;
	private long contentLength = (long) -1;
	private String contentType; //raw info about content type, don't parse info like "text/html; charset=utf-8"
	private String charSet;
	private WebClientRequest request; //the request associated with this response
	
	
	public WebClientResponse(WebClientRequest request) {
		this.request = request;
		this.headers = new HashMap<String, String>();
	}
	
	public void setBody(String body) {
		this.body = body;
	}

	public String getBody() {
		return body;
	}
	
	public WebClientRequest getRequest() {
		return request;
	}

	public void setStatusCode(int statusCode) {
		this.statusCode = statusCode;
		
	}

	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}

	public void putHeader(String key, String value) {
//		System.out.println("head: " + key + ", value: " + value);
		headers.put(key, value);
	}

	public void setContentType(String value) {
		this.contentType = value;
		if(WebUtils.isSubString(contentType, "charset")) {
			setCharSet();
		}
	}
	
	private void setCharSet() {
		String[] result = contentType.split("charset *=");
		if(result.length > 1) {
			this.charSet = result[1].trim().toUpperCase();	
		}
	}

	public void setContentLength(int parseInt) {
		this.contentLength = (long) parseInt;
	}
	
	public void setContentLength(Long length) {
		this.contentLength = length;
	}

	public void setDate(Date convertDate) {
		this.date = convertDate;
	}
	
	public void setDate(long longDate) {
		this.date = new Date(longDate);
	}

	public void setLastModified(Date convertDate) {
		this.lastModified = convertDate;
	}
	
	public void setLastModified(long longDate) {
		setLastModified(new Date(longDate));
	}
	
//	public void setMethod(String method) {
//		this.method = method;
//	}
	
	public String getMethod() {
		return this.request.getMethod();
	}
	
	public boolean isHeadMethod() {
		return request.isHeadMethod();
	}
	
	public boolean isGetMethod() {
		return request.isGetMethod();
	}
	
	public boolean hasContentType() {
		return contentType != null;
	}
	
	/**
	 * return raw info about content type returned from server, 
	 * don't parse info like "text/html; charset=utf-8"
	 * @return
	 */
	public String getContentType() {
		return contentType;
	}
	
	public String getCharSet() {
		return charSet;
	}
	
	public HashMap<String, String> getHeaders() {
		return headers;
	}

	public String getHead() {
		return head;
	}

	public String getProtocol() {
		return protocol;
	}

	public int getStatusCode() {
		return statusCode;
	}

	public Date getLastModified() {
		return lastModified;
	}

	public Date getDate() {
		return date;
	}

	/**
	 * return content length specified in response header, return -1 
	 * if no content length is specified
	 * @return
	 */
	public long getContentLength() {
		return contentLength;
	}

	public boolean isXML() {
		return WebUtils.isXML(contentType);
	}
	
	public boolean isHTML() {
		return WebUtils.isHTML(contentType);
	}
	
	public String getHtmlXmlType() {
		return WebUtils.getHtmlXmlType(contentType);
	}
	
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
	 * @return
	 */
	public String getBaseUrl() {
		if(request == null) {
//			System.err.println("Response.getBaseUrl(): request is null!!");
			return null;
		}
		return request.getBaseUrl();
	}
	
	/**
	 * return the string representation of the request url
	 * @return
	 */
	public String getUrlStr() {
		return request.getUrlStr();
	}

}
