import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Scanner;

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
    private ArrayList<String> peersThatRespond;

    public PutChunk(ServerPeer server, String fileId, int chunckNo, int repDeg, byte[] body) {

        this.serverPeer = server;
        this.fileId = fileId;
        this.chunckNo = chunckNo;
        this.repDeg = repDeg;
        this.body = body;
        this.msg = this.getMsg();
        this.peersThatRespond = new ArrayList<String>();

        try {
            mdb = new MulticastSocket(server.getMdbPort());
            mc = new MulticastSocket(server.getMcPort());
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void run() {

        try {

            mc.joinGroup(InetAddress.getByName(serverPeer.getMcIP()));
            InetAddress addr = InetAddress.getByName(serverPeer.getMdbIP());
            mdb.send(new DatagramPacket(msg, msg.length, addr, serverPeer.getMdbPort()));

            int attempts = 0;
            int currRep = 0;
            int timeOut = 1000;
            int currTime = 0;

            while (attempts < 5 && currRep < repDeg) {

                try {

                    byte[] buf = new byte[1024];
                    DatagramPacket msgRcvd = new DatagramPacket(buf, buf.length);

                    long before = System.currentTimeMillis();
                    mc.receive(msgRcvd);
                    long after = System.currentTimeMillis();
                    currTime += after - before;
                    mc.setSoTimeout(timeOut-currTime);

                    String[] result = new String(buf, 0, buf.length).split("\\s+");
                    if (this.newConfirmationReceived(result)) {
                        currRep++;
                        peersThatRespond.add(result[2]);
                    }

                }
                catch (SocketTimeoutException e) {
                    timeOut *= 2;
                    currTime = 0;
                    attempts++;
                    mc.setSoTimeout(timeOut);
                    mdb.send(new DatagramPacket(msg, msg.length, addr, serverPeer.getMdbPort()));
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    // STORED <Version> <SenderId> <FileId> <ChunkNo> <CRLF><CRLF>
    private boolean newConfirmationReceived(String[] confirmation) {
        if (confirmation.length == 5 &&
            confirmation[0].equals("STORED") &&
            !confirmation[2].equals(serverPeer.getServerID()) &&
            confirmation[3].equals(fileId) &&
            confirmation[4].equals(Integer.toString(chunckNo)) &&
            !peerResponded(confirmation[2])) {
            return true;
        }
        return false;
    }

    private boolean peerResponded(String peer) {
        for (String peerThatRespond: peersThatRespond) {
            if (peerThatRespond.equals(peer)) {
                return true;
            }
        }
        return false;
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
