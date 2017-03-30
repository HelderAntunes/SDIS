package backup.initiators;

import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;
import java.util.Set;

import backup.MetaDataChunk;
import backup.MetaDataFile;
import backup.Peer;
import backup.Utils;

public class DeleteInit implements Runnable {

	private Peer peer;
	private MulticastSocket mc;
	private String fileID;


	public DeleteInit(Peer peer, File file) {

		this.peer = peer;
		this.fileID = Utils.getFileId(file.getName() + Integer.toString((int)file.lastModified()));

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

			while (attempts < Utils.ATTEMPTS_ON_DELETE) {
				this.mc.send(new DatagramPacket(msg, msg.length, addr, this.peer.getMcPort()));
				Thread.sleep(timeOut);
				attempts++;
			}
			
			for (int i = 0; i < Peer.myFiles.size(); i++) {
				MetaDataFile myFile = Peer.myFiles.get(i);
				if (myFile.id.equals(fileID)) {
					Peer.myFiles.remove(i);
					i--;
				}
			}
			
			Set<MetaDataChunk> keys = Peer.backupDB.keySet();
			for(MetaDataChunk key : keys) {
				if (key.fileId.equals(fileID)) {
					Peer.backupDB.remove(key);
					for (int i = 0; i < Peer.myChunks.size(); i++) {
						MetaDataChunk chunk = Peer.myChunks.get(i);
						if (chunk.fileId.equals(fileID)) {
							Peer.myChunks.remove(i);
							i--;
						}
					}
				}
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
