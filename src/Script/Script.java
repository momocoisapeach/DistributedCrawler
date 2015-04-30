/**
 * 
 */
package Script;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

import Utils.IOUtils;

/**
 * @author dichenli
 *
 */
public class Script extends Thread {

	static String[] addresses = {
		"ec2-user@52.24.15.232", //0
		"ec2-user@54.213.17.150",
		"ec2-user@52.10.6.206",
		"ec2-user@52.24.15.231",
		"ec2-user@52.24.15.200",
		"ec2-user@52.24.15.228",//5
		"ec2-user@52.24.15.230",
		"ec2-user@52.24.15.229",
		"ec2-user@52.24.15.227",
		"ec2-user@52.24.15.194",
		"ec2-user@52.24.8.13",//10
		"ec2-user@52.24.17.4",
		"ec2-user@52.24.13.124",
		"ec2-user@52.24.18.1",
		"ec2-user@52.24.7.96",
		"ec2-user@52.10.53.0",//15
		"ec2-user@52.24.1.234",
		"ec2-user@52.24.5.121"//17
	};

	static String[] seeds = {
		"http://www.dmoz.org/", //0
		"http://www.aol.com/",
		"http://www.incrawler.com/",
		"http://news.google.com/",
		"http://www.gimpsy.com/",
		"http://www.msn.com/", //5
		"https://twitter.com/?lang=en /home/ec2-user/crawl_data",
		"https://en.wikipedia.org/wiki/Main_Page",
		"https://www.techmeme.com/",
		"https://business.yahoo.com/",
		"http://news.google.com/", //10
		"http://www.gimpsy.com/",
		"http://www.msn.com/",
		"https://twitter.com/?lang=en /home/ec2-user/crawl_data",
		"https://en.wikipedia.org/wiki/Main_Page",
		"https://www.techmeme.com/", //15
		"https://business.yahoo.com/",
		"http://www.aol.com/" //17
	};

	static String local_home = "/Users/dichenli/";
	static String ec2_home = local_home + ".ec2/";
	static String aws_home = local_home + ".aws/";
	static String local_download = local_home + "Downloads/";
	static String upload_data = "crawl_data";
	static String upload_zip = "crawl_data.zip";
	static String key_pem = ec2_home + "ec2try.pem";

	static String remote_home = "/home/ec2-user/";
	static String crawl_data = remote_home + "crawl_data/";
	static long crawl_limit = 10000;
	static int size = 1; //Mb data
	static int crawler_num = 18;

	int i; //crawler number
	
	@Override
	public void run() {
		System.out.println("Thread start: " + i);
		boolean success = true;
		PrintWriter writer = null;
		String filenameEc2Script = ec2_home + "ec2_script" + i;
		File ec2Script = new File(filenameEc2Script);
		success &= IOUtils.createFile(ec2Script);
		if(!success) {
			System.err.println("create file ec2 failed");
			return;
		}
		try {
			writer = IOUtils.getWriter(ec2Script);
			writer.println("unzip " + upload_zip);
			writer.println("mkdir ~/.aws");
			writer.println("mv " + remote_home + "credentials ~/.aws/credentials"); 
			writer.close();
			IOUtils.setFilePermission(ec2Script, 7, 0, 0);
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		
		String filenameEC2_run = ec2_home + "run_crawl" + i;
		File ec2_run = new File(filenameEC2_run);
		success &= IOUtils.createFile(ec2_run);
		if(!success) {
			System.err.println("create file ec2 failed");
			return;
		}
		try {
			writer = IOUtils.getWriter(ec2_run);
			writer.println("java -jar " + crawl_data + "Crawler.jar " + seeds[i] + " " + crawl_data + " " + size + " " + crawl_limit + " " + i + " " + crawler_num);
			writer.close();
			IOUtils.setFilePermission(ec2_run, 7, 0, 0);
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		
		String filenameUpload = ec2_home + "upload_crawler" + i;
		File uploadScript = new File(filenameUpload);

		success &= IOUtils.createFile(uploadScript);
		if(!success) {
			System.err.println("create file crawler failed");
			return;
		}

		try {
			writer = IOUtils.getWriter(uploadScript);
			writer.println("scp -i " + key_pem + " " + local_download + upload_zip + " " + addresses[i] + ":~/");
			writer.println("scp -i " + key_pem + " " + aws_home + "credentials " + addresses[i] + ":~/credentials");
			writer.println("scp -i " + key_pem + " " + filenameEc2Script + " " + addresses[i] + ":~/");
			writer.println("scp -i " + key_pem + " " + filenameEC2_run + " " + addresses[i] + ":~/");
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
		
		String filenameSSH = ec2_home + "crawler" + i;
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
	}
	
	/**
	 * @param args
	 * @throws IOException 
	 * @throws InterruptedException 
	 */
	public static void main(String[] args) throws IOException, InterruptedException {
		if(addresses.length != crawler_num || seeds.length != crawler_num) {
			System.err.println("wrong seeds or addresses!");
			return;
		}

		boolean success = true;
		
		String filenameZip = ec2_home + "zip_command";
		File zip = new File(filenameZip);
		IOUtils.createFile(zip);
		PrintWriter writer = IOUtils.getWriter(zip);
		writer.println("cd ~/Downloads/ && zip -r " + upload_zip + " " + upload_data + "/*");
		writer.close();
		IOUtils.setFilePermission(zip, 7, 0, 0);
		IOUtils.runtimeExec(zip.getAbsolutePath());

//		PrintWriter writer = null;
		Script[] scripts = new Script[crawler_num];
		for (int i = 0; i < crawler_num; i++) {
			scripts[i] = new Script();
			scripts[i].i = i;
			scripts[i].start();
		}
		for(Script sc : scripts) {
			sc.join();
		}
		
		System.out.println("====done====");
	}

}
