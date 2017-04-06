package backup;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class Utils {
	
	public static final int SIZE_OF_FILEID = 64;
	public static final int MAX_SIZE_CHUNK = 64000;
	public static final String DB_FILE_NAME = "backup_db";
	public static final String CHUNKS_DIR_NAME = "chunks";
	public static final String CHUNKS_RESTORED_DIR_NAME = "chunks_restored";
	public static final String FILES_RESTORED_DIR_NAME = "files_restored";
	public static final int PORT_RMI_REGISTRY = 1051;
	public static final int MAX_SPACE_DISK = 64000 * 1000; // 1000 chunks at most.
	public static final int ATTEMPTS_ON_DELETE = 2;
	public static final int MAX_SLEEP_MS = 400;
	public static final int MAX_NO_THREADS = 5;


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
        	while(true) {
        		int r = bis.read(buffer);
            	byte[] bufferAux = Arrays.copyOfRange(buffer, 0, r);
                res.add(bufferAux);
                if (r < Utils.MAX_SIZE_CHUNK)
                	break;
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
			System.err.println("attr bodyMsg is null (BackupResponse.java)");
		}
		return bodyMsg;
	}
    
    public static void mergeFiles(List<File> files, File into) throws IOException {
        try (BufferedOutputStream mergingStream = new BufferedOutputStream(new FileOutputStream(into))) {
            for (File f : files) {
                Files.copy(f.toPath(), mergingStream);
                f.delete();
            }
        }
    }
    
    public static void myRandomSleep(int maxSleep) {
		try {
			int n = new Random().nextInt(maxSleep+1);
			Thread.sleep(n);
			System.out.println("random = " + n);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
    }
    
}
