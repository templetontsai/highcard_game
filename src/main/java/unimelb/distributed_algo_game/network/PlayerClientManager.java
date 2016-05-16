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

	private Player mPlayer = null;
	private List<GamePlayerInfo> mNodeList = null;
	private Map<Integer, PlayerClientThread> mPlayerClientList = null;
	private Map<Integer, Player> mLocalPlayerList = null;
	private boolean isLockRound = false;

	private static final int GAME_SIZE = 3;

	/**
	 * Instantiates a new player client manager.
	 *
	 * @param playerClientNum
	 *            the player client num
	 */
	public PlayerClientManager(int playerClientNum) {

		mPlayerClientList = new HashMap<Integer, PlayerClientThread>(playerClientNum);
		mLocalPlayerList = new HashMap<Integer, Player>();
		mNodeList = new ArrayList<GamePlayerInfo>();

	}

	public synchronized void addClient(int nodeID, PlayerClientThread mPlayerClientThread) {
		mPlayerClientList.put(nodeID, mPlayerClientThread);
	}
	public synchronized void addNode(GamePlayerInfo gamePlayerInfo) {
		
		mLocalPlayerList.put(gamePlayerInfo.getNodeID(), new SlavePlayer(gamePlayerInfo));
		mNodeList.add(gamePlayerInfo);
		
	}

	public synchronized void removeNode(int nodeID) {
		int index = 0;
		int toRemove = -1;
		for (GamePlayerInfo gamePlayerInfo : mNodeList) {
			if (gamePlayerInfo.getNodeID() == nodeID) {
				toRemove = index;
				
			}
			index++;
		}
		mNodeList.remove(toRemove);
		mLocalPlayerList.remove(toRemove);
		mPlayerClientList.remove(toRemove);
		
	}

	/**
	 * This sets the player acting as the server of this client manager
	 */
	public void setPlayer(Player mPlayer) {
		this.mPlayer = mPlayer;
		mLocalPlayerList.put(mPlayer.getGamePlayerInfo().getNodeID(), mPlayer);
		mNodeList.add(mPlayer.getGamePlayerInfo());
	}

	/**
	 * Notify all clients.
	 */
	public synchronized void notifyAllClients(Object object, ClientConnectionState mConnectionState,
			MessageType messageType) {
		for (Map.Entry<Integer, PlayerClientThread> t : mPlayerClientList.entrySet()) {
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

		if (mPlayerClientList.size() > 0) {
			JSONObject mMessage = new JSONObject();
			BodyMessage bodyMessage = new BodyMessage(mPlayer.getGamePlayerInfo().getNodeID(), messageType, message);
			mMessage.put("header", mConnectionState);
			mMessage.put("body", bodyMessage);
			mPlayerClientList.get(clientID).sendMessage(mMessage);
		}
	}

	public synchronized void sendNodeList(ClientConnectionState mConnectionState, MessageType messageType) {
		
		if (mNodeList != null && mNodeList.size() > 0) {
			for (Map.Entry<Integer, PlayerClientThread> t : mPlayerClientList.entrySet()) {
				JSONObject mMessage = new JSONObject();
				
				BodyMessage bodyMessage = new BodyMessage(mPlayer.getGamePlayerInfo(), messageType, mNodeList);

				mMessage.put("header", mConnectionState);
				mMessage.put("body", bodyMessage);
				
				t.getValue().sendMessage(mMessage);
			}
		}
	}

	public synchronized boolean isLockRound() {
		if (mPlayerClientList.size() >= 1) {
			for (Map.Entry<Integer, PlayerClientThread> entry : mPlayerClientList.entrySet()) {
				this.isLockRound = entry.getValue().getClientStatus();
			}

		} else {

			this.isLockRound = false;
		}

		return this.isLockRound;

	}

	public String getPlayersSockets() {
		StringBuilder sb = new StringBuilder();
		for (Map.Entry<Integer, PlayerClientThread> t : mPlayerClientList.entrySet()) {
			GamePlayerInfo playerInfo = t.getValue().getClientGamePlayerInfo();
			sb.append(playerInfo.getNodeID() + ":" + playerInfo.getIPAddress() + ":" + playerInfo.getPort() + "\n");
		}
		return sb.toString();
	}

	/**
	 * Updates the card requested by the player
	 * 
	 * @param nodeID
	 * @param c
	 */
	public synchronized void updatePlayerCard(int nodeID, Card c) {
		Player p = mLocalPlayerList.get(nodeID);
		p.selectFromDeck(c);
		System.out.println("node: " + nodeID + ", " + c.getPattern() + ", " + c.getCardRank());

	}

	/**
	 * Checks all the player statuses in the game
	 */
	public synchronized int checkWinner() {

		int winnerNodeID = -1;
		if (isLockRound()) {
			winnerNodeID = Utils.compareRank(mLocalPlayerList);
			notifyAllClients(winnerNodeID, ClientConnectionState.CONNECTED, MessageType.BCT_RST);
			for (Map.Entry<Integer, PlayerClientThread> entry : mPlayerClientList.entrySet()) {
				entry.getValue().setClientStatus(false);
			}
		}
		return winnerNodeID;
	}

	public Card dealerDrawnCard() {
		Card c = null;
		if (isLockRound()) {
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

		List<Integer> playerIDList = new ArrayList<Integer>(mLocalPlayerList.size());
		for (GamePlayerInfo p : mNodeList) {
			playerIDList.add(p.getNodeID());
		}
		System.out.println(mLocalPlayerList.size());
		return playerIDList;
	}

}
