/**
 * Created by helder on 05-03-2017.
 */
public class ServerPeer {

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

    private PutChunk putChunk;

    /**
     * Server program.
     * @param args
     */
    public static void main(String[] args) {

        ServerPeer serverPeer = new ServerPeer(args);

    }

    public ServerPeer(String[] args) {

        this.protocolVersion = args[0];
        this.serverID = Integer.parseInt(args[1]);
        this.serverAccessPoint = args[2];
        this.serverPort = Integer.parseInt(serverAccessPoint); // suppose that run on localhost
        this.mcIP = args[3];
        this.mcPort = Integer.parseInt(args[4]);
        this.mdbIP = args[5];
        this.mdbPort = Integer.parseInt(args[6]);
        this.mdrIP = args[7];
        this.mdrPort = Integer.parseInt(args[8]);




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

}
