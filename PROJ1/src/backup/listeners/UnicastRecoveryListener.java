package backup.listeners;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.Arrays;

import backup.Peer;
import backup.Utils;

public class UnicastRecoveryListener implements Runnable {

	private Peer peer;
	private DatagramSocket socket;


	public UnicastRecoveryListener(Peer peer) {

		this.peer = peer;
		try {
			this.socket = new DatagramSocket(Utils.PORT_UNICAST_RECOVERY_LISTENER);
		} catch (SocketException e) {
			e.printStackTrace();
		}

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

		byte[] buf = new byte[Utils.MAX_SIZE_CHUNK+1000];
		DatagramPacket packet = new DatagramPacket(buf, buf.length);

		this.socket.receive(packet);

		buf = Arrays.copyOfRange(buf, 0, packet.getLength());
		String msgRcvdString = new String(buf, 0, buf.length);
		String[] result = msgRcvdString.split("\\s+");

		if (result[0].equals("CHUNK")) {
			if (!result[2].equals(Integer.toString(this.peer.getServerID()))) {				
				Peer.chunkMsgsReceived.add(buf);
			}
		}

	}

}
