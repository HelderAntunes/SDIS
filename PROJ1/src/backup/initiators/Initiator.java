package backup.initiators;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import backup.Peer;
import backup.Utils;

public class Initiator implements Runnable {
	
	public Initiator(Peer peer, String command, String[] args) {
		
		if (command.equals("putchunk")) {
			// args = [pathFile, repDeg]
			String pathFile = args[0];
			int repDeg = Integer.parseInt(args[1]);
			
			File file = new File(pathFile);
			
			ArrayList<byte[]> fileSplitted = Utils.splitFile(file);
			for (int i = 0; i < fileSplitted.size(); i++) {
				new Thread(new BackupInit(peer, file, i, repDeg, fileSplitted.get(i))).start();
			}
			
		}
		else if (command.equals("deletefile")) {
			// args = [pathFile]
			String pathFile = args[0];
			File file = new File(pathFile);
			new Thread(new DeleteInit(peer, file)).start();
		}
		else if (command.equals("getfile")) {
			// args = [pathFile]
			String pathFile = args[0];
			File file = new File(pathFile);
			String fileID = Utils.getFileId(file.getName() + Integer.toString((int)file.lastModified()));
			ArrayList<String> nameFiles = new ArrayList<String>();
			Set<String> backupDB = Peer.backupDB.keySet();
			for (String key: backupDB) {
				String fileIDKey = key.substring(0, Utils.SIZE_OF_FILEID);
				if (fileIDKey.equals(fileID)) {
					int chunkNO = Integer.parseInt(key.substring(Utils.SIZE_OF_FILEID, key.length()));
					new Thread(new RestoreInit(peer, file, chunkNO)).start();
					nameFiles.add(key);
					try {
						Thread.sleep(200);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
			Collections.sort(nameFiles);
			
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			System.out.println("dsdsd");
			
			List<File> files = new ArrayList<File>();
			
			for (String name: nameFiles) {
				File chunkFile = new File(Peer.chunksRestoredDir, name);
				files.add(chunkFile);
			}
			
			try {
				File fileRestored = new File(Peer.filesRestoredDir, file.getName());
				Utils.mergeFiles(files, fileRestored);
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		}
		
    }

	@Override
	public void run() {
		
	}

}
