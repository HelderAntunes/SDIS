package backup.initiators;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import backup.MetaDataChunk;
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
				try {
					Thread.sleep(200);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
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
			Set<MetaDataChunk> backupDB = Peer.backupDB.keySet();
			for (MetaDataChunk key: backupDB) {
				if (key.fileId.equals(fileID)) {
					new Thread(new RestoreInit(peer, file, key.chunkNo)).start();
					nameFiles.add(key.toString());
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
