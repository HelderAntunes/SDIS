package backup.listeners;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import backup.Peer;
import backup.Utils;
import backup.responseHandlers.BackupResponse;

public class DataChannelListener implements Runnable {
	
	private Peer peer;
    private MulticastSocket mdb;
    private ExecutorService executor = Executors.newFixedThreadPool(5);
    
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
				e.printStackTrace();
			}
		}
		
	}
	
	public void processRequests() throws IOException {

        byte[] buf = new byte[Utils.MAX_SIZE_CHUNK+1024];
        DatagramPacket msgRcvd = new DatagramPacket(buf, buf.length);
        mdb.receive(msgRcvd);
		buf = Arrays.copyOfRange(buf, 0, msgRcvd.getLength());
        String[] result = new String(buf, 0, buf.length).split("\\s+");

        if (result.length == 0)
        	return;
        
        if (result[2].equals(Integer.toString(this.peer.getServerID()))) {
			return;
		}
        
        if (result[0].equals("PUTCHUNK")) {
        	Peer.putChunkMsgsReceived.add(new String(buf, 0, buf.length));
        	this.executor.execute(new Thread(new BackupResponse(this.peer, buf)));
        }
 
    }

}
