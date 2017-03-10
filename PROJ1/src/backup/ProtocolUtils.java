package backup;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by helder on 07-03-2017.
 */
public class ProtocolUtils {

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
