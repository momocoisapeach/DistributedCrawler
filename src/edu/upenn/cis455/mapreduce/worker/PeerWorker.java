/**
 * 
 */
package edu.upenn.cis455.mapreduce.worker;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

import Utils.IOUtils;
import edu.upenn.cis455.mapreduce.WebClient.HttpSocketIO;
import edu.upenn.cis455.mapreduce.WebClient.WebClientRequest;
import edu.upenn.cis455.mapreduce.WebClient.WebClientResponse;
import edu.upenn.cis455.mapreduce.WebClient.WebHost;

/**
 * @author dichenli
 * this class represent a worker as a peer. It has a WebHost represents where
 * the peer is listening, a file describing a file in spool-out folder (to be
 * sent to Peer's spool-in) 
 */
class PeerWorker {
	static int myId; //the Id of myself
	
	int id; //id number of this peer, from 1 to numWorkers
	WebHost host;
	File peerOut; //a mail to be sent to the peer (map result from spool-out to spool-in)
	private PrintWriter outWriter; // write to the peerOut
	boolean myself = false; //true if this peer worker is myself


	PeerWorker(int id, WebHost host, File peerOut, boolean myself) throws IOException {
		if(peerOut == null || !peerOut.exists() || peerOut.isDirectory()) {
			System.err.println("PeerWorker(): wrong file status!");
			throw new IllegalArgumentException();
		}
		if(id <= 0 || host == null) {
			throw new IllegalArgumentException();
		}

		this.id = id;
		this.host = host;
		this.peerOut = peerOut;
		System.out.println("PeerWorker(): peerOut is: " + peerOut.getAbsolutePath());
		if(!peerOut.exists() || !peerOut.isFile()) {
			System.err.println("PeerWorker(): peerOut is not an existing file!");
			throw new IllegalArgumentException();
		}
		this.myself = myself;
		if(myself) {
			myId = id;
		}
		if(!peerOut.exists()) {
			throw new NullPointerException();
		}
		try {			
			this.outWriter = IOUtils.getWriter(peerOut);
		} catch (IOException e) {
			e.printStackTrace();
			throw e;
		}
	}

	/**
	 * enter a synchronized block to write a line to the peerOut, used for map
	 * phase, after the map function emits a key-value pair to a peer
	 * synchronized is used to prevent race condition writing to the same file
	 * @param writer
	 * @param lines
	 */
	void synchronizedMapWrite(String line) {
		if(!peerOut.exists()) {
			throw new NullPointerException();
		}
		synchronized(outWriter) {
			System.out.println("synchronizedMapWrite write line: " + line);
			outWriter.println(line);
			System.out.println("synchronizedMapWrite file size: " + peerOut.length());
		}
	}

	void closeOutput() {
		if(!peerOut.exists()) {
			throw new NullPointerException();
		}
		outWriter.close();
		System.out.println("closeOutput() file size: " + peerOut.length());
	}

	/**
	 * send file from current worker to the peer
	 * @return true if response is successful
	 */
	public boolean sendFile() {
		if(!peerOut.exists()) {
			throw new NullPointerException();
		}
		System.out.println("PeerWorker sendFile()...");
		String method = "POST";
		String url = "http://" + host.toString() + "/pushdata";
		String userAgent = "worker" + id;
		WebClientRequest request = WebClientRequest.getWebClientRequest(url, method, userAgent);
		WebClientResponse response = new HttpSocketIO().sendFileRequest(request, peerOut);
		if(response == null) {
			System.err.println("PeerWorker.sendFile failed, response null");
			return false;
		}
		
		if(response.getStatusCode() == 200) {
			return true;
		} else {
			System.err.println("PeerWorker.sendFile failed, error code: " + response.getStatusCode());
			return false;
		}
	}
	
}
