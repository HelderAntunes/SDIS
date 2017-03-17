package backup;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import backup.initiators.BackupInit;
import backup.listeners.ControlChannelListener;
import backup.listeners.DataChannelListener;

public class Peer {

	private String protocolVersion;
	private int serverID;
	private String serverAccessPoint;
	private int serverPort;
	private String mcIP;
	private int mcPort;
	private String mdbIP;
	private int mdbPort;
	private String mdrIP;
	private int mdrPort;

	public static ConcurrentHashMap<String, ArrayList<String> > backupDB = new ConcurrentHashMap<String, ArrayList<String> >();
	private static CopyOnWriteArrayList<String> chunksRecorded = new CopyOnWriteArrayList<String>();
	private Thread controlChannel;
	private Thread dataChannel;
	
	public static File chunksDir;
	
	public int teste = 0;

	public Peer(String[] args) throws IOException {

		this.setProtocolVersion(args[0]);
		this.setServerID(Integer.parseInt(args[1]));
		this.serverAccessPoint = args[2];
		this.setServerPort(Integer.parseInt(serverAccessPoint)); // suppose that run on localhost
		this.setMcIP(args[3]);
		this.setMcPort(Integer.parseInt(args[4]));
		this.setMdbIP(args[5]);
		this.setMdbPort(Integer.parseInt(args[6]));
		this.setMdrIP(args[7]);
		this.setMdrPort(Integer.parseInt(args[8]));

		this.controlChannel = new Thread(new ControlChannelListener(this));
		this.dataChannel = new Thread(new DataChannelListener(this));
		this.controlChannel.start();
		this.dataChannel.start();
		
		this.mkChunksDirIfNotExists();
	}

	public static void main(String[] args) throws IOException {
				
		Peer peer = new Peer(args);

		if (args[9].equals("putchunk")) {
			System.out.println("client peer");
			new Thread(new BackupInit(peer, "fileID", 0, 1, new String("test putchunk").getBytes())).start();
		}
		else if (args[9].equals("processchunk")){
			System.out.println("server peer");
		}
	}
	
	public void mkChunksDirIfNotExists() {
		
		File serverDir = new File(Integer.toString(this.serverID));

		if (!serverDir.exists()) {
			serverDir.mkdir();
		}
		
		Peer.chunksDir = new File(serverDir, "chunks");

		if (!Peer.chunksDir.exists()) {
			Peer.chunksDir.mkdir();
		}
	}

	public String getProtocolVersion() {
		return protocolVersion;
	}

	public void setProtocolVersion(String protocolVersion) {
		this.protocolVersion = protocolVersion;
	}

	public int getServerID() {
		return serverID;
	}

	public void setServerID(int serverID) {
		this.serverID = serverID;
	}

	public int getServerPort() {
		return serverPort;
	}

	public void setServerPort(int serverPort) {
		this.serverPort = serverPort;
	}

	public String getMcIP() {
		return mcIP;
	}

	public void setMcIP(String mcIP) {
		this.mcIP = mcIP;
	}

	public int getMcPort() {
		return mcPort;
	}

	public void setMcPort(int mcPort) {
		this.mcPort = mcPort;
	}

	public String getMdbIP() {
		return mdbIP;
	}

	public void setMdbIP(String mdbIP) {
		this.mdbIP = mdbIP;
	}

	public int getMdbPort() {
		return mdbPort;
	}

	public void setMdbPort(int mdbPort) {
		this.mdbPort = mdbPort;
	}

	public String getMdrIP() {
		return mdrIP;
	}

	public void setMdrIP(String mdrIP) {
		this.mdrIP = mdrIP;
	}

	public int getMdrPort() {
		return mdrPort;
	}

	public void setMdrPort(int mdrPort) {
		this.mdrPort = mdrPort;
	}

	public ConcurrentHashMap<String, ArrayList<String> > getBackupDB() {
		return backupDB;
	}

	public void setBackupDB(ConcurrentHashMap<String, ArrayList<String> > backupDB) {
		Peer.backupDB = backupDB;
	}

	public static boolean peerBackUpAChunk(String peerId, MetaDataChunk chunk) {
		
		if (!Peer.backupDB.containsKey(chunk.toString()))
			return false;
		
		ArrayList<String> peersThatBackedUp = Peer.backupDB.get(chunk.toString());
		for (String id: peersThatBackedUp)
			if (id.equals(peerId))
				return true;

		return false; 
	}

	public static int getReplicationOfChunk(MetaDataChunk chunk) {
		if (!Peer.backupDB.containsKey(chunk.toString())) {
			return 0;
		}
		
		ArrayList<String> peersThatBackedUp = Peer.backupDB.get(chunk.toString());
		return peersThatBackedUp.size();

	}

	public static void recordsBackupIfNeeded(MetaDataChunk chunk, String peerId) {
		
		
		if (!Peer.backupDB.containsKey(chunk.toString())) {
			ArrayList<String> peersThatBackedUp = new ArrayList<String>();
			peersThatBackedUp.add(peerId);
			Peer.backupDB.put(chunk.toString(), peersThatBackedUp);
		}
		else {
			ArrayList<String> peersThatBackedUp = Peer.backupDB.get(chunk.toString());
			peersThatBackedUp.add(peerId);
			Peer.backupDB.remove(chunk.toString());
			Peer.backupDB.put(chunk.toString(), peersThatBackedUp);
		}
	
	}

	public static CopyOnWriteArrayList<String> getChunksRecorded() {
		return chunksRecorded;
	}

	public static void setChunksRecorded(CopyOnWriteArrayList<String> chunksRecorded) {
		Peer.chunksRecorded = chunksRecorded;
	}
	
	public static ArrayList<String> getPeersThatSavedTheChunk(MetaDataChunk chunk) {
		return Peer.backupDB.get(chunk.toString());
	}

}
