import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class Client {
	
	public static void main(String[] args) throws IOException {
			
		String hostname = args[0];
		int portNumber = Integer.parseInt(args[1]);
		String oper = args[2];
		String msg = null;
		
		if (oper.equals("REGISTER")) {
			msg = oper + ":" + args[3] + ":" + args[4];
		}
		else if (oper.equals("LOOKUP")) {
			msg = oper + ":" + args[3];
		}
		else {
			System.out.println("ERROR: <oper> invalid.");
			return;
		}
		
		InetAddress address = InetAddress.getByName(hostname);
		DatagramSocket socket = new DatagramSocket();
		
		byte[] buf = msg.getBytes();
		DatagramPacket packet = new DatagramPacket(buf, buf.length, address, portNumber);
		socket.send(packet);
		
		DatagramPacket packetReceived = new DatagramPacket(buf, buf.length);
		socket.receive(packetReceived);
    	String received = new String(packetReceived.getData(), 0, packetReceived.getLength());
    	received.trim();
    	System.out.println("Message received: " + received);

    	socket.close();
    }
	
}
