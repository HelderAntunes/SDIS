package backup.responseHandlers;

import java.io.IOException;

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
		
		if (!this.peer.checkIfSaveAChunkOfAFile(fileID)) {
			return;
		}
		
		System.out.println("Init of DeleteResponse");
		
		Peer.deleteDataAssociatedWithAFileSaved(fileID);
		Peer.recordsDatabaseToFile();
		
		System.out.println("End of DeleteResponse");
	}

}
