package backup.responseHandlers;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;

import backup.MetaDataChunk;
import backup.Peer;
import backup.initiators.BackupInit;

public class ReclaimResponse implements Runnable {

	private Peer peer;
	private byte[] msgRcvd;
	private String[] msgRcvdString;


	public ReclaimResponse(Peer peer, byte[] msgRcvd) throws IOException {
		this.peer = peer;
		this.msgRcvd = msgRcvd;
		this.msgRcvdString = new String(this.msgRcvd, 0, this.msgRcvd.length).split("\\s+");
	}


	@Override
	public void run() {
		
		String serverID = this.msgRcvdString[2];

		if (serverID.equals(Integer.toString(peer.getServerID()))) {
			return;
		}
		
		System.out.println("Init of ReclaimResponse");
		
		String fileID = this.msgRcvdString[3];
		int chunkNO = Integer.parseInt(this.msgRcvdString[4]);
		MetaDataChunk chunkRemoved = new MetaDataChunk(fileID, chunkNO, -1);
		if (Peer.backupDB.containsKey(chunkRemoved)) {
			ArrayList<String> peers = Peer.backupDB.get(chunkRemoved);
			for(int j = 0; j < peers.size(); j++) {
				if (peers.get(j).equals(serverID)) {
					peers.remove(j);
					break;
				}
			}
		}

		for (int i = 0; i < Peer.chunksSaved.size(); i++) {
			
			MetaDataChunk chunk = Peer.chunksSaved.get(i);
			if(chunk.fileId.equals(fileID) && chunk.chunkNo == chunkNO) {

				int  n = new Random().nextInt(400) + 1;
				try {
					Thread.sleep(n);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

				if (this.msgPutChunkWasReceveid(fileID, chunkNO))
					return;
				
				if (Peer.getReplicationOfChunk(chunk) >= chunk.desiredRepDeg)
					return;
				File file = new File(Peer.chunksDir, chunk.toString());
				byte[] fileContent;
				try {
					fileContent = Files.readAllBytes(file.toPath());
					new Thread(new BackupInit(this.peer, fileID, chunkNO, chunk.desiredRepDeg, fileContent)).start();
				} catch (IOException e) {
					e.printStackTrace();
				}
				
				break;
		
			}
		}
		
		System.out.println("End of ReclaimResponse");
		
		Peer.recordsDatabaseToFile();

	}

	private boolean msgPutChunkWasReceveid(String fileID, int chunkNO) {

		CopyOnWriteArrayList<String> putChunkMsgsRcvd = Peer.putChunkMsgsReceived;

		for (int i = 0; i < putChunkMsgsRcvd.size(); i++) {

			String[] msgPutChunk = putChunkMsgsRcvd.get(i).split("\\s+");
			String fileID_msg = msgPutChunk[3];
			int chunkNO_msg = Integer.parseInt(msgPutChunk[4]);

			if (fileID_msg.equals(fileID) && chunkNO_msg == chunkNO) {
				putChunkMsgsRcvd.remove(i);
				return true;
			}
		}

		return false;
	}

}
