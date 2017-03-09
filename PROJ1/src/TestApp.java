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

        String speech = "Four score     and seven years    ago  \r\n\r\n hjghg";
        String[] result = speech.split("\\s+");
        System.out.println(result.length);
        for (int x=0; x<result.length; x++) {
            System.out.println(result[x]);
        }
        /*
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

        }*/

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
