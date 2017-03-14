package backup.initiators;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

import backup.Chunk;
import backup.Peer;
import backup.ProtocolUtils;

public class BackupInit implements Runnable {
	
    private Peer peer;
    private MulticastSocket mdb;
    private byte[] body;
    private byte[] msg;
	private Chunk chunk;
	
	public BackupInit(Peer peer, String fileId, int chunkNo, int repDeg, byte[] body) {

        this.peer = peer;
        this.body = body;
        this.chunk = new Chunk(fileId, chunkNo, repDeg);
        this.msg = this.getMsg();
        
        
        this.peer.recordsChunk(chunk);

        try {
            mdb = new MulticastSocket(peer.getMdbPort());
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
	
	/**
     * Message format:
     * "PUTCHUNK <Version> <SenderId> <FileId> <ChunkNo> <ReplicationDeg> <CRLF><CRLF><Body>"
     * @return msg
     */
    private byte[] getMsg() {

        String fileID = ProtocolUtils.getFileId(this.chunk.fileId);
        String senderID = Integer.toString(this.peer.getServerID());
        String version = this.peer.getProtocolVersion();
        String replication = Integer.toString(this.chunk.desiredRepDeg);
        String chunkNo = Integer.toString(this.chunk.chunkNo);
        String msg = "PUTCHUNK " + version + " " + senderID + " " + fileID + " " + chunkNo + " " + replication + " \r\n\r\n";

        byte[] header = msg.getBytes();
        byte[] byte_msg = new byte[header.length + body.length];
        System.arraycopy(header, 0, byte_msg, 0, header.length);
        System.arraycopy(body, 0, byte_msg, header.length, body.length);

        return byte_msg;
    }

	@Override
	public void run() {
		
		try {

			InetAddress addr = InetAddress.getByName(this.peer.getMdbIP());
            mdb.send(new DatagramPacket(this.msg, this.msg.length, addr, this.peer.getMdbPort()));

            int attempts = 0;
            int currRep = 0;
            int timeOut = 1000;
            
            System.out.println("Desirable repDeg: " + this.chunk.desiredRepDeg);
            while (attempts < 5 && currRep < this.chunk.desiredRepDeg) {
            	
            	if (attempts > 0) {
            		mdb.send(new DatagramPacket(msg, msg.length, addr, peer.getMdbPort()));
            	}
             
            	Thread.sleep(timeOut);
            	attempts++;
                timeOut *= 2;
                currRep = this.peer.getReplicationOfChunk(this.chunk);
                System.out.println("Current repDeg: " + currRep);
            }

        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
			e.printStackTrace();
		}
		
	}
	
    
}
