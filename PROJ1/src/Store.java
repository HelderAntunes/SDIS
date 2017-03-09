import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Random;

/**
 * Created by helder on 09-03-2017.
 */
public class Store implements Runnable {

    private ServerPeer server;
    private MulticastSocket mdb;
    private MulticastSocket mc;
    private ArrayList<Chunk> chunksStored;

    private volatile boolean execute;

    public Store(ServerPeer server) throws IOException {

        this.server = server;
        this.chunksStored = new ArrayList<Chunk>();
        this.mdb = new MulticastSocket(server.getMdbPort());
        this.mc = new MulticastSocket(server.getMcPort());

        this.mdb.joinGroup(InetAddress.getByName(this.server.getMdbIP()));
        this.execute = true;
    }

    @Override
    public void run() {

        try {

            while (this.execute) {
                this.processRequests();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void processRequests() throws IOException {

        byte[] buf = new byte[1024];
        DatagramPacket msgRcvd = new DatagramPacket(buf, buf.length);
        mdb.receive(msgRcvd);

        String[] result = new String(buf, 0, buf.length).split("\\s+");
        // PUTCHUNK <Version> <SenderId> <FileId> <ChunkNo> <ReplicationDeg> <CRLF><CRLF><Body>

        if (result.length < 7 ||
            !result[0].equals("PUTCHUNK") ||
            result[2].equals(this.server.getServerID())) {
            return;
        }

        if (this.chunkStored(new Chunk(result[3], Integer.parseInt(result[4])))) {
            return;
        }


        Random rand = new Random();
        int  n = rand.nextInt(400) + 1;




    }

    private boolean chunkStored(Chunk chunk) {
        for (Chunk c: this.chunksStored) {
            if (c.equals(chunk)) {
                return true;
            }
        }
        return false;
    }

}
