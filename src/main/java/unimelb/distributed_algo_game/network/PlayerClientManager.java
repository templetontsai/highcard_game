/*
 * 
 */
package unimelb.distributed_algo_game.network;

import java.util.HashMap;
import java.util.Map;

// TODO: Auto-generated Javadoc
/**
 * The Class PlayerClientManager.
 */
public final class PlayerClientManager {

	/** The playe client list. */
	private Map<Integer, PlayerClientThread> playerClientList = null;

	/**
	 * Instantiates a new player client manager.
	 *
	 * @param playerClientNum
	 *            the player client num
	 */
	public PlayerClientManager(int playerClientNum) {
		playerClientList = new HashMap<Integer, PlayerClientThread>(playerClientNum);
	}

	/**
	 * Adds the client.
	 *
	 * @param clientThread
	 *            the client thread
	 */
	public void addClient(int clientID,PlayerClientThread clientThread) {
		playerClientList.put(clientID, clientThread);
	}

	/**
	 * Removes the client.
	 *
	 * @param clientThread
	 *            the client thread
	 */
	public void removeClient(int clientThread) {
		playerClientList.remove(clientThread);
	}

	/**
	 * Notify all clients.
	 */
	public void notifyAllClients(Object object) {
		for (Map.Entry<Integer, PlayerClientThread> t : playerClientList.entrySet()) {
			t.getValue().sendMessage(object);
		}
	}
	
	public void sendMessageToClient(Object mesasge, int clientID) {
		playerClientList.get(clientID).sendMessage(mesasge);
	}

}
