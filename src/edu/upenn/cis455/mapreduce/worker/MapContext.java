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

// TODO: Auto-generated Javadoc
/**
 * The Class MapContext.
 *
 * @author dichenli
 * The context used for Map
 */
class MapContext implements Context{

	/** The md. */
	MessageDigest md;
	
	/** The peers. */
	HashMap<Integer, PeerWorker> peers;
	
	/** The num workers. */
	int numWorkers;
	
	/** The sha. */
	SHAUtils sha;
	
	/** The keys written. */
	AtomicLong keysWritten;
	
	/**
	 * Instantiates a new map context.
	 *
	 * @param peers the peers
	 * @param numWorkers the num workers
	 * @param keysWritten the keys written
	 */
	MapContext(HashMap<Integer, PeerWorker> peers, int numWorkers, AtomicLong keysWritten) {
		this.peers = peers;
		this.numWorkers = numWorkers;
		this.sha = new SHAUtils();
		this.keysWritten = keysWritten;
	}
	
	/**
	 * for a key value pair, use SHA-1 hash to find out the correct peer worker
	 * to send file to, find the file writer, write the pair to that file.
	 *
	 * @param key the key
	 * @param value the value
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
