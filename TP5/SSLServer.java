import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;

public class SSLServer {

    public static void main(String[] args) throws IOException {

        int port = 2000;

        // set keys
        System.setProperty("javax.net.ssl.keyStore", "/home/helder/IdeaProjects/SDIS/TP5/server.keys");
        System.setProperty("javax.net.ssl.keyStorePassword", "123456");
        System.setProperty("javax.net.ssl.trustStore","/home/helder/IdeaProjects/SDIS/TP5/truststore");
        System.setProperty("javax.net.ssl.trustStorePassword","123456");

        // create server socket
        SSLServerSocketFactory sslSrvFact = (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();
        SSLServerSocket s = (SSLServerSocket)sslSrvFact.createServerSocket(port);
        s.setNeedClientAuth(true);

        while(true) {
            try {
                // get client socket
                SSLSocket c = (SSLSocket)s.accept();
                // process request
                SSLServer server = new SSLServer();
                DataThread dataThread = server.new DataThread(c);
                dataThread.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public class DataThread extends Thread {

        SSLSocket socket = null;

        DataThread(SSLSocket socket) {
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
