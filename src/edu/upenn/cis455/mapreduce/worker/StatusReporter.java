/**
 * 
 */
package edu.upenn.cis455.mapreduce.worker;

import edu.upenn.cis455.mapreduce.WebClient.HttpSocketIO;
import edu.upenn.cis455.mapreduce.WebClient.WebClientRequest;
import edu.upenn.cis455.mapreduce.WebClient.WebClientRequestTest;
import edu.upenn.cis455.mapreduce.WebClient.WebClientResponse;

// TODO: Auto-generated Javadoc
/**
 * The Class StatusReporter.
 *
 * @author dichenli
 * A runnable that reports worker status to master
 */
class StatusReporter implements Runnable {

	/** The worker. */
	private Worker worker;
	
	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		while(true) {
			synchronized (worker.running) { //happens when server turns off
				if(!worker.running) {
					return;
				}
			}
			reportStatus();
			try {
				Thread.sleep(10 * 1000); //wait for 10 seconds
			} catch (InterruptedException e) {
				//nothing to do
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Instantiates a new status reporter.
	 *
	 * @param worker the worker
	 */
	StatusReporter(Worker worker) {
		this.worker = worker;
	}

	/**
	 * send status report to master.
	 */
	void reportStatus() {
		String url = reportStatusUrl();
		String method = "GET";
		String userAgent = worker.userAgent;
		WebClientRequest request = WebClientRequest.getWebClientRequest(url, method, userAgent);
		System.out.println("=======Worker statusReporter Request=======\n" + request.getFullRequest());
		WebClientResponse response = new HttpSocketIO().sendRequest(request);
		if(response != null) {			
			System.out.println("=======Worker statusReporter Response=======\n" + response.getHead() + "\n\n" + response.getBody());
		}
		//nothing more to do with response?
	}
	
	/**
	 * Report status url.
	 *
	 * @return the string
	 */
	private String reportStatusUrl() {
		return "http://" + worker.master.toString() 
				+ "/workerstatus" + statusQueryString();
	}
	
	/**
	 * Status query string.
	 *
	 * @return the string
	 */
	private String statusQueryString() {
		return "?port=" + worker.listenPort + "&status=" + worker.getStatus()
				+ "&job=" + worker.getJobClass() + "&keysRead=" + worker.getKeysRead()
				+ "&keysWritten=" + worker.getKeysWritten();
	}

}
