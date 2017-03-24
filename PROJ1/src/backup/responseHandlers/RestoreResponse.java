package backup.responseHandlers;

import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.nio.file.Files;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;

import backup.MetaDataChunk;
import backup.Peer;

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
		
	
		try {
			int  n = new Random().nextInt(400) + 1;
			
			Thread.sleep(n);
			if(this.noChunkHasArrived()) {
				InetAddress addr = InetAddress.getByName(peer.getMdrIP());
				byte[] msgToSend = this.createMsg();
				this.mdr.send(new DatagramPacket(msgToSend, msgToSend.length, addr, peer.getMdrPort()));
				System.out.println("sent in restore response");
			}
		} catch (InterruptedException | IOException e) {
			e.printStackTrace();
		}
		
	}

	private boolean noChunkHasArrived() {

		CopyOnWriteArrayList<String> chunkMsgReceived = Peer.chunkReceived;
		for (int i = 0; i < chunkMsgReceived.size(); i++) {

			String msg = chunkMsgReceived.get(i);
			String[] msgSplitted = msg.split("\\s+");
			String fileID = msgSplitted[3];
			int chunkNO = Integer.parseInt(msgSplitted[4]);

			if (fileID.equals(this.chunk.fileId) && chunkNO == this.chunk.chunkNo) {
				chunkMsgReceived.remove(i);
				return false;
			}

		}

		return true;
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
