/**
 * 
 */
package edu.upenn.cis455.mapreduce.worker;

import java.io.File;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicLong;

import edu.upenn.cis455.mapreduce.Context;

/**
 * @author dichenli
 * The context used for Map
 */
class MapContext implements Context{

	MessageDigest md;
	HashMap<Integer, PeerWorker> peers;
	int numWorkers;
	SHAUtils sha;
	AtomicLong keysWritten;
	
	MapContext(HashMap<Integer, PeerWorker> peers, int numWorkers, AtomicLong keysWritten) {
		this.peers = peers;
		this.numWorkers = numWorkers;
		this.sha = new SHAUtils();
		this.keysWritten = keysWritten;
	}
	
	/**
	 * for a key value pair, use SHA-1 hash to find out the correct peer worker
	 * to send file to, find the file writer, write the pair to that file
	 */
	@Override
	public void write(String key, String value) {
		String line = WorkerUtils.getLine(key, value);
		System.out.println("MapContext: key-value to be written: " + line);
		Integer id = sha.hashSplit(key, numWorkers);
		PeerWorker peer = peers.get(id);
		if(peer == null) {
			System.err.println("Error: can't find peer " + id);
		}
		System.out.println("MapContext.write: peer found: " + peer.peerOut.getAbsolutePath());
		if(!peer.peerOut.exists()) {
			throw new NullPointerException();
		}
		peer.synchronizedMapWrite(line);
		keysWritten.incrementAndGet();
	}
	
}
