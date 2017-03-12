package backup.listeners;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

import backup.Peer;
import backup.responseHandlers.BackupResponse;

public class DataChannelListener implements Runnable {
	
	private Peer peer;
    private MulticastSocket mdb;
    
    public DataChannelListener(Peer peer) throws IOException {

        this.peer = peer;
        this.mdb = new MulticastSocket(peer.getMdbPort());
        this.mdb.joinGroup(InetAddress.getByName(this.peer.getMdbIP()));
        
    }
    
	@Override
	public void run() {
		
		while (true) {
		    try {
				this.processRequests();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}
	
	public void processRequests() throws IOException {

        byte[] buf = new byte[1024];
        DatagramPacket msgRcvd = new DatagramPacket(buf, buf.length);
        mdb.receive(msgRcvd);

        String[] result = new String(buf, 0, buf.length).split("\\s+");
        // PUTCHUNK <Version> <SenderId> <FileId> <ChunkNo> <ReplicationDeg> <CRLF><CRLF><Body>

        if (result.length == 0)
        	return;
        
        if (result[0].equals("PUTCHUNCK")) {
        	BackupResponse backupResponse = new BackupResponse(this.peer, result);
        	backupResponse.run();
        }
 

    }

}
