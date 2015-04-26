/**
 * 
 */
package edu.upenn.cis455.mapreduce.WebClient;

import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.util.Date;
import java.util.HashMap;

import javax.servlet.ServletException;

/**
 * @author dichenli
 * represents a host on the web. A host's key is the combination of
 * its protocol (http or https), host name and port number
 * 
 */
public class WebHost {
	
	public static class HostPort {
		String hostName;
		int port;
		
		public HostPort(String hostName, int port) {
			this.hostName = hostName;
			this.port = port;
		}
	}
	
//	private static int defaultPort = URLUtils.defaultPort;
	private HostPort hostPort;
//	private String hostName;
//	private int port;
	private boolean hasRobots; //if the host has already retrieved robots.txt
	private Date lastCrawl; //last time a request has been sent to this host. null before first crawl
	//http or https, depend on which one the host prefer. 
	//Banks may like https, normal sites like http. Google can do both
	//default is http. 
	private String preferedProtocol; 
//	private String protocol; //HTTP or HTTPS
	
	
	
	
//	public static WebHost getHost(String hostName) {
//		return getHost(hostName, defaultPort);
//	}
	
	
	
	public WebHost(String hostName, int port) {
		if(hostName == null || port < 0) {
			throw new IllegalArgumentException();
		}
		this.hostPort = new HostPort(hostName, port);
//		hostPort.hostName = hostName;
//		hostPort.port = port;
//		this.hostName = hostName;
//		this.port = port;
		this.preferedProtocol = "http";
		this.hasRobots = false;
	}
	
	/**
	 * given an input hostport, example: 158.138.53.72:3000,
	 * parse it and formulate a WebHost object
	 * @param hostPort
	 */
	public static WebHost parseHostPort(String hostPort) {
		String[] splited = hostPort.split(":");
		if(splited.length != 2) {
			throw new IllegalArgumentException("malformatted hostPort!");
		}
		try {
			String hostName = splited[0];
			int port = Integer.parseInt(splited[1]);
			return new WebHost(hostName, port);
		} catch(NumberFormatException e) {
			throw new IllegalArgumentException("malformatted hostPort!");
		}
	}

	
//	private WebHost(String hostName) {
//		this(hostName, defaultPort);
//	}
	
	
	public String getHostName() {
		return hostPort.hostName;
	}
	
	
	public int getPort() {
		return hostPort.port;
	}
	
	//equals if both host name and port number equals
	@Override
	public boolean equals(Object other) {
		if(other == null || !(other instanceof WebHost)) {
			return false;
		}
		
		WebHost otherHost = (WebHost) other;
		return this.toString().equals(otherHost.toString());
	}
	
	///useful for hashmap. Each host should have only one instance
	@Override
	public int hashCode() {
		return toString().hashCode();
	}
	
	/**
	 * example: "localhost:8080", "www.google.com:80"
	 */
	@Override
	public String toString() {
		return hostPort.hostName + ":" + hostPort.port;
	}

	
	public boolean hasRobots() {
		return hasRobots;
	}
	
	
	public void setHasRobots(boolean robotsInfo) {
		this.hasRobots = robotsInfo;
	}
	
	/**
	 * return base url like http://www.google.com/ 
	 * or https://www.google.com:1234/ (if port is not protocol default)
	 * protocol is provided by parameter. If parameter if null,
	 * the default protocol will be used (which is http, and is changed
	 * when previous request has been sent to this host with a certain
	 * protocol).
	 * If the parameter protocol is not http or https, IllegalArgumentException
	 * will be thrown
	 * @param protocol
	 * @return
	 */
	public String getHostUrl(String protocol) {
		if(protocol == null) {
			protocol = preferedProtocol; //default
		}
		if(!WebUtils.isHttpOrHttps(protocol)) {
			throw new IllegalArgumentException();
		}
		protocol = protocol.toLowerCase();
		String hostPort = null;
		if(isDefaultPort(protocol, this.hostPort.port)) {
			hostPort = this.hostPort.hostName;
		} else {
			hostPort = this.toString();
		}
		
		return protocol + "://" + hostPort + "/";
	}
	
	public static boolean isDefaultPort(String protocol, int port) {
		if(protocol.equalsIgnoreCase("http") && port == 80) {
			return true;
		}
		if(protocol.equalsIgnoreCase("https") && port == 443) {
			return true;
		}
		return false;
	}

	
	public String getPreferedProtocol() {
		return preferedProtocol;
	}
	
	
	public void setPreferedProtocol(String protocol) {
		this.preferedProtocol = protocol;
	}

	
	public void setLastCrawl(Date date) {
		this.lastCrawl = date;
	}

	public Date getLastCrawl() {
		return lastCrawl;
	}
	
	
	
}
