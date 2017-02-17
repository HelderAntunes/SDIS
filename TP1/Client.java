import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class Client {
	
	public static void main(String[] args) throws IOException {
			
		String hostnameOfServer = args[0]; // "localhost"
		int portOfServer = Integer.parseInt(args[1]);
		String oper = args[2];
		
		// prepare the message to send to the server
		String msg = null;
		if (oper.equals("REGISTER")) 
			msg = oper + ":" + args[3] + ":" + args[4];
		else if (oper.equals("LOOKUP"))
			msg = oper + ":" + args[3];
		else {
			System.out.println("ERROR: <oper> invalid.");
			return;
		}
		
		// create socket
		DatagramSocket socket = new DatagramSocket();
		
		// prepare packet with the message and destination of server
		byte[] buf = msg.getBytes();
		InetAddress address = InetAddress.getByName(hostnameOfServer);
		DatagramPacket packetToSend = new DatagramPacket(buf, buf.length, address, portOfServer);
		
		// send request
		socket.send(packetToSend);
		
		// create a packet that will contain the response
		buf = new byte[255];
		DatagramPacket packetReceived = new DatagramPacket(buf, buf.length);
		
		// receive response
		socket.receive(packetReceived);
		
    	String received = new String(packetReceived.getData(), 0, packetReceived.getLength());
    	received.trim();
    	System.out.println("Message received: " + received);

    	socket.close();
    }
	
}
