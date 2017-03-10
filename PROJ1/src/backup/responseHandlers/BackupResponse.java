package backup.responseHandlers;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;
import java.util.Random;

import backup.Chunk;
import backup.Peer;

public class BackupResponse implements Runnable {
	
	private MulticastSocket mc;
	private Peer peer;
	private String[] msgReceived;
	
	public BackupResponse(Peer peer, String[] msgReceived) throws IOException {

        this.peer = peer;
        this.mc = new MulticastSocket(peer.getMcPort());
        this.msgReceived = msgReceived;
        
    }

	@Override
	public void run() {
		
		Chunk chunk = new Chunk(msgReceived[3], Integer.parseInt(msgReceived[4]), Integer.parseInt(msgReceived[5]));
		int repDeg = Integer.parseInt(msgReceived[5]);
		
		if (this.peer.peerBackUpAChunk(Integer.toString(this.peer.getServerID()), chunk)) {
			this.sendConfirmation();
		}
		else if (this.peer.getReplicationOfChunk(chunk) < repDeg){
			this.peer.recordsBackupIfNeeded(chunk, Integer.toString(this.peer.getServerID()));
			this.sendConfirmation();
		}
		
	}
	
	/**
	 * Send confirmation of storing of chunk to control channel.
	 * PUTCHUNK command: "PUTCHUNK <Version> <SenderId> <FileId> <ChunkNo> <ReplicationDeg> <CRLF><CRLF><Body>"
	 * STORED command: "STORED <Version> <SenderId> <FileId> <ChunkNo> <CRLF><CRLF>"
	 */
	private void sendConfirmation() {
		
        try {
        	String confirmation = "STORED " + this.peer.getProtocolVersion() 
        						+ " " + Integer.toString(this.peer.getServerID()) +
        						" " + this.msgReceived[3] + " " + this.msgReceived[4] + " \r\n\r\n";
    		byte[] byte_msg = confirmation.getBytes();
    		
    		Random rand = new Random();
            int  n = rand.nextInt(400) + 1;
			Thread.sleep(n);
			
			InetAddress addr = InetAddress.getByName(peer.getMdbIP());
		    this.mc.send(new DatagramPacket(byte_msg, byte_msg.length, addr, peer.getMdbPort()));
		    
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
	}
	
}
