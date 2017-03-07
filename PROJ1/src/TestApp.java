import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by helder on 04-03-2017.
 */
public class TestApp {

    /**
     * Invocation:
     * java TestApp <peer_ap> <sub_protocol> <opnd_1> <opnd_2>
     */
    public static void main(String[] args) {
        TestApp testApp = new TestApp();

        String sub_protocol = args[1];

        if (sub_protocol.equals("BACKUP")) {
            testApp.backup(args);
        }
        else if (sub_protocol.equals("DELETE")){

        }
        else if (sub_protocol.equals("RESTORE")) {

        }
        else if (sub_protocol.equals("RECLAIM")) {

        }

    }

    public TestApp() {

    }

    /**
     *
     * @param args 1923 BACKUP file replication
     * @return
     */
    public static boolean backup(String[] args) {

        return true;

    }

}
