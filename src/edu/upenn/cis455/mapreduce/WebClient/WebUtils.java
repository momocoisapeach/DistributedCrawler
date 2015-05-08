/**
 * 
 */
package edu.upenn.cis455.mapreduce.WebClient;

import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

// TODO: Auto-generated Javadoc
/**
 * The Class WebUtils.
 *
 * @author dichenli
 * utilities to parse and retrieve useful info from url
 */
public class WebUtils {

	/** The Constant httpPort. */
	public static final int httpPort = 80;
	
	/** The Constant httpsPort. */
	public static final int httpsPort = 443;

	/**
	 * get URL object from its string representation
	 * the url must be complete with http:// as beginning
	 * return null if failed.
	 *
	 * @param url the url
	 * @return the url
	 */
	public static URL getURL(String url) {
		if(url == null) {
			return null;
		}
		try {
			return new URL(url);
		} catch (MalformedURLException e) {
			return null;
		}
	}

	/**
	 * Checks if is http or https.
	 *
	 * @param protocol the protocol
	 * @return true, if is http or https
	 */
	public static boolean isHttpOrHttps(String protocol) {
		if(protocol == null) {
			return false;
		}
		return protocol.equalsIgnoreCase("http") 
				|| protocol.equalsIgnoreCase("https");
	}

	/**
	 * generate url from base + path. e.g. base = http://abc.com/foo, path = bar
	 * then http://abc.com/foo/bar is returned.
	 * if relativePath is actually a complete url, then baseUrl is ignored
	 *
	 * @param baseUrl the base url
	 * @param relativePath the relative path
	 * @return the url
	 */
	public static URL generateURL(String baseUrl, String relativePath) {
		if(baseUrl == null || relativePath == null) {
			throw new IllegalArgumentException();
		}
//		System.out.println("WebUtils.generateURL(): baseUrlStr: " + baseUrl.toString());
//		System.out.println("WebUtils.generateURL(): relative path: " + relativePath);
		baseUrl = baseUrl.trim();
		relativePath = relativePath.trim();
		URL url = null;
		try {
			url = new URL(relativePath);
			return url;
		} catch(MalformedURLException e) {
			//			System.out.println("relative url failed");
		}
		//		try {
		//			url = new URL(baseUrl, relativePath);
		//			return url;
		//		} catch (MalformedURLException e) {
		//			System.err.println(
		//			"WebUitls.generateURL(): can't create url by "
		//			+ baseUrl.toString() + " and " + relativePath);
		//			return null;
		//		}
//		String urlStr = null;
//		String temp = baseUrl.substring(0, baseUrl.lastIndexOf('/') + 1);
//		
//		System.out.println("WebUitls.generateURL(): final url is " + urlStr);
		try {
			return new URL(new URL(baseUrl), relativePath);
		} catch (MalformedURLException e) {
//			System.err.println(
//					"WebUitls.generateURL(): can't create url by "
//							+ baseUrl.toString() + " and " + relativePath);
			return null;
		}
	}

	/**
	 * Ends with slash.
	 *
	 * @param url the url
	 * @return true, if successful
	 */
	private static boolean endsWithSlash(String url) {
		return url.charAt(url.length() - 1) == '/';
	}

	/**
	 * Starts with slash.
	 *
	 * @param url the url
	 * @return true, if successful
	 */
	private static boolean startsWithSlash(String url) {
		return url.charAt(0) == '/';
	}

	//warning: not tested. not used anywhere yet
	/**
	 * Gets the host port.
	 *
	 * @param url the url
	 * @return the host port
	 */
	public static String getHostPort(URL url) {
		if(url == null) {
			throw new NullPointerException(); //fail fast
		}
		String host = getHostName(url);
		int port = getPort(url);
		if(host == null) {
			return null;
		}
		return host + ":" + port;
	}

	/**
	 * get host name from url. host name is like: "www.google.com", no port number
	 *
	 * @param url the url
	 * @return the host name
	 */
	public static String getHostName(URL url) {
		if(url == null) {
			return null;
		}
		return url.getHost();
	}

	/**
	 * get port from url object. return http default (80) if url port is absent
	 *
	 * @param url the url
	 * @return the port
	 */
	public static int getPort(URL url) {
		if(url == null) {
			throw new NullPointerException();
		}
		int port = url.getPort();
		if(port == -1) {
			String protocol = url.getProtocol().toLowerCase();
			if(protocol.equals("http")) {
				port = httpPort;
			} else if (protocol.equals("https")) {
				port = httpsPort;
			} else {
				throw new IllegalArgumentException("Unsupported protocol!");
			}
		}
		return port;
	}

	/**
	 * will return the requested url path as well as query string
	 * for example, http://www.google.com/abc/def?foo=bar#3
	 * this will return "/abc/def?foo=bar", but not host, port or section number
	 *
	 * @param url the url
	 * @return the request path
	 */
	public static String getRequestPath(URL url) {
		if(url == null) {
			return null;
		}

		//this magic method do the main job for us!
		String requestUrl = url.getFile();
		//if the url is http://www.google.com, url.getFile() will
		//return "", but what we want to send in http request is "GET / HTTP/1.1"
		//so we need "/"
		if(requestUrl.equals("")) {
			requestUrl = "/";
		}
		return requestUrl;
	}

	/**
	 * Gets the host url.
	 *
	 * @param host the host
	 * @param protocol the protocol
	 * @return the host url
	 * @throws MalformedURLException the malformed url exception
	 */
	public static URL getHostUrl(WebHost host, String protocol) 
			throws MalformedURLException {
		if(host == null || protocol == null) {
			return null;
		}
		String baseUrl = protocol + "://" + host.toString();
		return new URL(baseUrl);
	}

	/**
	 * Checks if is sub string.
	 *
	 * @param s the s
	 * @param sub the sub
	 * @return true, if is sub string
	 */
	public static boolean isSubString(String s, String sub) {
		return s.indexOf(sub) >= 0;
	}

	/**
	 * Checks if is xml.
	 *
	 * @param type the type
	 * @return if the mimetype implies an xml file
	 */
	public static boolean isXML(String type) {
		if(type == null) {
			return false;
		}

		if(isSubString(type, "application/xml")) {
			return true;
		}
		if(isSubString(type, "text/xml")) {
			return true;
		}
		if(isSubString(type, "+xml")) {
			return true;
		}
		return false;
	}

	/**
	 * Checks if is html.
	 *
	 * @param type the type
	 * @return true, if is html
	 */
	public static boolean isHTML(String type) {
		if(type == null) {
			return false;
		}

		if(isSubString(type, "text/html")) {
			return true;
		}
		if(isSubString(type, "application/xhtml")) {
			return true;
		}
		return false;
	}

	/**
	 * get a response of "HTML" or "XML" by the raw contentType from 
	 * client response. It returns null if the content type is neither of 
	 * them.
	 *
	 * @param rawType the raw type
	 * @return the html xml type
	 */
	public static String getHtmlXmlType(String rawType) {
		if(isHTML(rawType)) {
			return "HTML";
		} else if (isXML(rawType)) {
			return "XML";
		} else {
			return null;
		}
	}

	/**
	 * convert date from Http format to Date object.
	 *
	 * @param httpDate the http date
	 * @return the date
	 */
	public static Date convertDate(String httpDate) {
		SimpleDateFormat format = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz");
		try {
			return format.parse(httpDate);
		} catch (ParseException e) {
//			System.err.println("Can't parse the date format!");
			return null;
		}
	}

	/**
	 * get base url: for http://www.google.com/abc/foo?c=d#3, base url
	 * is http://www.google.com/abc/foo
	 *
	 * @param url the url
	 * @return the base url
	 */
	public static String getBaseUrl(URL url) {
		if(url == null) {
			throw new IllegalArgumentException();
		}
		return url.toString().split("\\?")[0].split("#")[0];
	}

}
