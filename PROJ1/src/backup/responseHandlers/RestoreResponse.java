package backup.responseHandlers;

import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.nio.file.Files;

import backup.MetaDataChunk;
import backup.Peer;
import backup.Utils;

public class RestoreResponse implements Runnable {

	private Peer peer;
	private byte[] msgRcvd;
	private String[] msgRcvdString;
	private MulticastSocket mdr;
	private MetaDataChunk chunk;

	public RestoreResponse(Peer peer, byte[] msgRcvd) throws IOException {

		this.peer = peer;
		this.msgRcvd = msgRcvd;
		this.msgRcvdString = new String(this.msgRcvd, 0, this.msgRcvd.length).split("\\s+");
		this.mdr = new MulticastSocket(peer.getMdrPort());
		this.chunk = new MetaDataChunk(this.msgRcvdString[3], Integer.parseInt(this.msgRcvdString[4]), -1);
	}

	@Override
	public void run() {
		String serverID = this.msgRcvdString[2];

		if (serverID.equals(Integer.toString(this.peer.getServerID())) ||
				!Peer.backUpAChunkPreviously(Integer.toString(this.peer.getServerID()), chunk)) {
			return;
		}
		
		System.out.println("Init of RestoreResponse");
		
		try {
			Utils.myRandomSleep(Utils.MAX_SLEEP_MS);
			if(Peer.noChunkHasArrived(chunk)) {
				InetAddress addr = InetAddress.getByName(peer.getMdrIP());
				byte[] msgToSend = this.createMsg();
				this.mdr.send(new DatagramPacket(msgToSend, msgToSend.length, addr, peer.getMdrPort()));
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		Peer.recordsDatabaseToFile();
		
		System.out.println("End of RestoreResponse");

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

		byte[] header = msg.getBytes();
		byte[] body = this.readChunk();


		byte[] byte_msg = new byte[header.length + body.length];
		System.arraycopy(header, 0, byte_msg, 0, header.length);
		System.arraycopy(body, 0, byte_msg, header.length, body.length);

		return byte_msg;
	}

	private byte[] readChunk() {

		try {
			String nameOfChunk = this.chunk.toString();
			File chunkFile = new File(Peer.chunksDir, nameOfChunk);
			byte[] buffer = Files.readAllBytes(chunkFile.toPath());
			return buffer;
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return null;
	}

}
