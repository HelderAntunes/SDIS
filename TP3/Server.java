import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {

	/*
	The server program shall be invoked as follows:

	    java Server <srvc_port>
	    where:

	        <srvc_port> is the port number where the server provides the service

	The server program should have an infinite loop in which the server waits for a request from a client, processes the request and sends the respective response to the client.

	To observe the operation of your solution, the server shall reveal what it is doing by printing messages with, e.g., the following format:

	    <oper> <opnd> * :: <out>
	    where:

	        <oper> should be "register'' or ''lookup'', according to the operation invoked;
	        <opnd> * is the list of operands received in the request for the operation;
	        <out> is the value returned by the operation, if any.
	 */
	public static void main(String[] args) throws IOException {
		
		Server server = new Server();
		
		@SuppressWarnings("resource")
		ServerSocket srvSocket = new ServerSocket(Integer.parseInt(args[0]));
		while(true) {
    		try {
				Socket echoSocket = srvSocket.accept();
				DataThread dataThread = server. new DataThread(echoSocket);
				dataThread.start();
			
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	}		
	}
	
	class DataThread extends Thread {
		
		Socket socket = null;
		
		DataThread(Socket socket) {
            this.socket = socket;
        }

        public void run() {
        	try {
        		PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
        		BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        		out.println(in.readLine());
        		
        		out.close();
        		in.close();
				socket.close();
        	
			} catch (IOException e) {
				e.printStackTrace();
			}
        }
    }

}
