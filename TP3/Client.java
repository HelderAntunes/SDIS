import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

public class Client {

	public static void main(String[] args) throws NumberFormatException, UnknownHostException, IOException {
		
		Socket clientSocket = new Socket("localhost", 2000);
		PrintWriter outToServer = new PrintWriter(clientSocket.getOutputStream(), true);
		BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
		
		outToServer.println("Hello");
		System.out.println("FROM SERVER: " + inFromServer.readLine());
		
		clientSocket.close();
    }

}
