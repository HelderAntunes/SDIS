import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;

public class Server {
	
	private static final int BUF_LENGTH = 256;
	public ArrayList<Vehicle> vehicles = new ArrayList<Vehicle>();
	
	public static void main(String[] args) throws IOException {
		
		Server server = new Server();
		
		String portString = args[0];
        System.out.println("Server started at port " + portString + ".");
        
        int portServer = Integer.parseInt(portString);
        DatagramSocket socket = new DatagramSocket(portServer);
        
        byte[] buf = new byte[Server.BUF_LENGTH];
        DatagramPacket packet = new DatagramPacket(buf, buf.length);
        String msg = null;
        
        int i = 10;
        while(i > 0) {
        	i--;
        	socket.receive(packet);
        	
        	String received = new String(packet.getData(), 0, packet.getLength());
        	received.trim();
        	
        	String[] stringDivided = received.split(":");
        	String oper = stringDivided[0];
        	
        	if (oper.equals("REGISTER")) {
        		String plate = stringDivided[1];
        		String owner = stringDivided[2];
        		Vehicle v = new Vehicle(owner, plate);
        		server.vehicles.add(v);
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
        	}
        	System.out.println(msg);
        
    		InetAddress address = packet.getAddress();
    		int portClient = packet.getPort();
    		buf = msg.getBytes();
    		packet = new DatagramPacket(buf, buf.length, address, portClient);
    		packet.setData(buf);
    		socket.send(packet);
    		
        }
        
        socket.close();
               
    }
	
}
