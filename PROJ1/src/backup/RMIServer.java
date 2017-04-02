package backup;

import java.io.File;
import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import backup.initiators.BackupInit;
import backup.initiators.DeleteInit;
import backup.initiators.ReclaimInit;
import backup.initiators.RestoreInit;

public class RMIServer implements Runnable, I_RMICalls {
	
	private String objRemoteName;
	private Peer peer;

	public RMIServer(Peer peer) {
		this.objRemoteName = peer.getServerAccessPoint();
		this.peer = peer;
	}

	@Override
	public void run() {
		try {
            RMIServer obj = new RMIServer(this.peer);
            I_RMICalls stub = (I_RMICalls) UnicastRemoteObject.exportObject(obj, 0);

            // Bind the remote object's stub in the registry
            Registry registry = LocateRegistry.getRegistry(Utils.PORT_RMI_REGISTRY);
            registry.rebind(this.objRemoteName, stub);

        } catch (Exception e) {
            System.err.println("Server exception: " + e.toString());
            e.printStackTrace();
        }
	}

	@Override
	public String call(String command, String[] args) throws RemoteException {
		
		if (command.equals("BACKUP")) {
			// args = [pathFile, repDeg]
			String pathFile = args[0];
			int repDeg = Integer.parseInt(args[1]);
			
			File file = new File(pathFile);
			String fileID = Utils.getFileId(file.getName() + Integer.toString((int)file.lastModified()));
			
			Peer.myFiles.add(new MetaDataFile(file.getName(), file.getAbsolutePath(), fileID, repDeg));

			ArrayList<byte[]> fileSplitted = Utils.splitFile(file);
			for (int i = 0; i < fileSplitted.size(); i++) {
				new Thread(new BackupInit(peer, fileID, i, repDeg, fileSplitted.get(i))).start();
			}
			
			MetaDataChunk.last_id = 0;

		}
		else if (command.equals("DELETE")) {
			// args = [pathFile]
			String pathFile = args[0];
			File file = new File(pathFile);
			new Thread(new DeleteInit(peer, file)).start();
		}
		else if (command.equals("RESTORE")) {
			
			// args = [pathFile]
			String pathFile = args[0];
			File file = new File(pathFile);
			String fileID = Utils.getFileId(file.getName() + Integer.toString((int)file.lastModified()));
			ArrayList<String> nameFiles = new ArrayList<String>();
			
			// request all chunks
			Set<MetaDataChunk> backupDB = Peer.backupDB.keySet();
			for (MetaDataChunk key: backupDB) {
				if (key.fileId.equals(fileID)) {
					new Thread(new RestoreInit(peer, file, key.chunkNo)).start();
					nameFiles.add(key.toString());
				}
			}
			Collections.sort(nameFiles);

			List<File> files = new ArrayList<File>();
			
			// collects all chunks received
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
			
			// merge the chunks in a unique file
			try {
				File fileRestored = new File(Peer.filesRestoredDir, file.getName());
				Utils.mergeFiles(files, fileRestored);
			} catch (IOException e) {
				e.printStackTrace();
			}

		}
		else if (command.equals("RECLAIM")) {
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
		else if (command.equals("STATE")) {
			return Peer.printState();
		}
		
		return "Command sent with success.";
	}

}
