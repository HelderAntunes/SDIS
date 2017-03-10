        
import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
        
public class Server implements Hello {
	
    HashMap<Integer, String> db = new HashMap<Integer, String>();        
    
    public Server() {}

    public String sayHello() {
        return "Hello, world!";
    }
        
    public static void main(String args[]) {
        
        try {
        	//System.setProperty("java.rmi.server.hostname","192.168.32.239");
            Server obj = new Server();
            Hello stub = (Hello) UnicastRemoteObject.exportObject(obj, 0);

            // Bind the remote object's stub in the registry
            Registry registry = LocateRegistry.getRegistry();
            registry.rebind("Hello", stub);

            System.err.println("Server ready");
        } catch (Exception e) {
            System.err.println("Server exception: " + e.toString());
            e.printStackTrace();
        }
    }

	@Override
	public void register(int id, String owner) {
		db.put(id, owner);
	}

	@Override
	public String lookup(int id) {
		return this.db.get(id);
	}
}