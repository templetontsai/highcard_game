/*
 * 
 */
package unimelb.distributed_algo_game.network;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.text.html.HTMLDocument.Iterator;

import org.json.simple.JSONObject;

import unimelb.distributed_algo_game.network.NetworkInterface.ServerConnectionState;

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
	public void notifyAllClients(Object object, ServerConnectionState mConnectionState) {
		for (Map.Entry<Integer, PlayerClientThread> t : playerClientList.entrySet()) {
			JSONObject mMessage = new JSONObject();
			   BodyMessageJSON bodyMessage = new BodyMessageJSON(t.getKey(), "BCT", object);
			   mMessage.put("header", mConnectionState);
			   mMessage.put("body", bodyMessage);
			t.getValue().sendMessage(mMessage);
		}
	}
	
	public void sendMessageToClient(Object message, int clientID,ServerConnectionState mConnectionState) {
		//System.out.println("Message: "+message+" ID: "+clientID);
		if(playerClientList.size()>0){
		   JSONObject mMessage = new JSONObject();
		   BodyMessageJSON bodyMessage = new BodyMessageJSON(clientID, "CRD", message);
		   mMessage.put("header", mConnectionState);
		   mMessage.put("body", bodyMessage);
		   playerClientList.get(clientID).sendMessage(mMessage);
		}
	}

}
