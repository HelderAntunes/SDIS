import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

public class Client {
	
	/*
	The client program shall be invoked as follows:

	    java Client <host_name> <port_number> <oper> <opnd> *
	    where:

	        <host_name> is the name of the host where the server runs;
	        <port_number> is the port number where the server provides the service ;
	        <oper> is ''register'' or ''lookup'', depending on the operation to invoke;
	        <opnd> * is the list of operands of the specified operation:

	            <plate number> <owner name>, for register;
	            <plate number>, for lookup.

	To observe the operation of your solution, the client shall reveal what it is doing by printing messages with the format used by the server, i.e.:

	    <oper> <opnd> *:: <out>
	    where:
	        <oper> <opnd*> and have the same meaning and format of homonyms arguments from the command line and
	        <out> is the value returned by the operation invoked or ''ERROR" if any error occurs.
	*/
	public static void main(String[] args) throws NumberFormatException, UnknownHostException, IOException {
		
		Socket clientSocket = new Socket("localhost", 2000);
		PrintWriter outToServer = new PrintWriter(clientSocket.getOutputStream(), true);
		BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
		
		outToServer.println("Hello");
		System.out.println("FROM SERVER: " + inFromServer.readLine());
		
		clientSocket.close();
    }

}
