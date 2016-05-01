/*
 * 
 */
package unimelb.distributed_algo_game.network;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.text.html.HTMLDocument.Iterator;

import org.json.simple.JSONObject;

import unimelb.distributed_algo_game.network.BodyMessage.MessageType;
import unimelb.distributed_algo_game.network.NetworkInterface.ServerConnectionState;
import unimelb.distributed_algo_game.player.Player;

// TODO: Auto-generated Javadoc
/**
 * The Class PlayerClientManager.
 */
public final class PlayerClientManager {

	/** The playe client list. */
	private Map<Integer, PlayerClientThread> playerClientList = null;
	/** The m player. */
	private Player mPlayer = null;

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
	 * This sets the player acting as the server of this client manager
	 */
	public void setPlayer(Player mPlayer) {
		this.mPlayer = mPlayer;
	}

	/**
	 * Adds the client.
	 *l
	 * @param clientThread
	 *            the client thread
	 */
	public void addClient(int clientID, PlayerClientThread clientThread) {
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
	public void notifyAllClients(Object object, ServerConnectionState mConnectionState, MessageType messageType) {
		for (Map.Entry<Integer, PlayerClientThread> t : playerClientList.entrySet()) {
			JSONObject mMessage = new JSONObject();
			BodyMessage bodyMessage = new BodyMessage(t.getKey(), messageType, object);
			mMessage.put("header", mConnectionState);
			mMessage.put("body", bodyMessage);
			t.getValue().sendMessage(mMessage);
		}
	}

	/**
	 * This method sends a message to a client in the thread pool
	 */
	public void sendMessageToClient(Object message, int clientID, ServerConnectionState mConnectionState,
			MessageType messageType) {
		// System.out.println("Message: "+message+" ID: "+clientID);
		if (playerClientList.size() > 0) {
			JSONObject mMessage = new JSONObject();
			BodyMessage bodyMessage = new BodyMessage(clientID, messageType, message);
			mMessage.put("header", mConnectionState);
			mMessage.put("body", bodyMessage);
			playerClientList.get(clientID).sendMessage(mMessage);
		}
	}
}
