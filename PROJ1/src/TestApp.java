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

        String msgBackup = TestApp.getMsgBackup(args);

        return true;

    }

    /**
     * Message format:
     * "PUTCHUNK <Version> <SenderId> <FileId> <ChunkNo> <ReplicationDeg> <CRLF><CRLF><Body>"
     * @param args
     * @return msg
     */
    public static String getMsgBackup(String[] args) {
        String fileID = TestApp.getFileId(args[2]);
        String senderID = "1";
        String version = "1.0";
        String replication = args[3];
        String chunkNo = "1";
        String msg = "PUTCHUNK " + version + " " + senderID + " " + fileID + " " + chunkNo + " " + replication + " \r\n\r\n";
        return msg;
    }

    /**
     * Obtain file id by using the SHA256 cryptographic hash function.
     * TODO: Support versions of files
     *
     * @param file original file name
     * @return fileID 64 ASCII character sequence
     */
    public static String getFileId(String file) {

        MessageDigest digest = null;
        try {
            digest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        byte[] hash = digest.digest(file.getBytes(StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();
        for (int i = hash.length-1; i >= 0; i--) {
            sb.append(String.format("%02X", hash[i]));
        }

        return sb.toString();
    }

}
