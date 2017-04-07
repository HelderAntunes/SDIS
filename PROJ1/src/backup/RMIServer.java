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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import backup.initiators.BackupInit;
import backup.initiators.DeleteInit;
import backup.initiators.DeleteInitEnh;
import backup.initiators.ReclaimInit;
import backup.initiators.RestoreInit;
import backup.initiators.RestoreInitEnh;

public class RMIServer implements Runnable, I_RMICalls {

	private String objRemoteName;
	private Peer peer;
	private ExecutorService executor = Executors.newFixedThreadPool(Utils.MAX_NO_THREADS);

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

			// arguments = [pathFile, repDeg]
			String pathFile = args[0];
			int repDeg = Integer.parseInt(args[1]);

			File file = new File(pathFile);
			String fileID = Utils.getFileId(file.getName() + Integer.toString((int)file.lastModified()));

			Peer.myFiles.add(new MetaDataFile(file.getName(), file.getAbsolutePath(), fileID, repDeg));

			ArrayList<byte[]> fileSplitted = Utils.splitFile(file);
			for (int i = 0; i < fileSplitted.size(); i++) {
				this.executor.execute(new Thread(new BackupInit(peer, fileID, i, repDeg, fileSplitted.get(i))));
			}

			MetaDataChunk.last_id = 0;

		}
		else if (command.equals("DELETE")) {

			// arguments = [pathFile]
			String pathFile = args[0];
			File file = new File(pathFile);
			this.executor.execute(new Thread(new DeleteInit(peer, file)));
		}
		else if (command.equals("RESTORE") || command.equals("RESTOREENH")) {

			// arguments = [pathFile]
			String pathFile = args[0];
			File file = new File(pathFile);
			String fileID = Utils.getFileId(file.getName() + Integer.toString((int)file.lastModified()));
			ArrayList<String> nameFiles = new ArrayList<String>();

			boolean found = false;
			for (int i = 0; i < Peer.myFiles.size(); i++) {
				if (Peer.myFiles.get(i).name.equals(file.getName())) {
					found = true;
					break;
				}
			}
			if (!found) {
				return "Error: File not found.";
			}
			// request all chunks
			Set<MetaDataChunk> backupDB = Peer.backupDB.keySet();
			for (MetaDataChunk key: backupDB) {
				if (key.fileId.equals(fileID)) {
					if (this.peer.getProtocolVersion().equals("1.0")) 
						this.executor.execute(new Thread(new RestoreInit(peer, file, key.chunkNo)));
					else
						this.executor.execute(new Thread(new RestoreInitEnh(peer, file, key.chunkNo)));
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

			// arguments = [space(KByte)]
			int spaceReclaim_bytes = Integer.parseInt(args[0]) * 1000;

			if (Peer.maxSpaceDisk_bytes < spaceReclaim_bytes) {
				return "Error: Peer max space in disck = " + Peer.maxSpaceDisk_bytes + " bytes < " + 
						spaceReclaim_bytes + ". Try reduce the space to reclaim.";
			}

			Peer.maxSpaceDisk_bytes -= spaceReclaim_bytes;
			if (Peer.spaceUsed_bytes < Peer.maxSpaceDisk_bytes) {
				return "Command sent successfully. Max space of peer = " + Peer.maxSpaceDisk_bytes/1000 + " KByte.";
			}

			this.peer.updateRepOfChunksSaved();
			ArrayList<MetaDataChunk> chunksSavedSorted = new ArrayList<MetaDataChunk>(Peer.chunksSaved);
			Collections.sort(chunksSavedSorted, new LowDiffRepSorter());

			int spaceSaved = 0;
			for (int i = 0; i < chunksSavedSorted.size(); i++) {

				if (spaceSaved >= spaceReclaim_bytes)
					break;

				MetaDataChunk chunk = chunksSavedSorted.get(i);
				String fileName = chunk.toString();
				File file = new File(Peer.chunksDir, fileName);
				Peer.spaceUsed_bytes -= file.length();
				spaceSaved += file.length();
				String fileOfChunk = fileName.substring(0, Utils.SIZE_OF_FILEID);
				int chunkNO = Integer.parseInt(fileName.substring(Utils.SIZE_OF_FILEID, fileName.length()));
				file.delete();

				this.executor.execute(new Thread(new ReclaimInit(peer, fileOfChunk, chunkNO)));

				if (Peer.spaceUsed_bytes < Peer.maxSpaceDisk_bytes) {
					return "Command sent successfully. Max space of peer = " + Peer.maxSpaceDisk_bytes/1000 + " KByte.";
				}

			}
		}
		else if (command.equals("STATE")) {

			return this.peer.printState();
		}
		else if (command.equals("DELETEENH")) {

			// arguments = [pathFile]
			String pathFile = args[0];
			File file = new File(pathFile);
			this.executor.execute(new Thread(new DeleteInitEnh(peer, file)));

		}
		else {
			return "Error: command not found.";
		}

		return "Command sent successfully.";
	}

}
