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
public class PopulateDocURLTitle extends Thread {


	static String[] addresses = {
		"ec2-user@52.24.28.94", //0
		"ec2-user@52.11.86.163",
		"ec2-user@52.24.44.221"
		};


	static String local_home = "/Users/dichenli/";
	static String local_ec2_home = local_home + ".ec2/";
	static String local_aws_home = local_home + ".aws/";
	static String key_pem = local_ec2_home + "ec2try.pem";
	static String berkeley_cleaner = "berkeley_cleaner";

	static String remote_home = "/home/ec2-user/";
	static String project_name = "DynamoDB555";
	static String remote_jar_dir = remote_home + project_name + "/";
	static String jar_name = "TitlePopulator.jar"; 
//	static long crawl_limit = 7000;
//	static int size = 1; //Mb data
	static int worker_num = 3;

	static AtomicInteger readyCount = null; //atomic integer to transfer signal among threads

	int i; //worker number
	JschCommander commander;

	@Override
	public void run() {

		System.out.println("Thread start: " + i);
		boolean success = true;
		PrintWriter writer = null;

		//create script to initiate crawl
		String filenameEC2_run = "run_title_pop" + i;
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
			System.err.println("create file worker failed");
			return;
		}

		try {
			writer = IOUtils.getWriter(uploadScript);
//			writer.println("scp -i " + key_pem + " " + local_ec2_home + berkeley_cleaner + " " + addresses[i] + ":~/");
			writer.println("scp -i " + key_pem + " " + local_ec2_home + filenameEC2_run + " " + addresses[i] + ":~/");
			writer.println("scp -i " + key_pem + " " + "~/.ec2/git_script_inverted_index" + " " + addresses[i] + ":~/");
			writer.close();
			IOUtils.setFilePermission(uploadScript, 7, 0, 0);
			System.out.println("upload for worker: " + i);
			boolean rv = IOUtils.runtimeExec(uploadScript.getAbsolutePath());
			if(!rv) {
				System.err.println("upload error");
			}
			System.out.println("upload for worker done");
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}

		//create files to ssh to workers
		String filenameSSH = local_ec2_home + "worker" + i;
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
	 * @param args
	 * @throws IOException 
	 * @throws InterruptedException 
	 */
	public static void main(String[] args) throws IOException, InterruptedException {
//		if(addresses.length != worker_num ) {
//			System.err.println("wrong seeds or addresses!");
//			return;
//		}

		readyCount = new AtomicInteger(0);
		boolean success = true;

		//		PrintWriter writer = null;
		PopulateDocURLTitle[] scripts = new PopulateDocURLTitle[worker_num];
		for (int i = 0; i < worker_num; i++) {
			scripts[i] = new PopulateDocURLTitle();
			scripts[i].i = i;
			scripts[i].start();
		}
		for(PopulateDocURLTitle sc : scripts) {
			sc.join();
		}

		System.out.println("====done====");
	}

}
