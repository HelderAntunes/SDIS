package backup.listeners;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.Arrays;

import backup.Peer;
import backup.Utils;

public class RecoveryChannelListener implements Runnable {

	Peer peer;
	private MulticastSocket mdr;

	public RecoveryChannelListener(Peer peer) throws IOException {
		this.peer = peer;
		this.mdr = new MulticastSocket(peer.getMdrPort());
		this.mdr.joinGroup(InetAddress.getByName(peer.getMdrIP()));
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

		byte[] buf = new byte[Utils.MAX_SIZE_CHUNK+1024];
		DatagramPacket msgRcvd = new DatagramPacket(buf, buf.length);
		this.mdr.receive(msgRcvd);
		
		buf = Arrays.copyOfRange(buf, 0, msgRcvd.getLength());
		String msgRcvdString = new String(buf, 0, buf.length);
		String[] result = msgRcvdString.split("\\s+");
		
		if (result.length == 0)
			return;
		
		if (result[0].equals("CHUNK")) {
			if (!result[2].equals(Integer.toString(this.peer.getServerID()))) {				
				Peer.chunkMsgsReceived.add(buf);
			}
		}
		
	}

}
