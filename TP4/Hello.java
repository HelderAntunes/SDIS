import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Hello extends Remote {
	String sayHello() throws RemoteException;
	void register(int id, String owner) throws RemoteException;
	String lookup(int id) throws RemoteException;
}
