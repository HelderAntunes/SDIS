package backup.listeners;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.ArrayList;

import backup.Chunk;
import backup.Peer;

public class ControlChannelListener implements Runnable {
	
	private Peer peer;
    private MulticastSocket mc;
    
    public ControlChannelListener(Peer peer) throws IOException {
    	this.peer = peer;
    	this.mc = new MulticastSocket(peer.getMcPort());
    	mc.joinGroup(InetAddress.getByName(this.peer.getMcIP()));
   
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

	private void processRequests() throws IOException {
		
		byte[] buf = new byte[1024];
		DatagramPacket msgRcvd = new DatagramPacket(buf, buf.length);
		mc.receive(msgRcvd);
		
		String[] result = new String(buf, 0, buf.length).split("\\s+");
		if (result.length == 0)
			return;
		
		// "STORED <Version> <SenderId> <FileId> <ChunkNo> <CRLF><CRLF>"
		if (result[0].equals("STORED")) {
			ArrayList<String> a = this.peer.getBackupDB().get(new Chunk(result[3], Integer.parseInt(result[4]), 1));
			System.out.println(a.size());
			this.peer.recordsBackupIfNeeded(new Chunk(result[3], Integer.parseInt(result[4]), 1), result[2]);
		}
		
	}
}
