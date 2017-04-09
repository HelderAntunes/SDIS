package backup;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Arrays;

public class TestApp {
	
	// args: <peer_ap> <sub_protocol> <opnd_1> <opnd_2> 
	public static void main(String[] args) {
		
		if (args.length < 2) {
			System.out.println("java TestApp <peer_ap> <sub_protocol> <opnd_1> <opnd_2>");
		}
		
		String peerAp = args[0];
		String subProtocol = args[1];
		String[] argProtocol = Arrays.copyOfRange(args, 2, args.length);
		
		try {
			Registry registry = LocateRegistry.getRegistry(Utils.PORT_RMI_REGISTRY);
			I_RMICalls stub = (I_RMICalls) registry.lookup(peerAp);
			
			String response = stub.call(subProtocol, argProtocol);
			System.out.println(response);
		} catch (RemoteException | NotBoundException e) {
			e.printStackTrace();
		}
		

	}

}
