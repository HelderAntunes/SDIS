package backup;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface I_RMICalls extends Remote {
	void call(String command, String[] args) throws RemoteException;
}
