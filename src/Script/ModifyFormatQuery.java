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

/**
 * @author dichenli
 * Modify format of crawled data
 */
public class ModifyFormatQuery extends Thread {


	static String[] addresses = {
		"ec2-user@52.24.10.246", //0
		"ec2-user@52.10.106.69",
		"ec2-user@52.24.60.48",
		"ec2-user@52.24.41.98",
		"ec2-user@52.24.53.215",

		"ec2-user@52.24.52.239",//5
		"ec2-user@52.24.19.231",
		"ec2-user@52.11.164.47",
		"ec2-user@52.24.47.203",
		"ec2-user@52.24.88.140",
		"ec2-user@52.24.39.227",//10

		"ec2-user@52.24.55.102",
		"ec2-user@52.11.211.241",
		"ec2-user@52.24.42.170",
		"ec2-user@52.24.43.215",
		"ec2-user@52.11.140.135",//15
		"ec2-user@52.24.43.249",
		"ec2-user@52.10.106.69"//17
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

	static String local_home = "~/";
	static String local_ec2_home = local_home + ".ec2/";
	static String local_aws_home = local_home + ".aws/";
	static String local_download = local_home + "Downloads/";
//	static String upload_data = "crawl_data";
//	static String upload_zip = "crawl_data.zip";
	static String key_pem = local_ec2_home + "ec2try.pem";
	static String berkeley_cleaner = "berkeley_cleaner";

	static String remote_home = "/home/ec2-user/";
//	static String remote_crawl_data = remote_home + "crawl_data/";
	static String remote_jar_dir = remote_home;
	static String jar_name = "MergeLine.jar"; 
	static String input_filename = "final"; 
	static String output_filename = "crawler"; 
//	static long crawl_limit = 7000;
//	static int size = 1; //Mb data
	static int crawler_num = 18;

	static AtomicInteger readyCount = null; //atomic integer to transfer signal among threads

	int i; //crawler number
	JschCommander commander;

	@Override
	public void run() {

		System.out.println("Thread start: " + i);
		boolean success = true;
		PrintWriter writer = null;

		//create script to initiate crawl
		String filenameEC2_run = "run_refromat" + i;
		File ec2_run = new File(local_ec2_home + filenameEC2_run);
		success &= IOUtils.createFile(ec2_run);
		if(!success) {
			System.err.println("create file ec2 failed");
			return;
		}
		try {
			writer = IOUtils.getWriter(ec2_run);
			writer.println("java -jar " + remote_jar_dir + jar_name + " " + input_filename + i + " " + i + output_filename);
			writer.println("s3cmd sync crawl_data/" + i + output_filename + " --preserve s3://mergecrawlercontent/");
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
//			writer.println("scp -i " + key_pem + " " + local_download + upload_zip + " " + addresses[i] + ":~/");
//			writer.println("scp -i " + key_pem + " " + aws_home + "credentials " + addresses[i] + ":~/credentials");
			writer.println("scp -i " + key_pem + " " + local_ec2_home + berkeley_cleaner + " " + addresses[i] + ":~/");
			writer.println("scp -i " + key_pem + " " + local_ec2_home + filenameEC2_run + " " + addresses[i] + ":~/");
			String line = "scp -i " + key_pem + " " + "~/.ec2/git_script_mergeline" + " " + addresses[i] + ":~/";
			System.out.println(line);
			writer.println(line);
//			writer.println("scp -i " + key_pem + " " + "~/.ec2/cleaner" + " " + addresses[i] + ":~/");
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
			commander.execute("./" + berkeley_cleaner);
			commander.execute("./git_script_mergeline");
			commander.execute("./" + filenameEC2_run);
			commander.disconnect();
			int count = readyCount.get();
			if (count < 0) {
				System.out.println("Error from other ec2");
//				return;
			}
			readyCount.incrementAndGet();
		} catch (JSchException | IOException | InterruptedException e) {
			e.printStackTrace();
			System.out.println("Thread " + i + " got error! Exit");
			readyCount.set(-1);
			return;
		}
	}

	/**
	 * @param args
	 * @throws IOException 
	 * @throws InterruptedException 
	 */
	public static void main(String[] args) throws IOException, InterruptedException {
		if(addresses.length != crawler_num ) {
			System.err.println("wrong seeds or addresses!");
			return;
		}

		readyCount = new AtomicInteger(0);
		boolean success = true;

		//		PrintWriter writer = null;
		ModifyFormatQuery[] scripts = new ModifyFormatQuery[crawler_num];
		for (int i = 0; i < crawler_num; i++) {
			scripts[i] = new ModifyFormatQuery();
			scripts[i].i = i;
			scripts[i].start();
		}
		for(ModifyFormatQuery sc : scripts) {
			sc.join();
		}

		System.out.println("====done====");
		
	}

}
