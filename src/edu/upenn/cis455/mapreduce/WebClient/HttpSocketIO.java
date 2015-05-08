/**
 * 
 */
package edu.upenn.cis455.mapreduce.WebClient;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import Utils.IOUtils;


// TODO: Auto-generated Javadoc
//import sun.security.krb5.internal.HostAddress;
//
//import com.sleepycat.je.rep.elections.Protocol.Shutdown;

/**
 * The Class HttpSocketIO.
 *
 * @author dichenli
 * use socket to achieve IO from HTTP host
 */
public class HttpSocketIO {

	/** The socket. */
	private Socket socket;
	
	/** The in. */
	private BufferedReader in;
	
	/** The out. */
	private PrintWriter out;
	
	/** The request. */
	private WebClientRequest request;
	
	//there is no constructors yet, all fields will be initialized when 
	//sendRequest() is called
	//in fact, the fields of the class is kind of useless, unless
	//in the future we will implement persistent connection

	/**
	 * Gets the socket.
	 *
	 * @param host the host
	 * @return the socket
	 * @throws UnknownHostException the unknown host exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	static Socket getSocket(WebHost host) 
			throws UnknownHostException, IOException {
		if(host == null) {
			throw new IllegalArgumentException();
		}
		return getSocket(host.getHostName(), host.getPort());
	}

	/**
	 * Gets the socket.
	 *
	 * @param hostName the host name
	 * @param portNumber the port number
	 * @return the socket
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	static Socket getSocket(String hostName, int portNumber) 
			throws IOException {
		return new Socket(hostName, portNumber);
	}

	/**
	 * get reader from a socket.
	 *
	 * @param socket the socket
	 * @return the reader
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	static BufferedReader getReader(
			Socket socket) throws IOException {
		InputStream is = socket.getInputStream();
		return IOUtils.getReader(is);
	}

	/**
	 * get writer from a a socket, suitable for writing characters.
	 *
	 * @param socket the socket
	 * @return the writer
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	static PrintWriter getWriter(
			Socket socket) throws IOException {
		OutputStream os = socket.getOutputStream();
		return IOUtils.getWriter(os);
	}
	

	/**
	 * Close connection.
	 *
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	void closeConnection() throws IOException {
//		System.out.println("Socket close connection!!");
		if(in != null) {
			in.close();
			in = null;
		}
		if(out != null) {
			out.close();
			out = null;
		}
		if(socket != null) {
			socket.close();
			socket = null;
		}
	}


	/**
	 * try to connect to the given host. calling this method
	 * will also reset existing connection!
	 *
	 * @param host the host
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	void connect(WebHost host) throws IOException {
		closeConnection(); //close old connection first
		socket = getSocket(host);
//		System.out.println("Get socket successful");
		this.in = getReader(socket);
		this.out = getWriter(socket);
//		System.out.println("Get HttpSocketIO in and out successful");
	}

	/**
	 * close output but still keep input open 
	 * after output is closed, it send clear message 
	 * to the server that the request is finished.
	 * If out.close() is called, it will also close 
	 * the socket and the associated input
	 * so shutdownOutput is called instead.
	 * shudownOutput() doesn't flush buffer
	 *
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	void flipIO() throws IOException {
//		System.out.println("socket shutdownOutput!");
		out.flush();
		socket.shutdownOutput();
	}


	/**
	 * send request to remote server, returns null if send fail.
	 *
	 * @param request the request
	 * @return the web client response
	 */
	public WebClientResponse sendRequest(WebClientRequest request) {
		if (request == null) {
			return null;
		}
		this.request = request;
		//set the time request is sent, so that when next time the request is sent
		//we can check whether it follows robots.txt crawl delay rule
		request.setLastCrawl(new Date());
		String requestString = request.getFullRequest();
		System.out.println("request to be sent: " + requestString);
		if (requestString == null) {
			return null;
		}
		
		try {
			connect(request.getHost());
		} catch (IOException e) {
			return null;
		}
		
		if(!sendRequest(requestString)) {
			return null;
		}
		return getResponse();
	}
	
	
	
	/**
	 * send a complete request text to remote server, returns whether send success.
	 *
	 * @param request the request
	 * @return if true is send successful
	 */
	private boolean sendRequest(String request) {
		if(request == null) {
			return false;
		}
//		System.out.println("Http IO get writer...");
//		PrintWriter writer = null;
		if(out == null) {
			return false;
		} 
		System.out.println("send request...");
		out.write(request);
		try {
			flipIO();
		} catch (IOException e) {
			System.err.println("ioInterface.closeOutput() exception");
			return false;
		}
		System.out.println("send request success!");
		return true;
	}
	
	/**
	 * similar to sendRequest, but suitable for sending file as response body when
	 * file size is too large for memory. In this case, the request body in
	 * the request object will be ignored. Only the file content is considered
	 * the request body
	 *
	 * @param request the request
	 * @param file the file
	 * @return the web client response
	 */
	public WebClientResponse sendFileRequest(WebClientRequest request, File file) {
		System.out.println("HttpSocketIO.sendFileRequest: send file: " + file.getAbsolutePath());
		if(!IOUtils.isValidFile(file) || request == null) {
			System.err.println("HttpSocketIO.sendFileRequest: invalid request or file");
			return null;
		}
		
		this.request = request;
		//set the time request is sent, so that when next time the request is sent
		//we can check whether it follows robots.txt crawl delay rule
		request.setLastCrawl(new Date());
		request.setContentLength(file.length());
		String requestString = request.getRequestHead();
		System.out.println("file request to be sent: " + requestString);
		if (requestString == null) {
			return null;
		}
		
		try {
			connect(request.getHost());
		} catch (IOException e) {
			return null;
		}
		
		if(!sendFileRequest(requestString, file)) {
			return null;
		}
		return getResponse();
	}
	
	/**
	 * Send file request.
	 *
	 * @param request the request
	 * @param file the file
	 * @return true, if successful
	 */
	private boolean sendFileRequest(String request, File file) {
		if(request == null || file == null || !file.exists() || !file.isFile()) {
			return false;
		}
		if(out == null) {
			return false;
		} 
		
		Scanner sc = null;
		try {
			sc = new Scanner(file);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return false;
		}
		
		out.write(request);
		while(sc.hasNextLine()) {
			out.println(sc.nextLine());
		}
		try {
			sc.close();
			flipIO();
		} catch (IOException e) {
			System.err.println("ioInterface.closeOutput() exception");
			return false;
		}
		System.out.println("send request success!");
		return true;
	}

	/**
	 * try to get response after a request is sent.
	 * The method must be called after sendRequest() is called, otherwise
	 * it will fail miserably
	 * Returns null if get response failed (like IO exception)
	 *
	 * @return the response
	 */
	private WebClientResponse getResponse() {
		if(in == null) {
//			System.err.println("WebClientIO.getResponse(): reader == null");
			return null;
		}
		
		return readResponse(in);
	}

	/**
	 * Read response.
	 *
	 * @param reader the reader
	 * @return the web client response
	 */
	WebClientResponse readResponse(BufferedReader reader) {
		StringWriter head = new StringWriter();
		StringWriter body = new StringWriter();
		int emptyLineCount = 0; //number of empty lines encountered during reading
//		System.out.println("start reading response...");
		while(true) {
			String line = null;
			try {
				line = reader.readLine();
//				System.out.println("read line: " + line);
			} catch (IOException e) {
				return null;
			}
			if(line == null) {
//				System.out.println("reader.readLine() has line == null");
				break;
			}
			//reader.readLine() will strip off the "\r\n", so I have it appended again
			if(emptyLineCount == 0) {
				head.write(line + "\r\n");
			} else {
				body.write(line + "\r\n");
			}
			if(line.equals("") && emptyLineCount == 0) {
				emptyLineCount++;
			}
		}
		try {
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		WebClientResponse response = new WebClientResponse(request);
		//parse head, if exception happens then something goes wrong
		//for example, maybe the response head format can't be understood by the method
		//in this case we need to debug it
		if(!parseResponseHead(head.toString(), response)) {
			return null;
		}
		
//		response.setMethod(request.getMethod());
		if(!request.isHeadMethod()) {
			response.setBody(body.toString());	
		}
		return response;
	}

	/**
	 * Parses the response head.
	 *
	 * @param head the head
	 * @param response the response
	 * @return if parse response head successful
	 */
	static boolean parseResponseHead(String head, WebClientResponse response) {
		if(head == null || response == null) {
			return false;
		}
		BufferedReader headReader = new BufferedReader(new StringReader(head));
		String inputLine = null;
		try {
			inputLine = headReader.readLine();
		} catch (IOException e1) {
			return false;
		}

		if(inputLine == null) {
//			System.err.println("parseResponseHead(): inputLine == null, Can't read line from server");
			return false;
		}

//		System.out.println("===Response===\n" + inputLine);
		try {
			matchFirstLine(inputLine, response);
		} catch (Exception e) {
//			System.err.println(e.getMessage());
//			System.out.println("First line not matched! " + e.getMessage());
			return false;
		}
		
		try {
			//read until we have exhausted head
			while (true) {
				inputLine = headReader.readLine();
//				System.out.println("head: " + inputLine);
				//it will break 
				if(inputLine == null || inputLine.equals("")) {
					break;
				}

				parseResponseLine(inputLine, response);
			}
		} catch (IOException e) {
//			System.err.println("Read input line exception!");
			return false;
		}
		return true;
	}
	
	/**
	 * Match first line.
	 *
	 * @param input the input
	 * @param response the response
	 * @throws Exception the exception
	 */
	static void matchFirstLine(String input, WebClientResponse response) throws Exception {
		//try to match "HTTP/1.1 200 OK"
		String pattern = "(HTTP/1.1|HTTP/1.0)[ |\t]+(\\d{3})[ |\t]+(.*)";
		Pattern p = Pattern.compile(pattern);
		Matcher firstLine = p.matcher(input);
		if(!firstLine.matches()) {
			throw new Exception("Illegal Response first line\n");
		}
		response.setProtocol(firstLine.group(1));
		response.setStatusCode(Integer.parseInt(firstLine.group(2)));
	}
	
	/**
	 * Parses the response line.
	 *
	 * @param line the line
	 * @param response the response
	 * @return true, if successful
	 */
	static boolean parseResponseLine(String line, WebClientResponse response) {
		//		System.out.println(line);
		String[] separated = line.split(":", 2);
		if(separated.length < 2) {
//			System.err.println("This line in response head can't be understand: " + line);
			return false;
		}
		String header = separated[0].trim();
		String value = separated[1].trim();
		response.putHeader(header, value);

		switch(header.toLowerCase()) {
		case "content-type":
			response.setContentType(value);
			break;
		case "content-length":
			response.setContentLength(Integer.parseInt(value));
			break;
		case "date":
			response.setDate(WebUtils.convertDate(value));
			break;
		case "last-modified":
			response.setLastModified(WebUtils.convertDate(value));
			break;
		default:
			break;
		}
		return true;
	}

	
}
