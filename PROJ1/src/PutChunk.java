import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

/**
 * Created by helder on 06-03-2017.
 */
public class PutChunk implements Runnable {

    private ServerPeer serverPeer;
    private MulticastSocket mdb;
    private MulticastSocket mc;
    private String fileId;
    private int chunckNo;
    private int repDeg;
    private byte[] body;
    private byte[] msg;

    public PutChunk(ServerPeer server, String fileId, int chunckNo, int repDeg, byte[] body) {

        this.serverPeer = server;
        this.fileId = fileId;
        this.chunckNo = chunckNo;
        this.repDeg = repDeg;
        this.body = body;
        this.msg = this.getMsg();

        try {
            mdb = new MulticastSocket(server.getMdbPort());
            mc = new MulticastSocket(server.getMcPort());
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void run() {

        try {
            mdb.send(new DatagramPacket(msg, msg.length, InetAddress.getByName(serverPeer.getMdbIP()), serverPeer.getMdbPort()));

            // TODO: ler do MC, e retransmitir o maximo 5 vezes se necessario (http://stackoverflow.com/questions/8437454/java-mulitcastsocket-receive-method-blocks-a-program)

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * Message format:
     * "PUTCHUNK <Version> <SenderId> <FileId> <ChunkNo> <ReplicationDeg> <CRLF><CRLF><Body>"
     * @return msg
     */
    private byte[] getMsg() {

        String fileID = ProtocolUtils.getFileId(this.fileId);
        String senderID = Integer.toString(this.serverPeer.getServerID());
        String version = this.serverPeer.getProtocolVersion();
        String replication = Integer.toString(this.repDeg);
        String chunkNo = Integer.toString(this.chunckNo);
        String msg = "PUTCHUNK " + version + " " + senderID + " " + fileID + " " + chunkNo + " " + replication + " \r\n\r\n";

        byte[] header = msg.getBytes();
        byte[] byte_msg = new byte[header.length + body.length];
        System.arraycopy(header, 0, byte_msg, 0, header.length);
        System.arraycopy(body, 0, byte_msg, header.length, body.length);

        return byte_msg;
    }


}
