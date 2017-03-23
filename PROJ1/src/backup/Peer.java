package backup;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import backup.initiators.Initiator;
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

	public static ConcurrentHashMap<String, ArrayList<String> > backupDB; // chunk string -> peersThatSavedThe chunk[]
	public static ConcurrentHashMap<String, String > nameFileToFileID; 
	
	public static CopyOnWriteArrayList<String> chunkReceived;

	public static File chunksDir;
	public static File serverDir;
	public static File chunksRestoredDir;

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
		
		Peer.chunkReceived = new CopyOnWriteArrayList<String>();

		this.init();

		new Thread(new ControlChannelListener(this)).start();
		new Thread(new DataChannelListener(this)).start();
	}

	/**
	 * 
	 * @param args:
	 * 1.0 1 1 224.0.0.1 2000 224.0.0.2 2002 224.0.0.3 2003 processchunk
	 * 1.0 2 1 224.0.0.1 2000 224.0.0.2 2002 224.0.0.3 2003 putchunk
	 */
	public static void main(String[] args) {
		Peer peer;
		try {
			peer = new Peer(args);
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}

		if (args[9].equals("putchunk")) {
			System.out.println("client peer");
			String[] argsInitiator = {"/home/helder/workspace/sdis-proj1/files/WWE   Rest In Peace The Undertaker.mp3", "1"};
			new Initiator(peer, "putchunk", argsInitiator);
		}
		else if (args[9].equals("deletefile")) {
			System.out.println("client peer");
			String[] argsInitiator = {"/home/helder/workspace/sdis-proj1/files/WWE   Rest In Peace The Undertaker.mp3"};
			new Initiator(peer, "deletefile", argsInitiator);
		}
		else if (args[9].equals("getfile")) {
			System.out.println("client peer");
			String[] argsInitiator = {"/home/helder/workspace/sdis-proj1/files/WWE   Rest In Peace The Undertaker.mp3", "0"};
			new Initiator(peer, "getfile", argsInitiator);
		}
		else if (args[9].equals("server")){
			System.out.println("server peer");
		}
		
	}
	
	private void init() {
		this.initDirectories();
		this.initDatabase();
	}
	
	private void initDirectories() {

		Peer.serverDir = new File(Integer.toString(this.serverID));

		if (!serverDir.exists()) {
			serverDir.mkdir();
		}

		Peer.chunksDir = new File(Peer.serverDir, Utils.CHUNKS_DIR_NAME);

		if (!Peer.chunksDir.exists()) {
			Peer.chunksDir.mkdir();
		}
		
		Peer.chunksRestoredDir = new File(Peer.serverDir, Utils.CHUNKS_RESTORED_DIR_NAME);
		
		if (!Peer.chunksRestoredDir.exists()) {
			Peer.chunksRestoredDir.mkdir();
		}

	}

	@SuppressWarnings("unchecked")
	private void initDatabase() {
		
		try {
			
			File db_file = new File(Peer.serverDir, Utils.DB_FILE_NAME);

			if(db_file.exists() && !db_file.isDirectory()) { 
				ObjectInputStream ois = new ObjectInputStream(new FileInputStream(db_file));
				Peer.backupDB = (ConcurrentHashMap<String, ArrayList<String>>) ois.readObject();
				Peer.nameFileToFileID = (ConcurrentHashMap<String, String>) ois.readObject();
				ois.close();
			}
			else {
				Peer.backupDB = new ConcurrentHashMap<String, ArrayList<String> >();
				Peer.nameFileToFileID = new ConcurrentHashMap<String, String>();
			}

		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

	/***************************************************/
	/********* BEGIN OF GETTERS AND SETTERS ************/
	/***************************************************/

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

	/*************************************************/
	/********* END OF GETTERS AND SETTERS ************/
	/*************************************************/

	/**
	 * Get peers that saved a chunk.
	 * @param chunk
	 * @return array of peer's string
	 */
	public static ArrayList<String> getPeersThatSavedTheChunk(MetaDataChunk chunk) {
		return Peer.backupDB.get(chunk.toString());
	}

	/**
	 * Check if the peer backup the chunk.
	 * @param peerId
	 * @param chunk
	 * @return true if the peer backup the chunk, false other wise.
	 */
	public static boolean backUpAChunkPreviously(String peerId, MetaDataChunk chunk) {

		if (!Peer.backupDB.containsKey(chunk.toString()))
			return false;

		ArrayList<String> peersThatBackedUp = Peer.backupDB.get(chunk.toString());
		for (String id: peersThatBackedUp)
			if (id.equals(peerId))
				return true;

		return false; 
	}

	/**
	 * Get the current replication of a chunk.
	 * @param chunk
	 * @return current replication of chunk
	 */
	public static int getReplicationOfChunk(MetaDataChunk chunk) {
		if (!Peer.backupDB.containsKey(chunk.toString())) {
			return 0;
		}

		ArrayList<String> peersThatBackedUp = Peer.backupDB.get(chunk.toString());
		return peersThatBackedUp.size();
	}

	/**
	 * Records backup if that backup was not recorded.
	 * @param chunk
	 * @param peerId
	 */
	public static void recordsBackupIfNeeded(MetaDataChunk chunk, String peerId) {

		if (!Peer.backupDB.containsKey(chunk.toString())) {
			ArrayList<String> peersThatBackedUp = new ArrayList<String>();
			peersThatBackedUp.add(peerId);
			Peer.backupDB.put(chunk.toString(), peersThatBackedUp);
		}
		else {
			if (!Peer.backUpAChunkPreviously(peerId, chunk)) {
				ArrayList<String> peersThatBackedUp = Peer.backupDB.get(chunk.toString());
				peersThatBackedUp.add(peerId);
				Peer.backupDB.remove(chunk.toString());
				Peer.backupDB.put(chunk.toString(), peersThatBackedUp);
			}
		}

	}

	/**
	 * Save the meta-data in non-volatile memory.
	 */
	public synchronized void recordsDatabaseToFile() {

		try {
			File db_file = new File(Peer.serverDir, Utils.DB_FILE_NAME);
			FileOutputStream fout = new FileOutputStream(db_file);
			ObjectOutputStream oos = new ObjectOutputStream(fout);
			oos.writeObject(Peer.backupDB);
			oos.writeObject(Peer.nameFileToFileID);
			oos.close();
			fout.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

}
