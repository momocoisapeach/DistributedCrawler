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

// TODO: Auto-generated Javadoc
/**
 * The Class WebHost.
 *
 * @author dichenli
 * represents a host on the web. A host's key is the combination of
 * its protocol (http or https), host name and port number
 */
public class WebHost {
	
	/**
	 * The Class HostPort.
	 */
	public static class HostPort {
		
		/** The host name. */
		String hostName;
		
		/** The port. */
		int port;
		
		/**
		 * Instantiates a new host port.
		 *
		 * @param hostName the host name
		 * @param port the port
		 */
		public HostPort(String hostName, int port) {
			this.hostName = hostName;
			this.port = port;
		}
	}
	
//	private static int defaultPort = URLUtils.defaultPort;
	/** The host port. */
private HostPort hostPort;
//	private String hostName;
//	private int port;
	/** The has robots. */
private boolean hasRobots; //if the host has already retrieved robots.txt
	
	/** The last crawl. */
	private Date lastCrawl; //last time a request has been sent to this host. null before first crawl
	//http or https, depend on which one the host prefer. 
	//Banks may like https, normal sites like http. Google can do both
	//default is http. 
	/** The prefered protocol. */
	private String preferedProtocol; 
//	private String protocol; //HTTP or HTTPS
	
	
	
	
//	public static WebHost getHost(String hostName) {
//		return getHost(hostName, defaultPort);
//	}
	
	
	
	/**
 * Instantiates a new web host.
 *
 * @param hostName the host name
 * @param port the port
 */
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
	 *
	 * @param hostPort the host port
	 * @return the web host
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
	
	
	/**
 * Gets the host name.
 *
 * @return the host name
 */
public String getHostName() {
		return hostPort.hostName;
	}
	
	
	/**
	 * Gets the port.
	 *
	 * @return the port
	 */
	public int getPort() {
		return hostPort.port;
	}
	
	//equals if both host name and port number equals
	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object other) {
		if(other == null || !(other instanceof WebHost)) {
			return false;
		}
		
		WebHost otherHost = (WebHost) other;
		return this.toString().equals(otherHost.toString());
	}
	
	///useful for hashmap. Each host should have only one instance
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return toString().hashCode();
	}
	
	/**
	 * example: "localhost:8080", "www.google.com:80"
	 *
	 * @return the string
	 */
	@Override
	public String toString() {
		return hostPort.hostName + ":" + hostPort.port;
	}

	
	/**
	 * Checks for robots.
	 *
	 * @return true, if successful
	 */
	public boolean hasRobots() {
		return hasRobots;
	}
	
	
	/**
	 * Sets the checks for robots.
	 *
	 * @param robotsInfo the new checks for robots
	 */
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
	 *
	 * @param protocol the protocol
	 * @return the host url
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
	
	/**
	 * Checks if is default port.
	 *
	 * @param protocol the protocol
	 * @param port the port
	 * @return true, if is default port
	 */
	public static boolean isDefaultPort(String protocol, int port) {
		if(protocol.equalsIgnoreCase("http") && port == 80) {
			return true;
		}
		if(protocol.equalsIgnoreCase("https") && port == 443) {
			return true;
		}
		return false;
	}

	
	/**
	 * Gets the prefered protocol.
	 *
	 * @return the prefered protocol
	 */
	public String getPreferedProtocol() {
		return preferedProtocol;
	}
	
	
	/**
	 * Sets the prefered protocol.
	 *
	 * @param protocol the new prefered protocol
	 */
	public void setPreferedProtocol(String protocol) {
		this.preferedProtocol = protocol;
	}

	
	/**
	 * Sets the last crawl.
	 *
	 * @param date the new last crawl
	 */
	public void setLastCrawl(Date date) {
		this.lastCrawl = date;
	}

	/**
	 * Gets the last crawl.
	 *
	 * @return the last crawl
	 */
	public Date getLastCrawl() {
		return lastCrawl;
	}
	
	
	
}
