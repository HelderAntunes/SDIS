package backup.responseHandlers;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Random;

import backup.MetaDataChunk;
import backup.Peer;

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
		
		MetaDataChunk chunk = new MetaDataChunk(msgRcvdString[3], Integer.parseInt(msgRcvdString[4]), Integer.parseInt(msgRcvdString[5]));
			
		if (this.peer.getServerID() == Integer.parseInt(msgRcvdString[2])) {
			return;
		}
		
		if (Peer.peerBackUpAChunk(Integer.toString(this.peer.getServerID()), chunk)) {
			this.sendConfirmation();
		}
		else if (Peer.getReplicationOfChunk(chunk) < chunk.desiredRepDeg){
			Peer.recordsBackupIfNeeded(chunk, Integer.toString(this.peer.getServerID()));
			Peer.getChunksRecorded().add(chunk.toString());
			this.saveChunkInFileSystem(chunk);
			this.sendConfirmation();
		}
		
	}
	
	private void saveChunkInFileSystem(MetaDataChunk chunk) {
		File file = new File(Peer.chunksDir, chunk.toString());
	    FileOutputStream outputStream;
		try {
			outputStream = new FileOutputStream(file);
			outputStream.write(this.getBodyOfMsg());
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private byte[] getBodyOfMsg() {
		byte[] bodyMsg = null;
		for (int i = 0; i < this.msgRcvd.length-3; i++) {
			if (this.msgRcvd[i] == (byte)'\r' && this.msgRcvd[i+1] == (byte)'\n' && 
					this.msgRcvd[i+2] == (byte)'\r' && this.msgRcvd[i+3] == (byte)'\n') {
				bodyMsg = Arrays.copyOfRange(this.msgRcvd, i+4, this.msgRcvd.length);
			}
		}
		if (bodyMsg == null) {
			System.err.println("attr bodyMsg is null (BackupResponse.java 77)");
		}
		return bodyMsg;
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
        						" " + this.msgRcvdString[3] + " " + this.msgRcvdString[4] + " \r\n\r\n";
    		byte[] byte_msg = confirmation.getBytes();
    		
    		Random rand = new Random();
            int  n = rand.nextInt(400) + 1;
			Thread.sleep(n);
			
			InetAddress addr = InetAddress.getByName(peer.getMcIP());
		    this.mc.send(new DatagramPacket(byte_msg, byte_msg.length, addr, peer.getMcPort()));
		    
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
