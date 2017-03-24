package backup.initiators;

import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

import backup.Peer;
import backup.Utils;

public class ReclaimInit implements Runnable {
	
	
	private Peer peer;
	private String fileID;
	private int chunkNo;
	private MulticastSocket mc;

	public ReclaimInit(Peer peer, File file, int chunkNo) {

		this.peer = peer;
		this.fileID = Utils.getFileId(file.getName() + Integer.toString((int)file.lastModified()));
		this.chunkNo = chunkNo;

		try {
			this.mc = new MulticastSocket(peer.getMcPort());
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	@Override
	public void run() {
		
		try {
			System.out.println("Init of ReclaimInit");
			
			byte[] msg = this.createMsg();
			InetAddress addr = InetAddress.getByName(this.peer.getMcIP());
			this.mc.send(new DatagramPacket(msg, msg.length, addr, this.peer.getMcPort()));
			
			System.out.println("End of ReclaimInit");

		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	/**
	 * Message format:
	 * REMOVED <Version> <SenderId> <FileId> <ChunkNo> <CRLF><CRLF>
	 * @return message
	 */
	private byte[] createMsg() {

		String senderID = Integer.toString(this.peer.getServerID());
		String version = this.peer.getProtocolVersion();
		String msg = "REMOVED " + version + " " + senderID + " " + fileID + Integer.toString(chunkNo) + " \r\n\r\n";

		return msg.getBytes();
	}

}
