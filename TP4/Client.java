import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class Client {

    private Client() {}

    public static void main(String[] args) throws RemoteException, NotBoundException {
		Registry registry = LocateRegistry.getRegistry();
		Hello stub = (Hello) registry.lookup("Hello");
    	
		if (args.length == 2) {
    		stub.register(Integer.parseInt(args[0]), args[1]);
    		System.out.println("Register: " + args[0] + " " + args[1]);
    	}
    	else {
    		System.out.println("Lookup responde: " + stub.lookup(Integer.parseInt(args[0])));
    	}

 
    }
    
}