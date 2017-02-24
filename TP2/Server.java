import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.MulticastSocket;
import java.net.InetAddress;
import java.util.Timer;
import java.util.TimerTask;

public class Server {

	public Server(String mcast_addr, int mcast_port, int serverPort) {
		new Timer().schedule(new RemindTask(mcast_addr, mcast_port, serverPort), 0, 1000);
	}

	public static void main(String[] args) {
        // Invocation: java Server <srvc_port> <mcast_addr> <mcast_port>
		Server server = new Server(args[1], Integer.parseInt(args[2]), Integer.parseInt(args[0]));
	}

	class RemindTask extends TimerTask {

        private int mcast_port;
        private MulticastSocket serverSocket;
        private InetAddress mcast_addr;

		public RemindTask(String mcast_addr, int mcast_port, int serverPort) {
            try {
                this.mcast_port = mcast_port;
                this.serverSocket = new MulticastSocket(serverPort);
                this.mcast_addr = InetAddress.getByName(mcast_addr);
            }
            catch (IOException ex) {
                ex.printStackTrace();
            }
		}

        public void run() {
    		try {
                String msg = "message";
                serverSocket.send(new DatagramPacket(msg.getBytes(), msg.getBytes().length, this.mcast_addr, this.mcast_port));
                System.out.println("Server sent packet with msg: " + msg);
            } catch (IOException ex) {
                ex.printStackTrace();
        	}
        }
	}

}
