import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.UnknownHostException;

public class SSLClient {

    public static void main(String[] args) throws NumberFormatException, UnknownHostException, IOException {

        int port = 2000;

        // set keys
        System.setProperty("javax.net.ssl.keyStore", "/home/helder/IdeaProjects/SDIS/TP5/client.keys");
        System.setProperty("javax.net.ssl.keyStorePassword", "123456");
        System.setProperty("javax.net.ssl.trustStore","/home/helder/IdeaProjects/SDIS/TP5/truststore");
        System.setProperty("javax.net.ssl.trustStorePassword","123456");

        // create socket
        SSLSocketFactory sslFact = (SSLSocketFactory) SSLSocketFactory.getDefault();
        SSLSocket s = (SSLSocket)sslFact.createSocket("localhost", port);

        // send msg
        PrintWriter outToServer = new PrintWriter(s.getOutputStream(), true);
        BufferedReader inFromServer = new BufferedReader(new InputStreamReader(s.getInputStream()));
        outToServer.println("Hello");
        System.out.println("FROM SERVER: " + inFromServer.readLine());

        s.close();
    }

}
