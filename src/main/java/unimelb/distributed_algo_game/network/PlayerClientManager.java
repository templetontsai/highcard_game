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
	private MainGamePanel mMainGamePanel = null;
	private GameServer mGameServer = null;
	private boolean isDealerSS = false;

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

	public PlayerClientManager(int playerClientNum, Player mPlayer, GameServer mGameServer) {

		mPlayerClientList = new HashMap<Integer, PlayerClientThread>(playerClientNum);
		mLocalPlayerList = new HashMap<Integer, Player>();
		mNodeList = new ArrayList<GamePlayerInfo>();
		this.mPlayer = mPlayer;
		mLocalPlayerList.put(mPlayer.getGamePlayerInfo().getNodeID(), mPlayer);
		mNodeList.add(mPlayer.getGamePlayerInfo());
		this.mGameServer = mGameServer;
		this.isDealerSS = mPlayer.isDealer();

	}

	public synchronized void addClient(int nodeID, PlayerClientThread mPlayerClientThread) {
		mPlayerClientList.put(nodeID, mPlayerClientThread);
	}

	public synchronized void addNode(GamePlayerInfo gamePlayerInfo) {

		mLocalPlayerList.put(gamePlayerInfo.getNodeID(), new SlavePlayer(gamePlayerInfo));
		mNodeList.add(gamePlayerInfo);
		if(isDealerSS) {
			mMainGamePanel.updatePlayerList(gamePlayerInfo.getNodeID());
		}
	

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
		if (toRemove != -1) {
			mNodeList.remove(toRemove);
			mLocalPlayerList.remove(toRemove);
			mPlayerClientList.remove(toRemove);
		}
	}

	/**
	 * Notify all clients.
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
	 * This method sends a message to a client in the thread pool
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

	public synchronized String getPlayersSockets() {
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

	public synchronized List<Integer> getPlayerIDList() {

		List<Integer> playerIDList = new ArrayList<Integer>(mLocalPlayerList.size());
		for (GamePlayerInfo p : mNodeList) {
			playerIDList.add(p.getNodeID());
		}
		System.out.println(mLocalPlayerList.size());
		return playerIDList;
	}

	public synchronized void broadcastGameResultToNodes(Object object) {

		notifyAllClients(object, ClientConnectionState.CONNECTED, MessageType.BCT_RST);
	}

	public synchronized void broadcastGameReadyToNodes(Object object) {
		notifyAllClients(object, ClientConnectionState.CONNECTED, MessageType.BCT_RDY);
	}

	public synchronized void broadcastNodeCard(Object object) {
		notifyAllClients(object, ClientConnectionState.CONNECTED, MessageType.BCT_CRD);

	}

	public synchronized void broadcastNodeList() {
		sendNodeList(ClientConnectionState.CONNECTED, MessageType.BCT_NODE_LST);
	}

	public synchronized void broadcastUpdateNodeList() {
		sendNodeList(ClientConnectionState.CONNECTED, MessageType.BCT_NODE_UPT);
	}

	public synchronized int getNumOfNodes() {
		return getPlayerIDList().size();
	}

	public void setPanel(MainGamePanel mMainGamePanel) {
		this.mMainGamePanel = mMainGamePanel;
	}

	public synchronized void updateGameTable() {
		if (mMainGamePanel == null) {
			System.out.println("mMainGamePanel == null");

		} else {
			mMainGamePanel.updateGameTable(getPlayerIDList());
		}
	}

	public boolean isDealer() {
		return mPlayer.isDealer();
	}

	public synchronized Card getCard(int index) {
		return mPlayer.getCard(index);
	}

	public synchronized void resetGameStart(int num) {
		mGameServer.resetGameStart(num);
	}

	public void setIsRequested(boolean isRequested, long requestedTimestamp) {
		mGameServer.setIsRequested(isRequested, requestedTimestamp);
	}

	public long getRequestedTimestamp() {
		return mGameServer.getRequestedTimestamp();
	}

	public boolean isRequested() {
		return mGameServer.isRequested();
	}

	public synchronized void updateCard(Card c, int nodeID) {
		this.mMainGamePanel.updateCard(c, nodeID);
	}

	public synchronized void showGameTable() {
		this.mMainGamePanel.showGameTable(true, getPlayerIDList());
	}
	
	public boolean isDealerSS() {
		return this.isDealerSS;
	}

}
