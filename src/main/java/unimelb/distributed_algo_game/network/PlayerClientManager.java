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
import unimelb.distributed_algo_game.network.gui.MainGamePanel;
import unimelb.distributed_algo_game.network.utils.Utils;
import unimelb.distributed_algo_game.player.DealerPlayer;
import unimelb.distributed_algo_game.player.GamePlayerInfo;
import unimelb.distributed_algo_game.player.Player;
import unimelb.distributed_algo_game.player.SlavePlayer;
import unimelb.distributed_algo_game.pokers.Card;

// TODO: Auto-generated Javadoc
/**
 * The Class PlayerClientManager.
 */
public final class PlayerClientManager {

	/** The m player. */
	private Player mPlayer = null;

	/** The m node list. */
	private List<GamePlayerInfo> mNodeList = null;

	/** The m player client list. */
	private Map<Integer, PlayerClientThread> mPlayerClientList = null;

	/** The m local player list. */
	private Map<Integer, Player> mLocalPlayerList = null;

	/** The is lock round. */
	private boolean isLockRound = false;

	/** The m main game panel. */
	private MainGamePanel mMainGamePanel = null;

	/** The m game server. */
	private GameServer mGameServer = null;

	/** The is dealer ss. */
	private boolean isDealerSS = false;

	/** The server time. */
	private long serverTime = -1;

	/** The requested timestamp. */
	private long requestedTimestamp = -1;

	/** The is crt requested. */
	private boolean isCRTRequested = false;

	/** The requested crt queue. */
	private List<Integer> requestedCRTQueue = null;

	/** The Constant GAME_SIZE. */
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
		this.serverTime = Utils.getProcessTimestamp();
		this.requestedCRTQueue = new ArrayList<Integer>();

	}

	/**
	 * Instantiates a new player client manager.
	 *
	 * @param playerClientNum
	 *            the player client num
	 * @param mPlayer
	 *            the m player
	 * @param mGameServer
	 *            the m game server
	 */
	public PlayerClientManager(int playerClientNum, Player mPlayer, GameServer mGameServer) {

		mPlayerClientList = new HashMap<Integer, PlayerClientThread>(playerClientNum);
		mLocalPlayerList = new HashMap<Integer, Player>();
		mNodeList = new ArrayList<GamePlayerInfo>();
		this.mPlayer = mPlayer;
		mLocalPlayerList.put(mPlayer.getGamePlayerInfo().getNodeID(), mPlayer);
		mNodeList.add(mPlayer.getGamePlayerInfo());
		this.mGameServer = mGameServer;
		this.isDealerSS = mPlayer.isDealer();
		this.serverTime = Utils.getProcessTimestamp();
		this.requestedCRTQueue = new ArrayList<Integer>();

	}

	/**
	 * Adds a new client to the manager.
	 *
	 * @param nodeID
	 *            the node id
	 * @param mPlayerClientThread
	 *            the m player client thread
	 */
	public synchronized void addClient(int nodeID, PlayerClientThread mPlayerClientThread) {
		mPlayerClientList.put(nodeID, mPlayerClientThread);
	}

	/**
	 * Adds a new node to the manager.
	 *
	 * @param gamePlayerInfo
	 *            the game player info
	 */
	public synchronized void addNode(GamePlayerInfo gamePlayerInfo) {

		mLocalPlayerList.put(gamePlayerInfo.getNodeID(), new SlavePlayer(gamePlayerInfo));
		mNodeList.add(gamePlayerInfo);
		if (isDealerSS) {

			mMainGamePanel.updatePlayerList(gamePlayerInfo.getNodeID());
		}

	}

	/**
	 * Closes all existing client connections.
	 */
	public synchronized void closeAllClientConnection() {
		for (Map.Entry<Integer, PlayerClientThread> t : mPlayerClientList.entrySet()) {
			t.getValue().closeConnection();
		}
	}

	/**
	 * Removes a node from all the lists in the manager.
	 *
	 * @param nodeID
	 *            the node id
	 */
	public synchronized void removeNode(int nodeID) {
		int index = 0;
		int toRemove = -1;
		for (GamePlayerInfo gamePlayerInfo : mNodeList) {
			if (gamePlayerInfo.getNodeID() == nodeID) {
				toRemove = index;

			}
			index++;
		}
		if (toRemove != -1) {
			mNodeList.remove(toRemove);
			mLocalPlayerList.remove(toRemove);
			mPlayerClientList.remove(toRemove);
		}
	}

	/**
	 * Removes all the clients from all the lists.
	 */
	public synchronized void removeAll() {
		mNodeList.clear();
		mLocalPlayerList.clear();
		mPlayerClientList.clear();
	}

	/**
	 * Notify all clients.
	 *
	 * @param object
	 *            the object
	 * @param mConnectionState
	 *            the m connection state
	 * @param messageType
	 *            the message type
	 */
	public synchronized void notifyAllClients(Object object, ClientConnectionState mConnectionState,
			MessageType messageType) {
		for (Map.Entry<Integer, PlayerClientThread> t : mPlayerClientList.entrySet()) {
			JSONObject mMessage = new JSONObject();
			BodyMessage bodyMessage = new BodyMessage(this.mPlayer.getGamePlayerInfo(), messageType, object);
			mMessage.put("header", mConnectionState);
			mMessage.put("body", bodyMessage);
			t.getValue().sendMessage(mMessage);
		}
	}

	/**
	 * This method sends a message to a client in the thread pool.
	 *
	 * @param message
	 *            the message
	 * @param clientID
	 *            the client id
	 * @param mConnectionState
	 *            the m connection state
	 * @param messageType
	 *            the message type
	 */
	public synchronized void sendMessageToClient(Object message, int clientID, ClientConnectionState mConnectionState,
			MessageType messageType) {

		if (mPlayerClientList.size() > 0) {
			JSONObject mMessage = new JSONObject();
			BodyMessage bodyMessage = new BodyMessage(this.mPlayer.getGamePlayerInfo().getNodeID(), messageType,
					message);
			mMessage.put("header", mConnectionState);
			mMessage.put("body", bodyMessage);
			mPlayerClientList.get(clientID).sendMessage(mMessage);
		}
	}

	/**
	 * Sends the node list to all the clients.
	 *
	 * @param mConnectionState
	 *            the m connection state
	 * @param messageType
	 *            the message type
	 */
	public synchronized void sendNodeList(ClientConnectionState mConnectionState, MessageType messageType) {

		if (mNodeList != null && mNodeList.size() > 0) {
			for (Map.Entry<Integer, PlayerClientThread> t : mPlayerClientList.entrySet()) {
				JSONObject mMessage = new JSONObject();

				BodyMessage bodyMessage = new BodyMessage(this.mPlayer.getGamePlayerInfo(), messageType, mNodeList);

				mMessage.put("header", mConnectionState);
				mMessage.put("body", bodyMessage);

				t.getValue().sendMessage(mMessage);
			}
		}
	}

	/**
	 * Adds the crt requested queue.
	 *
	 * @param nodeID
	 *            the node id
	 */
	public synchronized void addCRTRequestedQueue(int nodeID) {
		requestedCRTQueue.add(nodeID);
	}

	/**
	 * Broadcast crt is free.
	 */
	public synchronized void broadcastCRTIsFree() {
		if (requestedCRTQueue.size() > 0) {
			for (Integer i : requestedCRTQueue) {
				JSONObject mMessage = new JSONObject();
				BodyMessage bodyMessage = new BodyMessage(this.mPlayer.getGamePlayerInfo().getNodeID(),
						MessageType.BCT_CRT_FREE, "CRT is Free");
				mMessage.put("header", ClientConnectionState.CONNECTED);
				mMessage.put("body", bodyMessage);
				mPlayerClientList.get(i).sendMessage(mMessage);
			}
		}

	}

	/**
	 * Checks if all the clients are currently in a game round.
	 *
	 * @return true, if is lock round
	 */

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

	/**
	 * Returns the complete player information of all the clients.
	 *
	 * @return the players sockets
	 */
	public synchronized String getPlayersSockets() {
		StringBuilder sb = new StringBuilder();
		for (Map.Entry<Integer, PlayerClientThread> t : mPlayerClientList.entrySet()) {
			GamePlayerInfo playerInfo = t.getValue().getClientGamePlayerInfo();
			sb.append(playerInfo.getNodeID() + ":" + playerInfo.getIPAddress() + ":" + playerInfo.getPort() + "\n");
		}
		return sb.toString();
	}

	/**
	 * Updates the card requested by the player.
	 *
	 * @param nodeID
	 *            the node id
	 * @param c
	 *            the c
	 */
	public synchronized void updatePlayerCard(int nodeID, Card c) {
		Player p = mLocalPlayerList.get(nodeID);
		p.selectFromDeck(c);
		System.out.println("node: " + nodeID + ", " + c.getPattern() + ", " + c.getCardRank());

	}

	/**
	 * Checks all the player statuses in the game.
	 *
	 * @return the int
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

	/**
	 * Used to notify all the clients of the card drawn by the dealer.
	 */
	public synchronized void dealerDrawnCard() {
		Card c = null;
		if (isLockRound()) {
			// Dealer draw a card
			c = mPlayer.getCard(1);
			updatePlayerCard(mPlayer.getGamePlayerInfo().getNodeID(), c);
			Map<Integer, Card> playerCard = new HashMap<Integer, Card>(1);
			playerCard.put(mPlayer.getGamePlayerInfo().getNodeID(), c);
			notifyAllClients(playerCard, ClientConnectionState.CONNECTED, MessageType.BCT_CRD);

			if (c != null) {
				mMainGamePanel.updateCard(c, mPlayer.getGamePlayerInfo().getNodeID());
				mMainGamePanel.declareWinner(checkWinner());
			}
		}

	}

	/**
	 * Returns the list of player IDs in the game.
	 *
	 * @return the player id list
	 */
	public synchronized List<Integer> getPlayerIDList() {

		List<Integer> playerIDList = new ArrayList<Integer>(mLocalPlayerList.size());
		for (GamePlayerInfo p : mNodeList) {
			playerIDList.add(p.getNodeID());
		}
		System.out.println(mLocalPlayerList.size());
		return playerIDList;
	}

	/**
	 * Broadcasts the winner to the all the clients.
	 *
	 * @param object
	 *            the object
	 */
	public synchronized void broadcastGameResultToNodes(Object object) {

		notifyAllClients(object, ClientConnectionState.CONNECTED, MessageType.BCT_RST);
	}

	/**
	 * Broadcasts the game is ready to play to all the clients.
	 *
	 * @param object
	 *            the object
	 */
	public synchronized void broadcastGameReadyToNodes(Object object) {
		notifyAllClients(object, ClientConnectionState.CONNECTED, MessageType.BCT_RDY);
	}

	/**
	 * Broadcasts the card selected by a node to all the clients.
	 *
	 * @param object
	 *            the object
	 */
	public synchronized void broadcastNodeCard(Object object) {
		notifyAllClients(object, ClientConnectionState.CONNECTED, MessageType.BCT_CRD);

	}

	/**
	 * Broadcasts the list of nodes to all the clients.
	 */
	public synchronized void broadcastNodeList() {
		sendNodeList(ClientConnectionState.CONNECTED, MessageType.BCT_NODE_LST);
	}

	/**
	 * Broadcasts the updated node list to all the clients.
	 */
	public synchronized void broadcastUpdateNodeList() {
		sendNodeList(ClientConnectionState.CONNECTED, MessageType.BCT_NODE_UPT);
	}

	/**
	 * Returns the number of nodes in the game.
	 *
	 * @return the num of nodes
	 */
	public synchronized int getNumOfNodes() {
		return getPlayerIDList().size();
	}

	/**
	 * Sets the game panel.
	 *
	 * @param mMainGamePanel
	 *            the new panel
	 */
	public void setPanel(MainGamePanel mMainGamePanel) {
		this.mMainGamePanel = mMainGamePanel;
	}

	/**
	 * Updates the game panel.
	 */
	public synchronized void updateGameTable() {
		if (!isDealerSS) {
			System.out.println("This is not dealer server socket, do nothing");

		} else {
			mMainGamePanel.updateGameTable(getPlayerIDList());
		}
	}

	/**
	 * Returns if the player is the dealer.
	 *
	 * @return true, if is dealer
	 */
	public boolean isDealer() {
		return mPlayer.isDealer();
	}

	/**
	 * Returns a card from the dealer's deck.
	 *
	 * @param index
	 *            the index
	 * @return the card
	 */
	public synchronized Card getCard(int index) {
		return mPlayer.getCard(index);
	}

	/**
	 * Resets the game.
	 *
	 * @param num
	 *            the num
	 */
	public synchronized void resetGameStart(int num) {
		mGameServer.resetGameStart(num);
	}

	/**
	 * Sets the card requested boolean.
	 *
	 * @param isRequested
	 *            the is requested
	 * @param requestedTimestamp
	 *            the requested timestamp
	 */
	public void setIsRequested(boolean isRequested, long requestedTimestamp) {
		mGameServer.setIsRequested(isRequested, requestedTimestamp);
	}

	/**
	 * Returns the timestamp of the server.
	 *
	 * @return the requested timestamp
	 */
	public long getRequestedTimestamp() {
		return this.requestedTimestamp;
	}

	/**
	 * Returns the requested state of the server.
	 *
	 * @return true, if is requested
	 */
	public boolean isRequested() {
		return this.isCRTRequested;
	}

	/**
	 * Updates the card drawn by a player.
	 *
	 * @param c
	 *            the c
	 * @param nodeID
	 *            the node id
	 */
	public synchronized void updateCard(Card c, int nodeID) {
		this.mMainGamePanel.updateCard(c, nodeID);
	}

	/**
	 * draws the game table.
	 */
	public synchronized void showGameTable() {
		this.mMainGamePanel.showGameTable(true, getPlayerIDList());
	}

	/**
	 * Returns if this is a dealer client thread.
	 *
	 * @return true, if is dealer ss
	 */
	public boolean isDealerSS() {
		return this.isDealerSS;
	}

	/**
	 * Reinitializes the game as a player.
	 *
	 * @param player
	 *            the player
	 * @param newDealer
	 *            the new dealer
	 */
	public void reInitGameAsPlayer(GamePlayerInfo player, GamePlayerInfo newDealer) {
		closeAllClientConnection();
		mGameServer.reInitGameAsPlayer(player, newDealer);
	}

	/**
	 * Sets the is crt requested.
	 *
	 * @param isRequested
	 *            the is requested
	 * @param requestedTimestamp
	 *            the requested timestamp
	 */
	public void setIsCRTRequested(boolean isRequested, long requestedTimestamp) {
		this.isCRTRequested = isRequested;
		this.requestedTimestamp = requestedTimestamp;
	}

	/**
	 * Gets the server time.
	 *
	 * @return the server time
	 */
	public long getServerTime() {
		return this.serverTime;
	}

	/**
	 * Returns the list of player information.
	 *
	 * @return the node list
	 */
	public List<GamePlayerInfo> getNodeList() {
		return mNodeList;
	}

}
