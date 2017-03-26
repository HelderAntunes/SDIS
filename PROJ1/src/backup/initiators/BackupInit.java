package backup.initiators;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.ArrayList;

import backup.MetaDataChunk;
import backup.Peer;

public class BackupInit implements Runnable {

	private Peer peer;
	private MulticastSocket mdb;
	private byte[] body;
	private byte[] msg;
	private MetaDataChunk chunk;

	public BackupInit(Peer peer, String fileID, int chunkNo, int repDeg, byte[] body) {

		this.peer = peer;
		this.body = body;
		this.chunk = new MetaDataChunk(fileID, chunkNo, repDeg);
		
		if (!Peer.backupDB.containsKey(chunk)) {
			Peer.myChunks.add(this.chunk);
			Peer.backupDB.put(chunk, new ArrayList<String>());
			Peer.recordsDatabaseToFile();
		}
	
		this.msg = this.createMsg();

		try {
			this.mdb = new MulticastSocket(peer.getMdbPort());
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	@Override
	public void run() {

		try {

			System.out.println("Init of BackInit thread");

			InetAddress addr = InetAddress.getByName(this.peer.getMdbIP());
			int attempts = 0;
			int currRep = 0;
			int timeOut = 1000;
			
			while (attempts < 5 && currRep < this.chunk.desiredRepDeg) {
				System.out.println("send backup msg--------->" + (attempts+1));
				mdb.send(new DatagramPacket(msg, msg.length, addr, peer.getMdbPort()));             
				Thread.sleep(timeOut);
				attempts++;
				timeOut *= 2;
				currRep = Peer.getReplicationOfChunk(this.chunk);
			}

			System.out.println("End of BackInit thread");
			
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

	}
	
	/**
	 * Message format:
	 * "PUTCHUNK <Version> <SenderId> <FileId> <ChunkNo> <ReplicationDeg> <CRLF><CRLF><Body>"
	 * @return message
	 */
	private byte[] createMsg() {

		String fileID = this.chunk.fileId;
		String senderID = Integer.toString(this.peer.getServerID());
		String version = this.peer.getProtocolVersion();
		String replication = Integer.toString(this.chunk.desiredRepDeg);
		String chunkNo = Integer.toString(this.chunk.chunkNo);
		String msg = "PUTCHUNK " + version + " " + senderID + " " + fileID + " " + 
				chunkNo + " " + replication + " \r\n\r\n";

		byte[] header = msg.getBytes();
		byte[] byte_msg = new byte[header.length + body.length];
		System.arraycopy(header, 0, byte_msg, 0, header.length);
		System.arraycopy(body, 0, byte_msg, header.length, body.length);

		return byte_msg;
	}

}
