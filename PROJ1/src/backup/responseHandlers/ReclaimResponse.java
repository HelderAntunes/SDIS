package backup.responseHandlers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;

import backup.MetaDataChunk;
import backup.Peer;

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

		String fileID = this.msgRcvdString[3];
		int chunkNO = Integer.parseInt(this.msgRcvdString[4]);
		MetaDataChunk chunkRemoved = new MetaDataChunk(fileID, chunkNO, -1);

		String chunkID = chunkRemoved.toString();
		if (Peer.backupDB.containsKey(chunkID)) {

			ArrayList<String> peers = Peer.backupDB.get(chunkID);
			for(int i = 0; i < peers.size(); i++) {
				if (peers.get(i).equals(serverID)) {
					peers.remove(i);
					break;
				}
			}

			int  n = new Random().nextInt(400) + 1;
			try {
				Thread.sleep(n);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			if (this.msgPutChunkWasReceveid(fileID, chunkNO))
				return;
			
			//new Thread(new BackupInit(this.peer, file, i, repDeg, fileSplitted.get(i))).start();
		}

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
