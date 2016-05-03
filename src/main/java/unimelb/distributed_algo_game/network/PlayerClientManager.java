/*
 * 
 */
package unimelb.distributed_algo_game.network;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.json.simple.JSONObject;

import unimelb.distributed_algo_game.network.BodyMessage.MessageType;
import unimelb.distributed_algo_game.network.NetworkInterface.ClientConnectionState;
import unimelb.distributed_algo_game.network.utils.Utils;
import unimelb.distributed_algo_game.player.AIPlayer;
import unimelb.distributed_algo_game.player.Player;
import unimelb.distributed_algo_game.pokers.Card;

// TODO: Auto-generated Javadoc
/**
 * The Class PlayerClientManager.
 */
public final class PlayerClientManager {

	/** The playe client list. */
	private Map<Integer, PlayerClientThread> playerClientList = null;
	/** The m player. */
	private Player mPlayer = null;

	private boolean isPlay = false;

	private boolean isLockRound = false;

	private ArrayList<Player> playerList = null;

	/**
	 * Instantiates a new player client manager.
	 *
	 * @param playerClientNum
	 *            the player client num
	 */
	public PlayerClientManager(int playerClientNum) {
		playerClientList = new HashMap<Integer, PlayerClientThread>(playerClientNum);
		playerList = new ArrayList<Player>();

	}

	/**
	 * This sets the player acting as the server of this client manager
	 */
	public void setPlayer(Player mPlayer) {
		this.mPlayer = mPlayer;
		playerList.add(mPlayer);
	}

	/**
	 * Adds the client.
	 * 
	 * @param clientThread
	 *            the client thread
	 */
	public void addClient(int clientID, PlayerClientThread clientThread) {
		playerClientList.put(clientID, clientThread);
	}

	public void addPlayer(int clientID) {
		playerList.add(new AIPlayer(clientID));
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

	public void removePlayer(int nodeID) {
		playerClientList.remove(nodeID);
	}

	/**
	 * Notify all clients.
	 */
	public void notifyAllClients(Object object, ClientConnectionState mConnectionState, MessageType messageType) {
		for (Map.Entry<Integer, PlayerClientThread> t : playerClientList.entrySet()) {
			JSONObject mMessage = new JSONObject();
			BodyMessage bodyMessage = new BodyMessage(mPlayer.getID(), messageType, object);
			mMessage.put("header", mConnectionState);
			mMessage.put("body", bodyMessage);
			t.getValue().sendMessage(mMessage);
		}
	}

	/**
	 * This method sends a message to a client in the thread pool
	 */
	public void sendMessageToClient(Object message, int clientID, ClientConnectionState mConnectionState,
			MessageType messageType) {

		if (playerClientList.size() > 0) {
			JSONObject mMessage = new JSONObject();
			BodyMessage bodyMessage = new BodyMessage(mPlayer.getID(), messageType, message);
			mMessage.put("header", mConnectionState);
			mMessage.put("body", bodyMessage);
			playerClientList.get(clientID).sendMessage(mMessage);
		}
	}

	public synchornized boolean isLockRound() {
		for (Map.Entry<Integer, PlayerClientThread> entry : playerClientList.entrySet()) {
			this.isLockRound = entry.getValue().getClientStatus();
		}

		return this.isLockRound;
	}

	public void updatePlayerCard(int nodeID, Card c) {
		for (Player p : playerList) {
			if (p.getID() == nodeID) {
				p.selectFromDeck(c);
				System.out.println("node: " + nodeID + ", " + c.getPattern() + ", " + c.getCardRank());
			}
		}
	}

	public void checkPlayerStatus() {
		if (isLockRound()) {
			// Dealer draw a card
			updatePlayerCard(mPlayer.getID(), mPlayer.getCard(1));
			notifyAllClients(Utils.compareRank(playerList), ClientConnectionState.CONNECTED, MessageType.BCT);
		}
	}

}
