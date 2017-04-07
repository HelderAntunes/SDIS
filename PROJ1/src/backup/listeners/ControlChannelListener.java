package backup.listeners;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import backup.MetaDataChunk;
import backup.Peer;
import backup.Utils;
import backup.responseHandlers.DeleteResponse;
import backup.responseHandlers.DeleteResponseEnh;
import backup.responseHandlers.ReclaimResponse;
import backup.responseHandlers.RestoreResponse;

public class ControlChannelListener implements Runnable {

	private Peer peer;
	private MulticastSocket mc;
	private ExecutorService executor = Executors.newFixedThreadPool(Utils.MAX_NO_THREADS);

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
				e.printStackTrace();
			}
		}
	}

	private void processRequests() throws IOException {

		byte[] buf = new byte[1024];
		DatagramPacket msgRcvd = new DatagramPacket(buf, buf.length);
		mc.receive(msgRcvd);
		
		buf = Arrays.copyOfRange(buf, 0, msgRcvd.getLength());

		String[] result = new String(buf, 0, buf.length).split("\\s+");
		if (result.length == 0)
			return;
		
		if (result[0].equals("STORED")) {
			Peer.storedMsgsReceived.add(new String(buf, 0, buf.length));
			MetaDataChunk c = new MetaDataChunk(result[3], Integer.parseInt(result[4]), 1);
			Peer.recordsBackupIfNeeded(c, result[2]);
			Peer.remPutChunkMsgOfAChunk(c);
			Peer.recordsDatabaseToFile();
		}

		if (result[2].equals(Integer.toString(this.peer.getServerID()))) {
			return;
		}

		if (result[0].equals("DELETE")) {

			if (this.peer.getProtocolVersion().equals("1.0")) 
				this.executor.execute(new Thread(new DeleteResponse(this.peer, buf)));
			else 
				this.executor.execute(new Thread(new DeleteResponseEnh(this.peer, buf)));

		}
		else if (result[0].equals("DELETED")) {

			Peer.deletedMsgReceived.add(new String(buf, 0, buf.length));

		}
		else if (result[0].equals("GETCHUNK")) {

			this.executor.execute(new Thread(new RestoreResponse(this.peer, buf)));

		}
		else if (result[0].equals("REMOVED")) {

			this.executor.execute(new Thread(new ReclaimResponse(this.peer, buf)));

		}

	}
}
