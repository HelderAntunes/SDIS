import java.net.MulticastSocket;

/**
 * Created by helder on 09-03-2017.
 */
public class Dispatcher implements Runnable {

    private MulticastSocket mdb;
    private MulticastSocket mc;
    ServerPeer server;

    public void Dispatcher (ServerPeer server) {

        this.server = server;


    }

    @Override
    public void run() {

    }
}
