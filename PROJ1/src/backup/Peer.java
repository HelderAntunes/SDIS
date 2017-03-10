package backup;
import java.util.ArrayList;
import java.util.HashMap;

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

    private HashMap<Chunk, ArrayList<String> > backupDB;
    
    public Peer(String[] args) {

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

        this.setBackupDB(new HashMap<Chunk, ArrayList<String> >());

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

	public HashMap<Chunk, ArrayList<String> > getBackupDB() {
		return backupDB;
	}

	public void setBackupDB(HashMap<Chunk, ArrayList<String> > backupDB) {
		this.backupDB = backupDB;
	}
	
	public boolean peerBackUpAChunk(String peerId, Chunk chunk) {
		ArrayList<String> peersThatBackedUp = this.backupDB.get(chunk);
		if (peersThatBackedUp == null)
			return false;
		
		for (String id: peersThatBackedUp)
			if (id.equals(peerId))
				return true;
		
		return false; 
	}
	
	public int getReplicationOfChunk(Chunk chunk) {
		ArrayList<String> peersThatBackedUp = this.backupDB.get(chunk);
		if (peersThatBackedUp == null)
			return 0;
		else
			return peersThatBackedUp.size();
	}
	
	public void recordsBackupIfNeeded(Chunk chunk, String peerId) {
		ArrayList<String> peersThatBackedUp = this.backupDB.get(chunk);
		if (peersThatBackedUp == null) {
			peersThatBackedUp = new ArrayList<String>();
			peersThatBackedUp.add(peerId);
			this.backupDB.put(chunk, peersThatBackedUp);
		}
		else if (!this.peerBackUpAChunk(peerId, chunk)) {
			peersThatBackedUp.add(peerId);
		}
		return;
	}
    
}
