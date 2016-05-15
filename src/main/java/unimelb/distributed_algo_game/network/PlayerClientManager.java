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
import unimelb.distributed_algo_game.player.GamePlayerInfo;
import unimelb.distributed_algo_game.player.Player;
import unimelb.distributed_algo_game.player.SlavePlayer;
import unimelb.distributed_algo_game.pokers.Card;

// TODO: Auto-generated Javadoc
/**
 * The Class PlayerClientManager.
 */
public final class PlayerClientManager {

	/** The player client list. */
	private Map<Integer, PlayerClientThread> playerClientList = null;
	/** The m player. */
	private Player mPlayer = null;

	private boolean isLockRound = false;
    /** The player list hash map */
	private Map<Integer, Player> playerList = null;
	/** The node list hash map */
	private Map<Integer, Player> nodeList = null;
	/** The list of client servers arraylist */
	private ArrayList<String> serverList = null;

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

	/**
	 * Adds a new player to the list
	 * @param gamePlayerInfo
	 */
	public synchronized void addPlayer(GamePlayerInfo gamePlayerInfo) {

		playerList.put(gamePlayerInfo.getNodeID(), new SlavePlayer(gamePlayerInfo));
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

	/**
	 * Removes a player from the list
	 * @param nodeID
	 */
	public synchronized void removePlayer(int nodeID) {
		playerList.remove(nodeID);
		Integer toRemove = null;
		for (Integer i : playerIDList) {
			if (i == nodeID)
				toRemove = i;
		}
		playerIDList.remove(toRemove);
		System.out.println("Player List size is "+playerIDList.size());
		notifyAllClients(playerIDList, ClientConnectionState.CONNECTED, MessageType.BCT_UPT);
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

	/**
	 * Updates the card requested by the player
	 * @param nodeID
	 * @param c
	 */
	public synchronized void updatePlayerCard(int nodeID, Card c) {
		Player p = playerList.get(nodeID);
		p.selectFromDeck(c);
		System.out.println("node: " + nodeID + ", " + c.getPattern() + ", " + c.getCardRank());

	}

	/**
	 * Checks all the player statuses in the game
	 */
	public synchronized int checkWinner() {
		
		int winnerNodeID = -1;
		if (isLockRound() && playerIDList.size() == GAME_SIZE) {
			winnerNodeID = Utils.compareRank(playerList);
			notifyAllClients(winnerNodeID, ClientConnectionState.CONNECTED, MessageType.BCT_RST);
			for (Map.Entry<Integer, PlayerClientThread> entry : playerClientList.entrySet()) {
				entry.getValue().setClientStatus(false);
			}
		}
		return winnerNodeID;
	}
	
	/**
	 * This sets the node list of the client's neighbors
	 * @param gamePlayerInfo
	 */
	public void addNodeToList(GamePlayerInfo gamePlayerInfo){
		nodeList.put(gamePlayerInfo.getNodeID(), new SlavePlayer(gamePlayerInfo));
	}


	public Card dealerDrawnCard() {
		Card c = null;
		if(isLockRound()) {
			// Dealer draw a card
			c = mPlayer.getCard(1);
			updatePlayerCard(mPlayer.getGamePlayerInfo().getNodeID(), c);
			Map<Integer, Card> playerCard = new HashMap<Integer, Card>(1);
			playerCard.put(mPlayer.getGamePlayerInfo().getNodeID(), c);
			notifyAllClients(playerCard, ClientConnectionState.CONNECTED, MessageType.BCT_CRD);
		}

		
		return c;
	}

	public synchronized List<Integer> getPlayerIDList() {
		return playerIDList;
	}


	/**
	 * Updates the list of client servers
	 * @param serverList
	 */
	public void updateServerList(ArrayList<String> serverList){
		this.serverList.clear();
		this.serverList = serverList;
	}

}
