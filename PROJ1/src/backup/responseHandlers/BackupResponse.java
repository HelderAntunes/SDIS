package backup.responseHandlers;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;
import java.util.Random;

import backup.MetaDataChunk;
import backup.Peer;
import backup.Utils;

public class BackupResponse implements Runnable {

	private MulticastSocket mc;
	private Peer peer;
	private byte[] msgRcvd;
	private String[] msgRcvdString;

	public BackupResponse(Peer peer, byte[] msgRcvd) throws IOException {
		this.peer = peer;
		this.mc = new MulticastSocket(peer.getMcPort());
		this.msgRcvd = msgRcvd;
		this.msgRcvdString = new String(this.msgRcvd, 0, this.msgRcvd.length).split("\\s+");
	}

	@Override
	public void run() {

		if (Utils.getBodyOfMsg(this.msgRcvd).length + Peer.spaceUsed_bytes 
				> Peer.maxSpaceDisk_bytes) {
			return;
		}

		MetaDataChunk chunk = new MetaDataChunk(msgRcvdString[3], Integer.parseInt(msgRcvdString[4]), Integer.parseInt(msgRcvdString[5]));

		if (this.peer.getServerID() == Integer.parseInt(msgRcvdString[2]) || Peer.isMyChunk(chunk)) {
			return;
		}

		System.out.println("Init of BackupResponse");

		if (this.peer.getProtocolVersion().equals("1.0")) {
			Utils.myRandomSleep(Utils.MAX_SLEEP_MS);

			if (Peer.backUpAChunkPreviously(Integer.toString(this.peer.getServerID()), chunk)) {
				this.sendConfirmation();
			}
			else if (true){
				Peer.recordsBackupIfNeeded(chunk, Integer.toString(this.peer.getServerID()));
				Peer.chunksSaved.add(chunk);
				this.saveChunkInFileSystem(chunk);
				this.sendConfirmation();
			}

			Peer.remPutChunkMsgOfAChunk(chunk);

			Peer.recordsDatabaseToFile();
		}
		else {
			
			if (Peer.backUpAChunkPreviously(Integer.toString(this.peer.getServerID()), chunk)) {
				this.sendConfirmation();
			}
			else {
				Utils.myRandomSleep(Utils.MAX_SLEEP_MS);
				if (Peer.getReplicationOfChunk(chunk) < chunk.desiredRepDeg){
					this.sendConfirmation();
					Peer.chunksSaved.add(chunk);
					this.saveChunkInFileSystem(chunk);
				}
			}

			Peer.recordsDatabaseToFile();		
		}

		System.out.println("End of BackupResponse");

	}

	private void saveChunkInFileSystem(MetaDataChunk chunk) {

		try {

			File file = new File(Peer.chunksDir, chunk.toString());
			FileOutputStream outputStream = new FileOutputStream(file);
			outputStream.write(Utils.getBodyOfMsg(this.msgRcvd));
			outputStream.close();

			Peer.spaceUsed_bytes += (int)file.length();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Send confirmation of storing of chunk to control channel.
	 * Message format:
	 * STORED command: "STORED <Version> <SenderId> <FileId> <ChunkNo> <CRLF><CRLF>"
	 */
	private void sendConfirmation() {

		try {
			String confirmation = "STORED " + this.peer.getProtocolVersion() 
			+ " " + Integer.toString(this.peer.getServerID()) +
			" " + this.msgRcvdString[3] + " " + this.msgRcvdString[4] + " \r\n\r\n";
			byte[] byte_msg = confirmation.getBytes();

			int  n = new Random().nextInt(400) + 1;
			Thread.sleep(n);

			InetAddress addr = InetAddress.getByName(peer.getMcIP());
			this.mc.send(new DatagramPacket(byte_msg, byte_msg.length, addr, peer.getMcPort()));

		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

}
