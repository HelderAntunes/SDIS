package backup.initiators;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

import backup.MetaDataChunk;
import backup.Peer;
import backup.Utils;

public class RestoreInit implements Runnable {
	
	
	private Peer peer;
	private MetaDataChunk chunk;
	private MulticastSocket mc;

	public RestoreInit(Peer peer, String originalFileName, int chunkNo) {

		this.peer = peer;
		this.chunk = new MetaDataChunk(Utils.getFileId(originalFileName), chunkNo, -1);

		try {
			this.mc = new MulticastSocket(peer.getMcPort());			
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
	

	@Override
	public void run() {
				
		try {
			
			System.out.println("Init of RestoreInit thread");
			
			InetAddress addr = InetAddress.getByName(this.peer.getMcIP());
			
			byte[] msg = this.createMsg();
			mc.send(new DatagramPacket(msg, msg.length, addr, peer.getMcPort()));       

			this.peer.recordsDatabaseToFile();
			System.out.println("End of RestoreInit thread");
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	/**
	 * Message format:
	 * "GETCHUNK <Version> <SenderId> <FileId> <ChunkNo> <CRLF><CRLF>"
	 * @return message
	 */
	private byte[] createMsg() {

		String fileID = this.chunk.fileId;
		String senderID = Integer.toString(this.peer.getServerID());
		String version = this.peer.getProtocolVersion();
		String chunkNo = Integer.toString(this.chunk.chunkNo);
		String msg = "GETCHUNK " + version + " " + senderID + " " + fileID + " " + 
				chunkNo + " \r\n\r\n";

		return msg.getBytes();
	}

	
}
