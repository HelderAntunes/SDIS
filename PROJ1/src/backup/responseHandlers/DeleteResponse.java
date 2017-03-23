package backup.responseHandlers;

import java.io.File;
import java.io.IOException;
import java.util.Set;

import backup.Peer;
import backup.Utils;

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
		
		Set<String> keys = Peer.backupDB.keySet();
		for(String key : keys) {
			String fileIDOfChunk = key.substring(0, Utils.SIZE_OF_FILEID);
			
			if (fileIDOfChunk.equals(fileID)) {
				Peer.backupDB.remove(key);
				File fileToDelete = new File(Peer.chunksDir, key);
				fileToDelete.delete();
			}
		}
		
		System.out.println("End of delete response!");

	}

}
