package backup.initiators;

import java.io.File;
import java.util.ArrayList;

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
			// args = [pathFile, chunkNO]
			String pathFile = args[0];
			int chunkNO = Integer.parseInt(args[1]);
			File file = new File(pathFile);
			new Thread(new RestoreInit(peer, file, chunkNO)).start();
		}
		
    }

	@Override
	public void run() {
		
	}

}
