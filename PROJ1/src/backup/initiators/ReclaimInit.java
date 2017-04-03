package backup.initiators;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;

import backup.MetaDataChunk;
import backup.Peer;

public class ReclaimInit implements Runnable {
	
	
	private Peer peer;
	private String fileID;
	private int chunkNo;
	private MulticastSocket mc;

	public ReclaimInit(Peer peer, String fileID, int chunkNo) {

		this.peer = peer;
		this.fileID = fileID;
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
			
			MetaDataChunk chunk = new MetaDataChunk(this.fileID, this.chunkNo, -1);
			ArrayList<String> peers = Peer.backupDB.get(chunk);		
			for (int i = 0; i < peers.size(); i++) {
				if (peers.get(i).equals(Integer.toString(this.peer.getServerID()))) {
					peers.remove(i);
					break;
				}
			}
			
			CopyOnWriteArrayList<MetaDataChunk> chunksSaved = Peer.chunksSaved;
			for (int i = 0; i < chunksSaved.size(); i++) {
				if (chunksSaved.get(i).equals(chunk)) {
					chunksSaved.remove(i);
					break;
				}
			}
			
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
		String msg = "REMOVED " + version + " " + senderID + " " + fileID + " " + Integer.toString(chunkNo) + " \r\n\r\n";

		return msg.getBytes();
	}

}
