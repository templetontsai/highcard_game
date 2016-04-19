package unimelb.distributed_algo_game.network;

import java.util.ArrayList;
import java.util.List;

public final class PlayerClientManager {
	private List<PlayerClientThread> playeClientList = null;

	public PlayerClientManager(int playerClientNum) {
		playeClientList = new ArrayList<PlayerClientThread>(playerClientNum);
	}

	public void addClient(PlayerClientThread clientThread) {
		playeClientList.add(clientThread);
	}

	public void removeClient(PlayerClientThread clientThread) {
		playeClientList.remove(clientThread);
	}

	public void notifyAllClients() {
		for (PlayerClientThread t : playeClientList) {
			System.out.println("Notifying");
			t.update();
		}
	}

}
