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

import backup.listeners.ControlChannelListener;
import backup.listeners.DataChannelListener;
import backup.listeners.RecoveryChannelListener;

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

	// Data to save in file (Meta data)
	public static ConcurrentHashMap<MetaDataChunk, ArrayList<String> > backupDB;
	public static CopyOnWriteArrayList<MetaDataChunk> chunksSaved; 
	public static CopyOnWriteArrayList<MetaDataChunk> myChunks; 
	public static CopyOnWriteArrayList<MetaDataFile> myFiles; 
	
	// Data of messages used to avoid flooding (It is not saved in file)
	public static CopyOnWriteArrayList<String> chunkMsgsReceived; // data used for restore protocol
	public static CopyOnWriteArrayList<String> putChunkMsgsReceived; // data used for reclaim protocol

	public static File chunksDir;
	public static File serverDir;
	public static File chunksRestoredDir;
	public static File filesRestoredDir;
		
	public static int maxSpaceDisk_bytes = Utils.MAX_SPACE_DISK;
	public static int spaceUsed_bytes = 0;


	public Peer(String[] args) throws IOException {

		this.setProtocolVersion(args[0]);
		this.setServerID(Integer.parseInt(args[1]));
		this.serverAccessPoint = args[2];
		this.setMcIP(args[3]);
		this.setMcPort(Integer.parseInt(args[4]));
		this.setMdbIP(args[5]);
		this.setMdbPort(Integer.parseInt(args[6]));
		this.setMdrIP(args[7]);
		this.setMdrPort(Integer.parseInt(args[8]));
		
		Peer.chunkMsgsReceived = new CopyOnWriteArrayList<String>();
		Peer.putChunkMsgsReceived = new CopyOnWriteArrayList<String>();

		this.init();

		new Thread(new ControlChannelListener(this)).start();
		new Thread(new DataChannelListener(this)).start();
		new Thread(new RecoveryChannelListener(this)).start();
		new Thread(new RMIServer(this)).start();
	}

	/**
	 * 
	 * @param args:
	 * 1.0 1 1 224.0.0.1 2000 224.0.0.2 2002 224.0.0.3 2003 processchunk
	 * 1.0 2 1 224.0.0.1 2000 224.0.0.2 2002 224.0.0.3 2003 putchunk
	 */
	public static void main(String[] args) {
		
		try {
			Peer peer = new Peer(args);
			System.out.println("Peer " + peer.serverID + " started...");
		} catch (IOException e) {
			e.printStackTrace();
			return;
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
		
		Peer.filesRestoredDir = new File(Peer.serverDir, Utils.FILES_RESTORED_DIR_NAME);
		
		if (!Peer.filesRestoredDir.exists()) {
			Peer.filesRestoredDir.mkdir();
		}

	}

	@SuppressWarnings("unchecked")
	private void initDatabase() {
		
		try {
			
			File db_file = new File(Peer.serverDir, Utils.DB_FILE_NAME);

			if(db_file.exists() && !db_file.isDirectory()) { 
				ObjectInputStream ois = new ObjectInputStream(new FileInputStream(db_file));
				Peer.backupDB = (ConcurrentHashMap<MetaDataChunk, ArrayList<String>>) ois.readObject();
				Peer.chunksSaved = (CopyOnWriteArrayList<MetaDataChunk>) ois.readObject();
				Peer.myChunks = (CopyOnWriteArrayList<MetaDataChunk>) ois.readObject();
				Peer.myFiles = (CopyOnWriteArrayList<MetaDataFile>) ois.readObject();
				ois.close();
			}
			else {
				Peer.backupDB = new ConcurrentHashMap<MetaDataChunk, ArrayList<String> >();
				Peer.chunksSaved = new CopyOnWriteArrayList<MetaDataChunk>();
				Peer.myChunks = new CopyOnWriteArrayList<MetaDataChunk>();
				Peer.myFiles = new CopyOnWriteArrayList<MetaDataFile>();
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
	
	public String getServerAccessPoint() {
		return serverAccessPoint;
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

	public ConcurrentHashMap<MetaDataChunk, ArrayList<String> > getBackupDB() {
		return backupDB;
	}

	public void setBackupDB(ConcurrentHashMap<MetaDataChunk, ArrayList<String> > backupDB) {
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
		return Peer.backupDB.get(chunk);
	}

	/**
	 * Check if the peer backup the chunk.
	 * @param peerId
	 * @param chunk
	 * @return true if the peer backup the chunk, false other wise.
	 */
	public static boolean backUpAChunkPreviously(String peerId, MetaDataChunk chunk) {

		if (!Peer.backupDB.containsKey(chunk))
			return false;

		ArrayList<String> peersThatBackedUp = Peer.backupDB.get(chunk);
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
		if (!Peer.backupDB.containsKey(chunk)) {
			return 0;
		}

		ArrayList<String> peersThatBackedUp = Peer.backupDB.get(chunk);
		return peersThatBackedUp.size();
	}

	/**
	 * Records backup if that backup was not recorded.
	 * @param chunk
	 * @param peerId
	 */
	public static void recordsBackupIfNeeded(MetaDataChunk chunk, String peerId) {

		if (!Peer.backupDB.containsKey(chunk)) {
			ArrayList<String> peersThatBackedUp = new ArrayList<String>();
			peersThatBackedUp.add(peerId);
			Peer.backupDB.put(chunk, peersThatBackedUp);
		}
		else {
			if (!Peer.backUpAChunkPreviously(peerId, chunk)) {
				ArrayList<String> peersThatBackedUp = Peer.backupDB.get(chunk);
				peersThatBackedUp.add(peerId);
			}
		}

	}

	/**
	 * Save the meta-data in non-volatile memory.
	 */
	public static synchronized void recordsDatabaseToFile() {

		try {
			File db_file = new File(Peer.serverDir, Utils.DB_FILE_NAME);
			FileOutputStream fout = new FileOutputStream(db_file);
			ObjectOutputStream oos = new ObjectOutputStream(fout);
			oos.writeObject(Peer.backupDB);
			oos.writeObject(Peer.chunksSaved);
			oos.writeObject(Peer.myChunks);
			oos.writeObject(Peer.myFiles);
			oos.close();
			fout.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
	
	public static String printState() {
		
		StringBuilder sb = new StringBuilder("");
		
		sb.append("Local service state information\n\n");
		
		sb.append("Files whose backup it has initiated:\n");
		if (Peer.myFiles.size() == 0)
			sb.append("No backup has initiated.\n");
		for (MetaDataFile myFile: Peer.myFiles) {
			sb.append("Pathname: " + myFile.path + "\n");
			sb.append("Id in the backup service: " + myFile.id + "\n");
			sb.append("Desired replication degree: " + myFile.repDeg + "\n");
			sb.append("\nChunks of file:\n");
			
			for (MetaDataChunk myChunk: Peer.myChunks) {
				sb.append("Id of chunk: " + myChunk.toString() + "\n");
				sb.append("Perceived replication degree: " + Peer.backupDB.get(myChunk).size() + "\n");
			}
		}
		
		sb.append("\nChunks stored:\n");
		if (Peer.chunksSaved.size() == 0)
			sb.append("No chunk was stored.\n");
		for (int i = 0; i < Peer.chunksSaved.size(); i++) {
			MetaDataChunk chunk = Peer.chunksSaved.get(i);
			sb.append("id: " + chunk.toString() + "\n");
			long size_kbytes = new File(Peer.chunksDir, chunk.toString()).length() / 1000;
			sb.append("size (KBytes): " + size_kbytes + "\n");
			sb.append("Perceived replication degree: " + Peer.backupDB.get(chunk).size() + "\n");	
		}
		
		sb.append("\nTotal of space in disk (KBytes): " + Peer.maxSpaceDisk_bytes/1000 + "\n");
		sb.append("Total of used space (KBytes): " + Peer.spaceUsed_bytes/1000 + "\n");

		
		return sb.toString();
	}
	
	public static boolean isMyChunk(MetaDataChunk chunk) {
		
		for (MetaDataChunk c: Peer.myChunks) {
			if (c.equals(chunk)) {
				return true;
			}
		}
		
		return false;
	}
	
	public void printTab(int numSpaces) {
		for (int i = 0; i < numSpaces; i++) {
			System.out.print(" ");
		}
	}
	
	public static synchronized void remPutChunkMsgOfAChunk(MetaDataChunk chunk) {
		for (int i = 0; i < Peer.putChunkMsgsReceived.size(); i++) {
			String[] msgPutChunk = Peer.putChunkMsgsReceived.get(i).split("\\s+");
			String fileID_msg = msgPutChunk[3];
			int chunkNO_msg = Integer.parseInt(msgPutChunk[4]);
			if (fileID_msg.equals(chunk.fileId) && chunkNO_msg == chunk.chunkNo) {
				Peer.putChunkMsgsReceived.remove(i);
				break;
			}
		}
	}

}
