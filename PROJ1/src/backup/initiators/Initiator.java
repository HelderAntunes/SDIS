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
				String fileID = Utils.getFileId(file.getName() + Integer.toString((int)file.lastModified()));
				Peer.nameFileToFileID.put(file.getName(), fileID);
				Peer.fileIDToNameFile.put(fileID, file.getName());
				new Thread(new BackupInit(peer, fileID, i, repDeg, fileSplitted.get(i))).start();
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

			List<File> files = new ArrayList<File>();

			for (String name: nameFiles) {
				File chunkFile = new File(Peer.chunksRestoredDir, name);
				while (!chunkFile.exists()) {
					try {
						Thread.sleep(400);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				files.add(chunkFile);
			}

			try {
				File fileRestored = new File(Peer.filesRestoredDir, file.getName());
				Utils.mergeFiles(files, fileRestored);
			} catch (IOException e) {
				e.printStackTrace();
			}

		}
		else if (command.equals("reclaim")) {
			// args = [sapce(kb)]
			int spaceReclaim_bytes = Integer.parseInt(args[0]) * 1000;
			File[] chunkFiles = Peer.chunksDir.listFiles();
			
			int spaceSaved = 0;
			for (File file : chunkFiles) {
				
				if (spaceSaved >= spaceReclaim_bytes)
					break;
				
				spaceSaved += file.length();
				String fileName = file.getName();
				String fileOfChunk = fileName.substring(0, Utils.SIZE_OF_FILEID);
				int chunkNO = Integer.parseInt(fileName.substring(Utils.SIZE_OF_FILEID, fileName.length()));
	
				new Thread(new ReclaimInit(peer, fileOfChunk, chunkNO)).start();
				file.delete();
			}
			
		}

		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		Peer.printState();

	}

	@Override
	public void run() {

	}

}
