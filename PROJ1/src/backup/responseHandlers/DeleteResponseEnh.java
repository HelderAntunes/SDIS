package backup.responseHandlers;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

import backup.Peer;

public class DeleteResponseEnh implements Runnable {
	
	private Peer peer;
	private byte[] msgRcvd;
	private MulticastSocket mc;
	private String[] msgRcvdString;
	
	public DeleteResponseEnh(Peer peer, byte[] msgRcvd) throws IOException {
        this.peer = peer;
        this.msgRcvd = msgRcvd;
        this.msgRcvdString = new String(this.msgRcvd, 0, this.msgRcvd.length).split("\\s+");
        try {
			this.mc = new MulticastSocket(peer.getMcPort());
		} catch (IOException e) {
			e.printStackTrace();
		}
    }

	@Override
	public void run() {
		
		String serverID = this.msgRcvdString[2];
		String fileID = this.msgRcvdString[3];
		
		if (serverID.equals(Integer.toString(this.peer.getServerID()))) {
			return;
		}
		
		if (!this.peer.checkIfSaveAChunkOfAFile(fileID)) {
			return;
		}
		
		System.out.println("Init of delete response ENH!");
		
		// send DELETED message
		try {
			byte[] msg = this.createMsg(fileID);
			InetAddress addr = InetAddress.getByName(this.peer.getMcIP());
			this.mc.send(new DatagramPacket(msg, msg.length, addr, this.peer.getMcPort()));
		} catch (IOException e) {
			e.printStackTrace();
		} 
		
		Peer.deleteDataAssociatedWithAFileSaved(fileID);
		
		Peer.recordsDatabaseToFile();
		
		System.out.println("End of delete response ENH!");
	
	}
	
	/**
	 * Message format:
	 * DELETED <Version> <SenderId> <FileId> <CRLF><CRLF>
	 * @return message
	 */
	private byte[] createMsg(String fileID) {

		String senderID = Integer.toString(this.peer.getServerID());
		String version = this.peer.getProtocolVersion();
		String msg = "DELETED " + version + " " + senderID + " " + fileID + " \r\n\r\n";

		return msg.getBytes();
	}

}
