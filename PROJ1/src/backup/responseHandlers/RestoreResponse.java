package backup.responseHandlers;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.Random;

import backup.MetaDataChunk;
import backup.Peer;

public class RestoreResponse implements Runnable {
	
	private Peer peer;
	private byte[] msgRcvd;
	private String[] msgRcvdString;
	private Boolean sendChunk;
	private MulticastSocket mdr;
	private MetaDataChunk chunk;

	public RestoreResponse(Peer peer, byte[] msgRcvd) throws IOException {
		this.peer = peer;
		this.msgRcvd = msgRcvd;
        this.msgRcvdString = new String(this.msgRcvd, 0, this.msgRcvd.length).split("\\s+");
        this.sendChunk = true;
        this.mdr = new MulticastSocket(peer.getMdrPort());
        this.chunk = new MetaDataChunk(this.msgRcvdString[3], Integer.parseInt(this.msgRcvdString[4]), -1);
	}

	@Override
	public void run() {
		// GETCHUNK <Version> <SenderId> <FileId> <ChunkNo> <CRLF><CRLF>
		String serverID = this.msgRcvdString[2];
		
		if (serverID.equals(Integer.toString(this.peer.getServerID()))) {
			return;
		}
		
		if (!Peer.backUpAChunkPreviously(Integer.toString(this.peer.getServerID()), chunk)) {
			return;
		}
		
		int  n = new Random().nextInt(400) + 1;
		
		try {
			Thread.sleep(n);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		if (this.sendChunk == true) {
			InetAddress addr;
			try {
				addr = InetAddress.getByName(peer.getMdrIP());
				byte[] msg = this.createMsg();
			    this.mdr.send(new DatagramPacket(msg, msg.length, addr, peer.getMdrPort()));
			} catch (IOException e) {
				e.printStackTrace();
			}
		
		}
			
	}
	
	public void update(String msg) {
		
	}
	
	/**
	 * Message format:
	 * "CHUNK <Version> <SenderId> <FileId> <ChunkNo> <CRLF><CRLF><Body>"
	 * @return message
	 */
	private byte[] createMsg() {

		String fileID = this.chunk.fileId;
		String senderID = Integer.toString(this.peer.getServerID());
		String version = this.peer.getProtocolVersion();
		String chunkNo = Integer.toString(this.chunk.chunkNo);
		String msg = "CHUNK " + version + " " + senderID + " " + fileID + " " + 
				chunkNo + " \r\n\r\n";
		
		

		return msg.getBytes();
	}

	
}
