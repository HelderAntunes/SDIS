import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {

	public static void main(String[] args) throws IOException {
		
		Server server = new Server();
		
		@SuppressWarnings("resource")
		ServerSocket srvSocket = new ServerSocket(Integer.parseInt(args[0]));
		while(true) {
            try {
                Socket echoSocket = srvSocket.accept();
                DataThread dataThread = server.new DataThread(echoSocket);


                dataThread.start();

            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
	}
	
	public class DataThread extends Thread {
		
		Socket socket = null;
		
		DataThread(Socket socket) {
            this.socket = socket;
        }

        public void run() {
        	try {
        		PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
        		BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        		String s = in.readLine();
        		out.println(s);
        		System.out.println(s);
        		
        		out.close();
        		in.close();
				socket.close();
        	
			} catch (IOException e) {
				e.printStackTrace();
			}
        }
    }

}
