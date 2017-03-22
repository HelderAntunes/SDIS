package backup.listeners;

import java.util.concurrent.CopyOnWriteArrayList;

import backup.responseHandlers.RestoreResponse;

public class RecoveryChannelListener implements Runnable {

	
	CopyOnWriteArrayList<RestoreResponse> restoreResponseHandlers;
	
	public RecoveryChannelListener() {
		this.restoreResponseHandlers = new CopyOnWriteArrayList<RestoreResponse>();
	}
	
	@Override
	public void run() {
		
	}
	
	public void addRestoreResponse(RestoreResponse rh) {
		this.restoreResponseHandlers.add(rh);
	}
	
	public void remRestoreResponse(RestoreResponse rh) {
		this.restoreResponseHandlers.remove(rh);
	}
	
}
