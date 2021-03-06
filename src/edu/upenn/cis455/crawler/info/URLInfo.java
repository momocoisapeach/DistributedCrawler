package edu.upenn.cis455.crawler.info;

// TODO: Auto-generated Javadoc
/**
 * The Class URLInfo.
 */
public class URLInfo {
	
	/** The host name. */
	private String hostName;
	
	/** The port no. */
	private int portNo;
	
	/** The file path. */
	private String filePath;
	
	/**
	 * Constructor called with raw URL as input - parses URL to obtain host name and file path.
	 *
	 * @param docURL the doc url
	 */
	public URLInfo(String docURL){
		if(docURL == null || docURL.equals(""))
			return;
		docURL = docURL.trim();
		if(!docURL.startsWith("http://") || docURL.length() < 8)
			return;
		// Stripping off 'http://'
		docURL = docURL.substring(7);
		/*If starting with 'www.' , stripping that off too
		if(docURL.startsWith("www."))
			docURL = docURL.substring(4);*/
		int i = 0;
		while(i < docURL.length()){
			char c = docURL.charAt(i);
			if(c == '/')
				break;
			i++;
		}
		String address = docURL.substring(0,i);
		if(i == docURL.length())
			filePath = "/";
		else
			filePath = docURL.substring(i); //starts with '/'
		if(address.equals("/") || address.equals(""))
			return;
		if(address.indexOf(':') != -1){
			String[] comp = address.split(":",2);
			hostName = comp[0].trim();
			try{
				portNo = Integer.parseInt(comp[1].trim());
			}catch(NumberFormatException nfe){
				portNo = 80;
			}
		}else{
			hostName = address;
			portNo = 80;
		}
	}
	
	/**
	 * Instantiates a new URL info.
	 *
	 * @param hostName the host name
	 * @param filePath the file path
	 */
	public URLInfo(String hostName, String filePath){
		this.hostName = hostName;
		this.filePath = filePath;
		this.portNo = 80;
	}
	
	/**
	 * Instantiates a new URL info.
	 *
	 * @param hostName the host name
	 * @param portNo the port no
	 * @param filePath the file path
	 */
	public URLInfo(String hostName,int portNo,String filePath){
		this.hostName = hostName;
		this.portNo = portNo;
		this.filePath = filePath;
	}
	
	/**
	 * Gets the host name.
	 *
	 * @return the host name
	 */
	public String getHostName(){
		return hostName;
	}
	
	/**
	 * Sets the host name.
	 *
	 * @param s the new host name
	 */
	public void setHostName(String s){
		hostName = s;
	}
	
	/**
	 * Gets the port no.
	 *
	 * @return the port no
	 */
	public int getPortNo(){
		return portNo;
	}
	
	/**
	 * Sets the port no.
	 *
	 * @param p the new port no
	 */
	public void setPortNo(int p){
		portNo = p;
	}
	
	/**
	 * Gets the file path.
	 *
	 * @return the file path
	 */
	public String getFilePath(){
		return filePath;
	}
	
	/**
	 * Sets the file path.
	 *
	 * @param fp the new file path
	 */
	public void setFilePath(String fp){
		filePath = fp;
	}
	
}
