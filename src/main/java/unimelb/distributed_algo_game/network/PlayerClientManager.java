/*
 * 
 */
package unimelb.distributed_algo_game.network;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONObject;

import unimelb.distributed_algo_game.network.BodyMessage.MessageType;
import unimelb.distributed_algo_game.network.NetworkInterface.ClientConnectionState;
import unimelb.distributed_algo_game.network.utils.Utils;
import unimelb.distributed_algo_game.player.AIPlayer;
import unimelb.distributed_algo_game.player.GamePlayerInfo;
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

	private boolean isLockRound = false;

	private Map<Integer, Player> playerList = null;

	private List<Integer> playerIDList = null;

	private static final int GAME_SIZE = 3;

	/**
	 * Instantiates a new player client manager.
	 *
	 * @param playerClientNum
	 *            the player client num
	 */
	public PlayerClientManager(int playerClientNum) {
		playerClientList = new HashMap<Integer, PlayerClientThread>(playerClientNum);
		playerList = new HashMap<Integer, Player>();
		playerIDList = new ArrayList<Integer>();

	}

	/**
	 * This sets the player acting as the server of this client manager
	 */
	public void setPlayer(Player mPlayer) {
		this.mPlayer = mPlayer;
	}

	/**
	 * Adds the client.
	 * 
	 * @param clientThread
	 *            the client thread
	 */
	public synchronized void addClient(int clientID, PlayerClientThread clientThread) {
		playerClientList.put(clientID, clientThread);
	}

	public synchronized void addPlayer(GamePlayerInfo gamePlayerInfo) {

		playerList.put(gamePlayerInfo.getNodeID(), new AIPlayer(gamePlayerInfo));
		playerIDList.add(gamePlayerInfo.getNodeID());

	}

	/**
	 * Removes the client.
	 *
	 * @param clientThread
	 *            the client thread
	 */
	public synchronized void removeClient(int clientThread) {
		playerClientList.remove(clientThread);

	}

	public synchronized void removePlayer(int nodeID) {
		playerList.remove(nodeID);
		for (Integer i : playerIDList) {
			if (i == nodeID)
				playerIDList.remove(i);
		}
	}

	/**
	 * Notify all clients.
	 */
	public synchronized void notifyAllClients(Object object, ClientConnectionState mConnectionState,
			MessageType messageType) {
		for (Map.Entry<Integer, PlayerClientThread> t : playerClientList.entrySet()) {
			JSONObject mMessage = new JSONObject();
			BodyMessage bodyMessage = new BodyMessage(mPlayer.getGamePlayerInfo(), messageType, object);
			mMessage.put("header", mConnectionState);
			mMessage.put("body", bodyMessage);
			t.getValue().sendMessage(mMessage);
		}
	}

	/**
	 * This method sends a message to a client in the thread pool
	 */
	public synchronized void sendMessageToClient(Object message, int clientID, ClientConnectionState mConnectionState,
			MessageType messageType) {

		if (playerClientList.size() > 0) {
			JSONObject mMessage = new JSONObject();
			BodyMessage bodyMessage = new BodyMessage(mPlayer.getGamePlayerInfo().getNodeID(), messageType, message);
			mMessage.put("header", mConnectionState);
			mMessage.put("body", bodyMessage);
			playerClientList.get(clientID).sendMessage(mMessage);
		}
	}

	/**
	 * Sends the current client list to the client's server port
	 * 
	 * @param mConnectionState
	 * @param messageType
	 */
	public synchronized void sendClientList(ClientConnectionState mConnectionState, MessageType messageType) {
		for (Map.Entry<Integer, PlayerClientThread> t : playerClientList.entrySet()) {
			JSONObject mMessage = new JSONObject();

			BodyMessage bodyMessage = new BodyMessage(mPlayer.getGamePlayerInfo(), messageType, getPlayersSockets());

			mMessage.put("header", mConnectionState);
			mMessage.put("body", bodyMessage);
			t.getValue().sendMessage(mMessage);
		}
	}

	public synchronized void sendPlayerIDList(ClientConnectionState mConnectionState, MessageType messageType) {
		for (Map.Entry<Integer, PlayerClientThread> t : playerClientList.entrySet()) {
			JSONObject mMessage = new JSONObject();

			BodyMessage bodyMessage = new BodyMessage(mPlayer.getGamePlayerInfo(), messageType, playerIDList);

			mMessage.put("header", mConnectionState);
			mMessage.put("body", bodyMessage);
			t.getValue().sendMessage(mMessage);
		}

	}

	public synchronized boolean isLockRound() {
		if (playerClientList.size() >= 1) {
			for (Map.Entry<Integer, PlayerClientThread> entry : playerClientList.entrySet()) {
				this.isLockRound = entry.getValue().getClientStatus();
			}

		} else {

			this.isLockRound = false;
		}

		return this.isLockRound;

	}

	/**
	 * Generates list of current clients and their socket details
	 * 
	 * @return
	 */
	public String getPlayersSockets() {
		StringBuilder sb = new StringBuilder();
		for (Map.Entry<Integer, PlayerClientThread> t : playerClientList.entrySet()) {
			GamePlayerInfo playerInfo = t.getValue().getClientGamePlayerInfo();
			sb.append(playerInfo.getNodeID() + ":" + playerInfo.getIPAddress() + ":" + playerInfo.getPort() + "\n");
		}
		return sb.toString();
	}

	public synchronized void updatePlayerCard(int nodeID, Card c) {
		Player p = playerList.get(nodeID);
		p.selectFromDeck(c);
		System.out.println("node: " + nodeID + ", " + c.getPattern() + ", " + c.getCardRank());

	}

	public synchronized void checkPlayerStatus() {
		// Trigger the play panel here and to have the fixed size of the player
		if (isLockRound() && playerIDList.size() == GAME_SIZE) {

			notifyAllClients(Utils.compareRank(playerList), ClientConnectionState.CONNECTED, MessageType.BCT_RST);
			for (Map.Entry<Integer, PlayerClientThread> entry : playerClientList.entrySet()) {
				entry.getValue().setClientStatus(false);
			}
		}
	}

	public Card dealerDrawnCard() {
		// Dealer draw a card
		Card c = mPlayer.getCard(1);
		updatePlayerCard(mPlayer.getGamePlayerInfo().getNodeID(), c);
		Map<Integer, Card> playerCard = new HashMap<Integer, Card>(1);
		playerCard.put(mPlayer.getGamePlayerInfo().getNodeID(), c);
		notifyAllClients(playerCard, ClientConnectionState.CONNECTED, MessageType.BCT_CRD);
		
		return c;
	}

	public synchronized List<Integer> getPlayerIDList() {
		return playerIDList;
	}

}
