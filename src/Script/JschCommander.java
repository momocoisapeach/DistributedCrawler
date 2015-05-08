package Script;
import java.io.IOException;
import java.io.InputStream;

import com.jcraft.jsch.*;

// TODO: Auto-generated Javadoc
/**
 * The Class JschCommander.
 */
public class JschCommander {

	
		
	/** The pem. */
	public static String pem = Script.key_pem;
	
	/** The host. */
	String host;
	
	/** The ip. */
	String ip;
	
	/** The channel. */
	ChannelExec channel;
	
	/** The session. */
	Session session;
	
	/** The jsch. */
	JSch jsch;
	
	/** The id. */
	int id;

	/**
	 * Instantiates a new jsch commander.
	 *
	 * @param host the host
	 * @param ip the ip
	 * @param id the id
	 * @throws JSchException the j sch exception
	 */
	public JschCommander(String host, String ip, int id) throws JSchException {
		this.host = host;
		this.ip = ip;
		this.id = id;
		connect();
	}
	
	/**
	 * Connect.
	 *
	 * @throws JSchException the j sch exception
	 */
	public void connect() throws JSchException {
		jsch=new JSch();
		jsch.addIdentity(pem);
		jsch.setConfig("StrictHostKeyChecking", "no");

		//enter your own EC2 instance IP here
		session=jsch.getSession(host, ip, 22);
		session.connect();
	}

	/**
	 * Execute.
	 *
	 * @param command the command
	 * @throws JSchException the j sch exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws InterruptedException the interrupted exception
	 */
	public void execute(String command)  throws JSchException, IOException, InterruptedException {
		//run stuff
		channel = (ChannelExec) session.openChannel("exec");
		channel.setCommand(command);
//		channel.setErrStream(System.err);
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
				System.out.println("crawler#" + id + " exit-status: " + channel.getExitStatus());
				break;
			}
			Thread.sleep(1000);
		}
		
	}
	
	/**
	 * Disconnect.
	 */
	public void disconnect() {
		channel.disconnect();
		session.disconnect();
	}

	/**
	 * The main method.
	 *
	 * @param args the arguments
	 * @throws JSchException the j sch exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws InterruptedException the interrupted exception
	 */
	public static void main(String[] args) throws JSchException, IOException, InterruptedException {
		JschCommander commander = new JschCommander("ec2-user", "54.213.17.150", 0);
		commander.execute("yes | ./script");
	}

}
