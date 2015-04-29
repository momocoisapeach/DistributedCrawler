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
public class Script {

	static String[] addresses = {
		"ec2-user@54.213.17.150",
		"ec2-user@52.10.6.206",
		"ec2-user@54.149.220.141",
		"ec2-user@54.213.235.114",
		"ec2-user@54.149.228.245",
		"ec2-user@54.191.133.199",
		"ec2-user@52.10.33.146",
		"ec2-user@54.69.137.17",
		"ec2-user@54.187.253.21",
		"ec2-user@54.148.82.163"
	};

	static String[] seeds = {
		"http://www.dmoz.org/",
		"http://www.aol.com/",
		"http://www.incrawler.com/",
		"http://news.google.com/",
		"http://www.gimpsy.com/",
		"http://www.msn.com/",
		"https://twitter.com/?lang=en /home/ec2-user/crawl_data",
		"https://en.wikipedia.org/wiki/Main_Page",
		"https://www.techmeme.com/",
		"https://business.yahoo.com/"
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
	static double size = 1; //Mb data
	static int crawler_num = 2;

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
//		if(addresses.length != crawler_num || seeds.length != crawler_num) {
//			System.err.println("wrong seeds or addresses!");
//			return;
//		}

		boolean success = true;
		
		String filenameEc2Script = local_download + upload_data + "/ec2_script";
		File ec2Script = new File(filenameEc2Script);
		success &= IOUtils.createFile(ec2Script);
		if(!success) {
			System.err.println("create file ec2 failed");
			return;
		}

		try {
			PrintWriter writer = IOUtils.getWriter(ec2Script);
			writer.println("unzip *.zip");
			writer.println("mkdir ~/.aws");
			writer.println("mv " + crawl_data + "credentials " + "~/.aws/credentials");
			writer.println("");
			writer.close();
			IOUtils.setFilePermission(ec2Script, 7, 0, 0);
		} catch (IOException e) {
			e.printStackTrace();
			throw e;
		}
		
		String filenameZip = ec2_home + "zip_command";
		File zip = new File(filenameZip);
		IOUtils.createFile(zip);
		PrintWriter writer = IOUtils.getWriter(zip);
		writer.println("cd ~/Downloads/ && zip -r " + upload_zip + " " + upload_data + "/*");
		writer.close();
		IOUtils.setFilePermission(zip, 7, 0, 0);
		IOUtils.runtimeExec(zip.getAbsolutePath());

//		PrintWriter writer = null;
		for (int i = 0; i < crawler_num; i++) {
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
				writer.println("scp -i " + key_pem + " " + aws_home + "credentials " + addresses[i] + ":~/");
				writer.close();
				IOUtils.setFilePermission(uploadScript, 7, 0, 0);
				IOUtils.runtimeExec(uploadScript.getAbsolutePath());
			} catch (IOException e) {
				e.printStackTrace();
				throw e;
			}
			
			String filenameSSH = ec2_home + "crawler" + i;
			File ssh = new File(filenameSSH);
			try {
				IOUtils.createFile(ssh);
				writer = IOUtils.getWriter(ssh);
				writer.println("ssh -i " + key_pem + " " + addresses[i]);
				writer.close();
				IOUtils.setFilePermission(ssh, 7, 0, 0); 
			} catch (IOException e) {
				e.printStackTrace();
				throw e;
			}
		}
	}

}
