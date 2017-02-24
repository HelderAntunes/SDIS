import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class Server {
	
    /*java Server <srvc_port> <mcast_addr> <mcast_port>
   where:

       <srvc_port> is the port number where the server provides the service
       <mcast_addr> is the IP address of the multicast group used by the server to advertise its service.
       <mcast_port> is the multicast group port number used by the server to advertise its service.   
	*/
	  
	public static void main(String[] args) throws IOException, InterruptedException {
		int serverPort = Integer.parseInt(args[0]); // 2001
		String mcast_addr = args[1]; // "224.0.0.3"
		int mcast_port = Integer.parseInt(args[2]); // 2000
		
		
		InetAddress addr = InetAddress.getByName(mcast_addr);
		
		try (DatagramSocket serverSocket = new DatagramSocket(serverPort)) {
            for (int i = 0; i < 5; i++) {
                String msg = "Sent message no " + i;

               
                DatagramPacket msgPacket = new DatagramPacket(msg.getBytes(),
                msg.getBytes().length, addr, mcast_port);
                
                serverSocket.send(msgPacket);
     
                System.out.println("Server sent packet with msg: " + msg);
                Thread.sleep(500);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
	
}
