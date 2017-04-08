package backup.responseHandlers;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;

import backup.Peer;
import backup.Utils;

public class ValidResponse implements Runnable {

	private Peer peer;
	private byte[] msgRcvd;
	private String[] msgRcvdString;
	private MulticastSocket mc;

	public ValidResponse(Peer peer, byte[] msgRcvd) throws IOException {
		this.peer = peer;
		this.msgRcvd = msgRcvd;
		this.msgRcvdString = new String(this.msgRcvd, 0, this.msgRcvd.length).split("\\s+");
		try {
			this.mc = new MulticastSocket(peer.getMcPort());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void run() {

		String serverID = this.msgRcvdString[2];
		String fileID = this.msgRcvdString[3];

		if (serverID.equals(Integer.toString(this.peer.getServerID()))) {
			return;
		}

		System.out.println("Init of ValidResponse");

		try {
			if (Peer.fileWasDeleted(fileID)) {
				byte[] msgDelete = Peer.getDeleteMsgOfAFile(fileID);
				InetAddress addr;
				addr = InetAddress.getByName(this.peer.getMcIP());
				int attempts = 0;
				int timeOut = 1000;

				while (attempts < Utils.ATTEMPTS_ON_DELETE) {
					this.mc.send(new DatagramPacket(msgDelete, msgDelete.length, addr, this.peer.getMcPort()));
					Thread.sleep(timeOut);
					attempts++;
				}

			}

		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		System.out.println("End of ValidResponse");

	}
}
