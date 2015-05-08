/**
 * 
 */
package edu.upenn.cis455.mapreduce.WebClient;

import java.util.ArrayList;


// TODO: Auto-generated Javadoc
/**
 * The Class RequestHelper.
 *
 * @author dichenli
 * contains exclusively static method to help build client request string
 * called exclusively by WebClientRequest, so it actually should've been a nested class
 * it is isolated out to avoid huge class
 */
class RequestHelper {
	/*
	GET /favicon.ico HTTP/1.1
	Host: docs.oracle.com
	Accept: * /*
	Accept-Encoding: gzip, deflate, sdch
	Accept-Language: en-US,en;q=0.8,zh-CN;q=0.6,zh;q=0.4
	Cookie: ...
	User-Agent: Mozilla/5.0 (Macintosh; Intel Mac OS X 10_9_5) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/40.0.2214.115 Safari/537.36
	 */
	
	/**
	 * build the first line like "GET /abc HTTP/1.1\r\n"
	 * return the stringBuilder to continue next steps
	 *
	 * @param method the method
	 * @param requestUrl the request url
	 * @param protocol the protocol
	 * @return the string builder
	 */
	static StringBuilder writeFirstLine(String method, 
			String requestUrl, String protocol) {
		//no illegalArgumentException checker, the responsibility is at webclientRequest
		StringBuilder head = new StringBuilder();
		head.append(method).append(' ').append(requestUrl)
		.append(' ').append(protocol).append("\r\n");
		return head;
	}

	/**
	 * append the line: "Host: localhost:8080\r\n" to head.
	 *
	 * @param head the head
	 * @param webHost the web host
	 * @return the string builder
	 */
	public static StringBuilder setHost(StringBuilder head, WebHost webHost) {
		if(webHost == null) {
			throw new IllegalArgumentException();
		}
		return addNameValuePair(head, "Host: ", webHost.toString());
	}

	/**
	 * generate accept type list such as:
	 * Accept: image/webp,* /*;q=0.8
	 * and append to head
	 *
	 * @param head the head
	 * @param acceptTypes the accept types
	 * @return the string builder
	 */
	public static StringBuilder setAccept(StringBuilder head,
			ArrayList<String> acceptTypes) {
		
		if(head == null || acceptTypes == null) {
			throw new IllegalArgumentException();
		}
		String accept = constructAcceptHead(acceptTypes);
		return addNameValuePair(head, "Accept: ", accept);
	}
	
	/**
	 * Construct accept head.
	 *
	 * @param acceptTypes the accept types
	 * @return the string
	 */
	public static String constructAcceptHead(ArrayList<String> acceptTypes) {
		StringBuilder accept = new StringBuilder();
		if(acceptTypes.size() == 0) {
			accept.append("*/*");
		} else {
			for(String type : acceptTypes) {
				accept.append(type).append(',');
			}
			//remove the last ',' character
			accept.deleteCharAt(accept.length() - 1);
		}
		return accept.toString();
	}

	/**
	 * append the line:
	 * Accept-Language: en-US,en;q=0.8,zh-CN;q=0.6,zh;q=0.4
	 *
	 * @param head the head
	 * @param acceptLang the accept lang
	 * @return the string builder
	 */
	public static StringBuilder setAcceptLanguage(StringBuilder head,
			String acceptLang) {
		return addNameValuePair(head, "Accept-Language: ", acceptLang);
	}

	/**
	 * Append the line:
	 * User-Agent: Mozilla/5.0
	 *
	 * @param head the head
	 * @param userAgent the user agent
	 * @return the string builder
	 */
	public static StringBuilder setUserAgent(StringBuilder head,
			String userAgent) {
		return addNameValuePair(head, "User-Agent: ", userAgent);
	}
	
	
	/**
	 * Adds the name value pair.
	 *
	 * @param head the head
	 * @param name the name
	 * @param value the value
	 * @return the string builder
	 */
	public static StringBuilder addNameValuePair(
			StringBuilder head, String name, String value) {
		
		if(head == null || name == null || value == null) {
			throw new IllegalArgumentException();
		}
		
		head.append(name).append(value);
		return head.append("\r\n");
	}

	/**
	 * Sets the content length.
	 *
	 * @param head the head
	 * @param contentLength the content length
	 * @return the string builder
	 */
	public static StringBuilder setContentLength(StringBuilder head,
			long contentLength) {
		head.append("Content-Length: " + contentLength + "\r\n");
		return head;
	}

	/**
	 * Sets the content type.
	 *
	 * @param head the head
	 * @param contentType the content type
	 * @return the string builder
	 */
	static StringBuilder setContentType(StringBuilder head,
			String contentType) {
		head.append("Content-Type: " + contentType + "\r\n");
		return head;
	}
}
