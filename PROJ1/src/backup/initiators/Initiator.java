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
			System.out.println("sdfdf");
			ArrayList<byte[]> fileSplitted = Utils.splitFile(file);
			System.out.println("sdfdf");
			for (int i = 0; i < fileSplitted.size(); i++) {
				new Thread(new BackupInit(peer, pathFile, i, repDeg, fileSplitted.get(i))).start();
				System.out.println("sdfdf");
			}
			
		}
		else if (command.equals("deletefile")) {
			// args = [pathFile]
			String pathFile = args[0];
			new Thread(new DeleteInit(peer, pathFile)).start();
		}
		
    }

	@Override
	public void run() {
		
	}

}
