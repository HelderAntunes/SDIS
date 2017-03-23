package backup;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;

public class Utils {
	
	public static final int SIZE_OF_FILEID = 64;
	public static final int MAX_SIZE_CHUNK = 64000;
	public static final String DB_FILE_NAME = "backup_db";
	public static final String CHUNKS_DIR_NAME = "chunks";
	public static final String CHUNKS_RESTORED_DIR_NAME = "chunks_restored";

    /**
     * Obtain file id by using the SHA256 cryptographic hash function.
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
    
    /**
     * Split a file in array of bytes.
     * @param f
     * @return res array of arrays of bytes
     */
    public static ArrayList<byte[]> splitFile(File f) {
        
    	ArrayList<byte[]> res = new ArrayList<byte[]>();
    	        
        byte[] buffer = new byte[Utils.MAX_SIZE_CHUNK];

        try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(f))) {
            while (bis.read(buffer) > 0) {
            	res.add(buffer.clone());
            }
        } catch (IOException e) {
			e.printStackTrace();
		}
        
        if (res.get(res.size()-1).length == Utils.MAX_SIZE_CHUNK) {
        	res.add(new byte[0]);
        }
        
		return res;
    }
    
    public static byte[] getBodyOfMsg(byte[] msgRcvd) {
		byte[] bodyMsg = null;
		for (int i = 0; i < msgRcvd.length-3; i++) {
			if (msgRcvd[i] == (byte)'\r' && msgRcvd[i+1] == (byte)'\n' && 
					msgRcvd[i+2] == (byte)'\r' && msgRcvd[i+3] == (byte)'\n') {
				bodyMsg = Arrays.copyOfRange(msgRcvd, i+4, msgRcvd.length);
			}
		}
		if (bodyMsg == null) {
			System.err.println("attr bodyMsg is null (BackupResponse.java 76)");
		}
		return bodyMsg;
	}
    
}
