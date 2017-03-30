package backup;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface I_RMICalls extends Remote {
	String call(String command, String[] args) throws RemoteException;
}
