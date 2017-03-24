package backup.responseHandlers;

import java.io.File;
import java.io.IOException;
import java.util.Set;

import backup.MetaDataChunk;
import backup.Peer;

public class DeleteResponse implements Runnable {
	
	private Peer peer;
	private byte[] msgRcvd;
	private String[] msgRcvdString;
	
	public DeleteResponse(Peer peer, byte[] msgRcvd) throws IOException {
        this.peer = peer;
        this.msgRcvd = msgRcvd;
        this.msgRcvdString = new String(this.msgRcvd, 0, this.msgRcvd.length).split("\\s+");
    }

	@Override
	public void run() {
		
		String serverID = this.msgRcvdString[2];
		String fileID = this.msgRcvdString[3];
		
		if (serverID.equals(Integer.toString(this.peer.getServerID()))) {
			return;
		}
		
		System.out.println("Init of delete response!");
		
		Set<MetaDataChunk> keys = Peer.backupDB.keySet();
		for(MetaDataChunk key : keys) {
			
			if (key.fileId.equals(fileID)) {
				Peer.backupDB.remove(key);
				File fileToDelete = new File(Peer.chunksDir, key.toString());
				fileToDelete.delete();
			}
		}
		
		System.out.println("End of delete response!");
		
		Peer.recordsDatabaseToFile();
	}

}
