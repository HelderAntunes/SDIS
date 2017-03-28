package backup.initiators;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.Arrays;

import backup.MetaDataChunk;
import backup.Peer;
import backup.Utils;

public class RestoreInit implements Runnable {


	private Peer peer;
	private MetaDataChunk chunk;
	private MulticastSocket mc;

	public RestoreInit(Peer peer, File file, int chunkNo) {

		this.peer = peer;
		String fileID = Utils.getFileId(file.getName() + Integer.toString((int)file.lastModified()));
		this.chunk = new MetaDataChunk(fileID, chunkNo, -1);

		try {
			this.mc = new MulticastSocket(peer.getMcPort());			
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	@Override
	public void run() {

		try {

			System.out.println("Init of RestoreInit thread");

			InetAddress addr = InetAddress.getByName(this.peer.getMcIP());

			byte[] msg = this.createMsg();
			mc.send(new DatagramPacket(msg, msg.length, addr, peer.getMcPort()));   
			
			
			MulticastSocket mdr = new MulticastSocket(peer.getMdrPort());
			mdr.joinGroup(InetAddress.getByName(this.peer.getMdrIP()));
			
			while(true) {

				byte[] buf = new byte[Utils.MAX_SIZE_CHUNK+3000];
				DatagramPacket msgRcvd = new DatagramPacket(buf, buf.length);
				mdr.receive(msgRcvd);
				buf = Arrays.copyOfRange(buf, 0, msgRcvd.getLength());
				String msgRcvdString = new String(buf, 0, buf.length);
				String[] result = msgRcvdString.split("\\s+");
				//  "CHUNK <Version> <SenderId> <FileId> <ChunkNo> <CRLF><CRLF><Body>"
				if (result[0].equals("CHUNK")) {
					String fileID = result[3];
					int chunkNO = Integer.parseInt(result[4]);
					if (fileID.equals(this.chunk.fileId) && chunkNO == this.chunk.chunkNo) {
						byte[] body = Utils.getBodyOfMsg(buf);
						this.saveRestoredChunk(body);
						break;
					}
				}
				
			}

			mdr.close();

			System.out.println("End of RestoreInit thread");

		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	/**
	 * Message format:
	 * "GETCHUNK <Version> <SenderId> <FileId> <ChunkNo> <CRLF><CRLF>"
	 * @return message
	 */
	private byte[] createMsg() {

		String fileID = this.chunk.fileId;
		String senderID = Integer.toString(this.peer.getServerID());
		String version = this.peer.getProtocolVersion();
		String chunkNo = Integer.toString(this.chunk.chunkNo);
		String msg = "GETCHUNK " + version + " " + senderID + " " + fileID + " " + 
				chunkNo + " \r\n\r\n";

		return msg.getBytes();
	}

	private void saveRestoredChunk(byte[] body) {

		try {
			File file = new File(Peer.chunksRestoredDir, chunk.toString());
			FileOutputStream outputStream = new FileOutputStream(file);
			outputStream.write(body);
			outputStream.close();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
