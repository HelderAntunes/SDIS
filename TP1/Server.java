import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.ArrayList;

public class Server {
	
	private static final int BUF_LENGTH = 256;
	public ArrayList<Vehicle> vehicles = new ArrayList<Vehicle>();
	
	public static void main(String[] args) throws IOException {
		
		Server server = new Server();
		
		String portString = args[0];
        System.out.println("Server started at port " + portString + ".");
        
        // create socket in port specified
        DatagramSocket socket = new DatagramSocket(Integer.parseInt(portString));
        
        // create the packet that receives the messages of clients
        byte[] buf = new byte[Server.BUF_LENGTH];
        DatagramPacket packet = new DatagramPacket(buf, buf.length);
        String msg = null;
        
        int i = 10;
        while(i > 0) {
        	i--;
        	
        	// receive a message from a client
        	socket.receive(packet);
        	
        	// prepare a message to send to the client
        	String received = new String(packet.getData(), 0, packet.getLength());
        	received.trim();
        	
        	String[] stringDivided = received.split(":");
        	String oper = stringDivided[0];
        	
        	if (oper.equals("REGISTER")) {
        		String plate = stringDivided[1];
        		String owner = stringDivided[2];
        		server.vehicles.add(new Vehicle(owner, plate));
        		msg = oper + " plate: " + plate + " " + "owner: " + owner;
        	}
        	else if (oper.equals("LOOKUP")) {
        		String plate = stringDivided[1];
        		for (Vehicle v: server.vehicles) {
        			if (v.plate.equals(plate)) {
                		msg = oper + " owner: " + v.owner;
                		break;
        			}
        		}
        		
        	}
        	else {
        		System.out.println("ERROR");
        		return;
        	}
        	
        	// prepare packet and set the destination of packet
    		buf = msg.getBytes();
    		packet = new DatagramPacket(buf, buf.length, packet.getAddress(), packet.getPort());
    		
        	// send the message to the respective client
    		socket.send(packet);
    		
        	System.out.println("Command: " + msg);

        }
        
        socket.close();
               
    }
	
}
