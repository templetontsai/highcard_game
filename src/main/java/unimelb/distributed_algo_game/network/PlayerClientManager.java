/*
 * 
 */
package unimelb.distributed_algo_game.network;

import java.util.ArrayList;
import java.util.List;

// TODO: Auto-generated Javadoc
/**
 * The Class PlayerClientManager.
 */
public final class PlayerClientManager {

	/** The playe client list. */
	private List<PlayerClientThread> playeClientList = null;

	/**
	 * Instantiates a new player client manager.
	 *
	 * @param playerClientNum
	 *            the player client num
	 */
	public PlayerClientManager(int playerClientNum) {
		playeClientList = new ArrayList<PlayerClientThread>(playerClientNum);
	}

	/**
	 * Adds the client.
	 *
	 * @param clientThread
	 *            the client thread
	 */
	public void addClient(PlayerClientThread clientThread) {
		playeClientList.add(clientThread);
	}

	/**
	 * Removes the client.
	 *
	 * @param clientThread
	 *            the client thread
	 */
	public void removeClient(PlayerClientThread clientThread) {
		playeClientList.remove(clientThread);
	}

	/**
	 * Notify all clients.
	 */
	public void notifyAllClients() {
		for (PlayerClientThread t : playeClientList) {
			System.out.println("Notifying");
			t.update();
		}
	}

}
