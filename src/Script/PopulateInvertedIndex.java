/**
 * 
 */
package Script;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicInteger;

import com.jcraft.jsch.JSchException;

import Utils.IOUtils;

// TODO: Auto-generated Javadoc
/**
 * The Class PopulateInvertedIndex.
 *
 * @author dichenli
 * Modify format of crawled data
 */
public class PopulateInvertedIndex extends Thread {


	/** The addresses. */
	static String[] addresses = {
		"ec2-user@52.24.60.12", //0
		"ec2-user@52.24.37.11",
		"ec2-user@52.24.42.228",
		"ec2-user@52.24.28.79",
		"ec2-user@52.24.26.194",

		"ec2-user@52.10.66.78",//5
		"ec2-user@52.24.46.29",
		"ec2-user@52.24.62.195",
		"ec2-user@52.24.62.201",
		"ec2-user@52.24.70.144",
		"ec2-user@52.24.59.13",//10

		"ec2-user@52.24.55.80",
		"ec2-user@52.24.53.106",
		"ec2-user@52.24.56.130",
		"ec2-user@52.24.55.208",
		"ec2-user@52.24.29.82"//15
		};

//	static String[] seeds = {
//		"http://www.dmoz.org/", //0
//		"http://www.aol.com/",
//		"http://www.incrawler.com/",
//		"http://news.google.com/",
//		"http://www.gimpsy.com/",
//		"http://www.msn.com/", //5
//		"https://twitter.com/?lang=en",
//		"https://en.wikipedia.org/wiki/Main_Page",
//		"https://www.techmeme.com/",
//		"https://business.yahoo.com/",
//		"http://news.google.com/", //10
//		"http://www.gimpsy.com/",
//		"http://www.msn.com/",
//		"https://twitter.com/?lang=en",
//		"https://en.wikipedia.org/wiki/Main_Page",
//		"https://www.techmeme.com/", //15
//		"https://business.yahoo.com/",
//		"http://www.aol.com/" //17
//	};

	/** The local_home. */
static String local_home = "/Users/dichenli/";
	
	/** The local_ec2_home. */
	static String local_ec2_home = local_home + ".ec2/";
	
	/** The local_aws_home. */
	static String local_aws_home = local_home + ".aws/";
	
	/** The local_download. */
	static String local_download = local_home + "Downloads/";
//	static String upload_data = "crawl_data";
//	static String upload_zip = "crawl_data.zip";
	/** The key_pem. */
static String key_pem = local_ec2_home + "ec2try.pem";
	
	/** The berkeley_cleaner. */
	static String berkeley_cleaner = "berkeley_cleaner";

	/** The remote_home. */
	static String remote_home = "/home/ec2-user/";
	
	/** The project_name. */
	static String project_name = "DynamoDB555";
	
	/** The remote_jar_dir. */
	static String remote_jar_dir = remote_home + project_name + "/";
	
	/** The jar_name. */
	static String jar_name = "DataPopulator.jar"; 
//	static long crawl_limit = 7000;
//	static int size = 1; //Mb data
	/** The crawler_num. */
static int crawler_num = 16;

	/** The ready count. */
	static AtomicInteger readyCount = null; //atomic integer to transfer signal among threads

	/** The i. */
	int i; //crawler number
	
	/** The commander. */
	JschCommander commander;

	/* (non-Javadoc)
	 * @see java.lang.Thread#run()
	 */
	@Override
	public void run() {

		System.out.println("Thread start: " + i);
		boolean success = true;
		PrintWriter writer = null;

		//create script to initiate crawl
		String filenameEC2_run = "run_populate" + i;
		File ec2_run = new File(local_ec2_home + filenameEC2_run);
		success &= IOUtils.createFile(ec2_run);
		if(!success) {
			System.err.println("create file ec2 failed");
			return;
		}
		try {
			writer = IOUtils.getWriter(ec2_run);
			writer.println("java -jar " + remote_jar_dir + jar_name + " " + i);
			writer.close();
			IOUtils.setFilePermission(ec2_run, 7, 0, 0);
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}

		//create script to do file upload to ec2
		String filenameUpload = local_ec2_home + "upload_formater" + i;
		File uploadScript = new File(filenameUpload);

		success &= IOUtils.createFile(uploadScript);
		if(!success) {
			System.err.println("create file crawler failed");
			return;
		}

		try {
			writer = IOUtils.getWriter(uploadScript);
//			writer.println("scp -i " + key_pem + " " + local_ec2_home + berkeley_cleaner + " " + addresses[i] + ":~/");
			writer.println("scp -i " + key_pem + " " + local_ec2_home + filenameEC2_run + " " + addresses[i] + ":~/");
			writer.println("scp -i " + key_pem + " " + "~/.ec2/git_script_inverted_index" + " " + addresses[i] + ":~/");
			writer.close();
			IOUtils.setFilePermission(uploadScript, 7, 0, 0);
			System.out.println("upload for crawler: " + i);
			boolean rv = IOUtils.runtimeExec(uploadScript.getAbsolutePath());
			if(!rv) {
				System.err.println("upload error");
			}
			System.out.println("upload for crawler done");
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}

		//create files to ssh to crawlers
		String filenameSSH = local_ec2_home + "crawler" + i;
		File ssh = new File(filenameSSH);
		try {
			IOUtils.createFile(ssh);
			writer = IOUtils.getWriter(ssh);
			writer.println("ssh -i " + "ec2try.pem" + " " + addresses[i]);
			writer.close();
			IOUtils.setFilePermission(ssh, 7, 0, 0); 
		} catch (IOException e) {
			e.printStackTrace();
			return ;
		}

		String[] hostip = addresses[i].split("@");
		String host = hostip[0];
		String ip = hostip[1];

		try {
			JschCommander commander = new JschCommander(host, ip, i);
			commander.execute("./git_script_inverted_index");
//			commander.execute("./" + filenameEC2_run);
			commander.disconnect();
			int count = readyCount.get();
			if (count < 0) {
				System.out.println("Error from other ec2");
//				return;
			}
			readyCount.incrementAndGet();
		} catch (/*JSchException | IOException | InterruptedException*/Exception e) {
			e.printStackTrace();
			System.out.println("Thread " + i + " got error! Exit");
			readyCount.set(-1);
			return;
		}
	}

	/**
	 * The main method.
	 *
	 * @param args the arguments
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws InterruptedException the interrupted exception
	 */
	public static void main(String[] args) throws IOException, InterruptedException {
//		if(addresses.length != crawler_num ) {
//			System.err.println("wrong seeds or addresses!");
//			return;
//		}

		readyCount = new AtomicInteger(0);
		boolean success = true;

		//		PrintWriter writer = null;
		PopulateInvertedIndex[] scripts = new PopulateInvertedIndex[crawler_num];
		for (int i = 0; i < crawler_num; i++) {
			scripts[i] = new PopulateInvertedIndex();
			scripts[i].i = i;
			scripts[i].start();
		}
		for(PopulateInvertedIndex sc : scripts) {
			sc.join();
		}

		System.out.println("====done====");
		
	}

}
