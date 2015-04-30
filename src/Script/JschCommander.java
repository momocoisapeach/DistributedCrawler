package Script;
import java.io.IOException;
import java.io.InputStream;

import com.jcraft.jsch.*;

public class JschCommander {


	public static String pem = Script.key_pem;
	String host;
	String ip;
	ChannelExec channel;
	Session session;
	JSch jsch;

	public void connect() throws JSchException, IOException, InterruptedException {
		jsch=new JSch();
		jsch.addIdentity(pem);
		jsch.setConfig("StrictHostKeyChecking", "no");

		//enter your own EC2 instance IP here
		session=jsch.getSession(host, ip, 22);
		session.connect();
	}

	public void execute(String command)  throws JSchException, IOException, InterruptedException {
		//run stuff
		channel = (ChannelExec) session.openChannel("exec");
		channel.setCommand(command);
		channel.setErrStream(System.err);
		channel.connect();
		
		InputStream input = channel.getInputStream();
		//start reading the input from the executed commands on the shell
		byte[] tmp = new byte[1024];
		while (true) {
			while (input.available() > 0) {
				int i = input.read(tmp, 0, 1024);
				if (i < 0) break;
				System.out.print(new String(tmp, 0, i));
			}
			if (channel.isClosed()){
				System.out.println("exit-status: " + channel.getExitStatus());
				break;
			}
			Thread.sleep(1000);
		}

		channel.disconnect();
		session.disconnect();
	}

	public static void main(String[] args) throws JSchException, IOException, InterruptedException {

	}

}
