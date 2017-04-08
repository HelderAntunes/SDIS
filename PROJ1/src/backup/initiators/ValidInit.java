package backup.initiators;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;

import backup.Peer;

public class ValidInit implements Runnable {

	private Peer peer;
	private MulticastSocket mc;
	private String fileID;

	public ValidInit (Peer peer, String fileID) {

		this.peer = peer;
		this.fileID =fileID;
		try {
			this.mc = new MulticastSocket(peer.getMcPort());
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	@Override
	public void run() {

		try {

			System.out.println("Init of ValidInit");

			byte[] msg = this.createMsg();
			InetAddress addr = InetAddress.getByName(this.peer.getMcIP());
			System.out.println(new String(msg));
			this.mc.send(new DatagramPacket(msg, msg.length, addr, this.peer.getMcPort()));

			System.out.println("End of ValidInit");

		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} 

	}

	/**
	 * Message format:
	 * VALID <Version> <SenderId> <FileId> <CRLF><CRLF>
	 * @return message
	 */
	private byte[] createMsg() {

		String senderID = Integer.toString(this.peer.getServerID());
		String version = this.peer.getProtocolVersion();
		String msg = "VALID " + version + " " + senderID + " " + this.fileID + " \r\n\r\n";

		return msg.getBytes();
	}

}
