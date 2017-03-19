package backup.initiators;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;

import backup.Peer;
import backup.Utils;

public class DeleteInit implements Runnable {

	private Peer peer;
	private MulticastSocket mc;
	private String fileID;


	public DeleteInit(Peer peer, String originalFileName) {

		this.peer = peer;
		this.fileID = Utils.getFileId(originalFileName);

		try {
			this.mc = new MulticastSocket(peer.getMcPort());
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	@Override
	public void run() {

		try {
			
			System.out.println("Init of DeleteInit");
			
			byte[] msg = this.createMsg();
			InetAddress addr = InetAddress.getByName(this.peer.getMcIP());
			int attempts = 0;
			int timeOut = 1000;

			while (attempts < 1) {
				this.mc.send(new DatagramPacket(msg, msg.length, addr, this.peer.getMcPort()));
				Thread.sleep(timeOut);
				attempts++;
			}

			System.out.println("End of DeleteInit");
			
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

	}

	/**
	 * Message format:
	 * DELETE <Version> <SenderId> <FileId> <CRLF><CRLF>
	 * @return message
	 */
	private byte[] createMsg() {

		String senderID = Integer.toString(this.peer.getServerID());
		String version = this.peer.getProtocolVersion();
		String msg = "DELETE " + version + " " + senderID + " " + fileID + " \r\n\r\n";

		return msg.getBytes();
	}

}
