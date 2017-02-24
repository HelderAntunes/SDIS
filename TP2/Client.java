import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;


public class Client {
	/*
    java client <mcast_addr> <mcast_port> <oper> <opnd> *
   where:

       <mcast_addr> is the IP address of the multicast group used by the server to advertise its service;
       <mcast_port> is the port number of the multicast group used by the server to advertise its service;
       <oper> is ''register'' or ''lookup'', depending on the operation to invoke;
       <opnd> * is the list of operands of the specified operation:

           <plate number> <owner name>, for register;
           <plate number>, for lookup.
	 */
	public static void main(String[] args) throws IOException {

		String mcast_addr = args[0];// "224.0.0.3"
		int mcast_port = Integer.parseInt(args[1]); // 2000
		//String oper = args[2];
		//String opnd = args[3];

		InetAddress address = InetAddress.getByName(mcast_addr);

		byte[] buf = new byte[256];

		try (MulticastSocket clientSocket = new MulticastSocket(mcast_port)){
			//Joint the Multicast group.
			clientSocket.joinGroup(address);

			while (true) {
				// Receive the information and print it.
				DatagramPacket msgPacket = new DatagramPacket(buf, buf.length);
				clientSocket.receive(msgPacket);

				String msg = new String(buf, 0, buf.length);
				System.out.println("Socket 1 received msg: " + msg);
			}
		} catch (IOException ex) {
			ex.printStackTrace();
		}

	}

}
