/*
 * 
 */
package unimelb.distributed_algo_game.network;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import unimelb.distributed_algo_game.network.BodyMessage.MessageType;
import unimelb.distributed_algo_game.network.gui.GameTablePanel;
import unimelb.distributed_algo_game.network.gui.MainGamePanel;
import unimelb.distributed_algo_game.player.DealerPlayer;
import unimelb.distributed_algo_game.player.GamePlayerInfo;
import unimelb.distributed_algo_game.player.Player;
import unimelb.distributed_algo_game.player.SlavePlayer;
import unimelb.distributed_algo_game.pokers.Card;

// TODO: Auto-generated Javadoc
/**
 * The Class GameServer.
 *
 * @author Ting-Ying Tsai
 */
public final class GameServer implements Runnable, NetworkInterface {

	/** The instance. */
	private static GameServer instance = null;

	/** The id. */
	private int nodeID = -1;

	/** The m player. */
	private Player mPlayer = null;

	/** The m socket. */
	private Socket mSocket = null;

	/** The m server socket. */
	private ServerSocket mServerSocket = null;

	/** The m lock. */
	private Object mLock = null;

	/** The connection state. */
	private ServerConnectionState mConnectionState = null;

	/** The m player client manager. */

	private PlayerClientManager mPlayerClientManager = null;

	/** The m main game panel. */
	private MainGamePanel mMainGamePanel = null;

	/** The game start. */
	// This number is the total player number but not including node 0 itself
	private int GAME_START = 3;

	/** The m game client socket manager. */
	private GameClientSocketManager mGameClientSocketManager = null;

	/** The m game client. */
	private GameClient mGameClient = null;

	/** The is requested. */
	private boolean isRequested = false;

	/** The requested timestamp. */
	private long requestedTimestamp = -1;

	/**
	 * Instantiates a new game server.
	 */
	protected GameServer() {
		mLock = new Object();
		mConnectionState = ServerConnectionState.DISCONNECTED;

	}

	/**
	 * Gets the single instance of GameServer.
	 *
	 * @return single instance of GameServer
	 */
	public static GameServer getInstance() {
		if (instance == null) {
			instance = new GameServer();
		}
		return instance;
	}

	/**
	 * Sets the reference to the player that is acting as the server.
	 *
	 * @param mPlayer
	 *            the new player
	 */
	public void setPlayer(Player mPlayer) {
		if (mPlayer != null) {

			this.mPlayer = mPlayer;
			nodeID = this.mPlayer.getGamePlayerInfo().getNodeID();
			mPlayerClientManager = new PlayerClientManager(10, this.mPlayer, this);

		} else {
			System.out.println("Player can't be null");
			throw new NullPointerException();
		}

	}

	/**
	 * Runs the main thread of the game server.
	 */
	public void run() {

		// This runs if the player is the dealer/server
		if (mPlayer.isDealer()) {

			try {
				runLeaderState();
			} catch (IOException ioe) {
				// Display the details of the exception error
				System.out.println("Connection Closed in GameServer, leader state");

			}
			// This runs if the player is a client
		} else {
			try {

				runSlaveState();
			} catch (IOException ioe) {
				// Display the details of the exception error
				System.out.println("Connection Closed in GameServer, slave state");

			}

		}

	}

	/**
	 * This method is responsible for sending and receiving messages from the
	 * clients including managing the thread pool of clients.
	 *
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	private void runLeaderState() throws IOException {

		// Only runs if the socket is open
		if (mServerSocket != null) {
			System.out.println("Server Start, Waiting....");
			synchronized (mLock) {

				// Only runs if the server is in a connected state
				while (mConnectionState == ServerConnectionState.CONNECTED) {

					// Listen for messages from clients and add them to the
					// thread pool
					mSocket = mServerSocket.accept();
					System.out.println("a client connected to node" + mPlayer.getGamePlayerInfo().getNodeID());
					PlayerClientThread t = new PlayerClientThread(mSocket, mPlayerClientManager,
							mPlayer.getGamePlayerInfo());

					t.setName("GameServer Socket Thread");
					t.start();

					// Wait till we get a valid nodeID from the connection and
					// then add to the manager's list
					while (t.getClientNodeID() == -1)
						;
					mPlayerClientManager.addClient(t.getClientNodeID(), t);
					mPlayerClientManager.addNode(t.getClientGamePlayerInfo());

					System.out.println(
							"GameServer: getPlayerIDList size: " + mPlayerClientManager.getPlayerIDList().size());
					if (mPlayerClientManager.getPlayerIDList().size() == GAME_START) {

						broadcastNodeList();
						System.out.println("Game Start");

						try {
							Thread.sleep(2000);
						} catch (InterruptedException e) {
							// TODO Adding error handling
							e.printStackTrace();
						}

						broadcastGameReadyToNodes(new Boolean(true));

						mPlayerClientManager.showGameTable();

					}

				}
				// Close server port once the server is no longer running
				mServerSocket.close();
			}
		}
	}

	/**
	 * This runs when the server isn't the main dealer of the game and is a
	 * slave to another server.
	 *
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	private void runSlaveState() throws IOException {
		// TODO slavestate for a cue to work
		// Only runs if the socket is open

		if (mServerSocket != null) {
			System.out.println("Server Slave Start, Waiting....");
			synchronized (mLock) {
				// Only runs if the server is in a connected state
				while (mConnectionState == ServerConnectionState.CONNECTED) {

					// Listen for messages from clients and add them to the
					// thread pool
					System.out.println("Waiting for new server clients");
					mSocket = mServerSocket.accept();
					System.out.println("a client connected to node" + mPlayer.getGamePlayerInfo().getNodeID());
					PlayerClientThread t = new PlayerClientThread(mSocket, mPlayerClientManager,
							mPlayer.getGamePlayerInfo());

					t.setName("GameServer Socket Thread");
					t.start();
					// Wait till we get a vaild nodeID from the connection and
					// then add to the manager's list
					while (t.getClientNodeID() == -1)
						;
					mPlayerClientManager.addClient(t.getClientNodeID(), t);
					mPlayerClientManager.addNode(t.getClientGamePlayerInfo());

					// mGameClientSocketManager.broadcastClientsList();

				}
				System.out.println("Slave Connection closed");
				// Close server port once the server is no longer running
				mServerSocket.close();

			}

		}

	}

	/**
	 * Initializes the server socket and connection state of the server.
	 *
	 * @return true, if successful
	 */
	public synchronized boolean connect() {

		try {
			int port = Integer.parseInt(mPlayer.getGamePlayerInfo().getPort());
			mServerSocket = new ServerSocket(port);
			mConnectionState = ServerConnectionState.CONNECTED;

		} catch (IOException ioe) {
			ioe.printStackTrace();
			mServerSocket = null;
			return false;
		}
		return true;
	}

	/**
	 * Disconnects the server from the clients by changing the server state.
	 */
	public synchronized void disconnect() {
		mConnectionState = ServerConnectionState.DISCONNECTED;
		try {
			mServerSocket.close();
		} catch (IOException ioe) {
			ioe.printStackTrace();
			mServerSocket = null;
		}
		System.out.println("Server state is " + mConnectionState);
	}

	/**
	 * Sends a message to all the clients in the thread pool.
	 *
	 * @param object
	 *            the object
	 */
	public void broadcastGameResultToNodes(Object object) {

		mPlayerClientManager.notifyAllClients(object, ClientConnectionState.CONNECTED, MessageType.BCT_RST);
	}

	/**
	 * Broadcast game ready to nodes.
	 *
	 * @param object
	 *            the object
	 */
	public void broadcastGameReadyToNodes(Object object) {
		mPlayerClientManager.notifyAllClients(object, ClientConnectionState.CONNECTED, MessageType.BCT_RDY);
	}

	/**
	 * Broadcast node card.
	 *
	 * @param object
	 *            the object
	 */
	public void broadcastNodeCard(Object object) {
		mPlayerClientManager.notifyAllClients(object, ClientConnectionState.CONNECTED, MessageType.BCT_CRD);

	}

	/**
	 * Broadcast node list.
	 */
	public void broadcastNodeList() {
		mPlayerClientManager.sendNodeList(ClientConnectionState.CONNECTED, MessageType.BCT_NODE_LST);
	}

	/**
	 * Broadcast update node list.
	 */
	public void broadcastUpdateNodeList() {
		mPlayerClientManager.sendNodeList(ClientConnectionState.CONNECTED, MessageType.BCT_NODE_UPT);
	}

	/**
	 * This methods sends a card from the deck to the player.
	 *
	 * @param card
	 *            the card
	 * @param id
	 *            the id
	 */
	public void sendCard(Card card, int id) {
		mPlayerClientManager.sendMessageToClient(card, id, ClientConnectionState.CONNECTED, MessageType.CRD);
	}

	/**
	 * Removes the node.
	 *
	 * @param nodeID
	 *            the node id
	 */
	public synchronized void removeNode(int nodeID) {
		mPlayerClientManager.removeNode(nodeID);
	}

	/**
	 * This sets the player as the dealer of the game.
	 */
	public void setPlayerDealer() {
		mPlayer.setDealer(true);
	}

	/**
	 * Sets the panel.
	 *
	 * @param mMainGamePanel
	 *            the new panel
	 */
	public void setPanel(MainGamePanel mMainGamePanel) {
		this.mMainGamePanel = mMainGamePanel;
		this.mPlayerClientManager.setPanel(mMainGamePanel);
	}

	/**
	 * This returns the leader state of the player.
	 *
	 * @return the checks if is leader
	 */
	public boolean getIsLeader() {
		return mPlayer.isDealer();
	}

	/**
	 * This update the sever details for the game client.
	 *
	 * @param gameServerInfo
	 *            the new game server leader
	 */
	public void setGameServerLeader(GamePlayerInfo gameServerInfo) {
		mPlayer.setGameServerInfo(gameServerInfo);
	}

	/**
	 * This returns the player object of this server.
	 *
	 * @return the player
	 */
	public Player getPlayer() {
		return mPlayer;
	}

	/**
	 * This sets the game client object for this player server.
	 *
	 * @param mGameClient
	 *            the new game client
	 */
	public void setGameClient(GameClient mGameClient) {
		this.mGameClient = mGameClient;
	}

	/**
	 * This returns the server details of the game client.
	 *
	 * @return the server details
	 */
	public String getServerDetails() {
		return mGameClient.getServerDetails();
	}

	/**
	 * This disconnects the client connection of the player.
	 */
	public void disconnectClient() {
		mGameClient.disconnect();
	}

	/**
	 * Draws a card from the deck for the dealer.
	 */
	public void dealerDrawnCard() {

		mPlayerClientManager.dealerDrawnCard();
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
		mPlayerClientManager.setIsCRTRequested(isRequested, requestedTimestamp);
	}

	/**
	 * Broadcast cr tis free.
	 */
	public void broadcastCRTisFree() {
		mPlayerClientManager.broadcastCRTIsFree();
	}

	/**
	 * Sets the requested timestamp.
	 *
	 * @param isRequested
	 *            the is requested
	 * @param requestedTimestamp
	 *            the requested timestamp
	 */
	public void setIsRequested(boolean isRequested, long requestedTimestamp) {
		this.isRequested = isRequested;
		this.requestedTimestamp = requestedTimestamp;
	}

	/**
	 * Returns the timestamp.
	 *
	 * @return the requested timestamp
	 */
	public long getRequestedTimestamp() {
		return this.requestedTimestamp;
	}

	/**
	 * Returns the is requested boolean.
	 *
	 * @return true, if is requested
	 */

	public boolean isRequested() {
		return this.isRequested;
	}

	/**
	 * Returns the number of nodes in the game.
	 *
	 * @return the numof nodes
	 */
	public synchronized int getNumofNodes() {
		return mPlayerClientManager.getPlayerIDList().size();
	}

	/**
	 * Reinitializes the game as a dealer after leader election.
	 *
	 * @param newDealer
	 *            the new dealer
	 */
	public void reInitGameAsDealer(GamePlayerInfo newDealer) {
		System.out.println("1reInitGameAsDealer");
		int playersLeft = mPlayerClientManager.getNumOfNodes();
		if (mPlayerClientManager != null) {
			System.out.println("0mPlayerClientManager destroy");
			mPlayerClientManager.closeAllClientConnection();
			mPlayerClientManager.removeAll();
			mPlayerClientManager = null;
		}
		if (mGameClientSocketManager != null) {
			System.out.println("0mGameClientSocketManager destroy");
			mGameClientSocketManager.closeAllClientConnection();
			mGameClientSocketManager.removeAll();
			mGameClientSocketManager = null;
		}
		disconnect();
		/*
		 * try { Thread.sleep(1000); } catch (InterruptedException e) { // TODO
		 * Auto-generated catch block e.printStackTrace(); }
		 */
		System.out.println("2reInitGameAsDealer");

		newDealer.setDealer(true);
		mMainGamePanel.setDealer(true);
		mMainGamePanel.setNewLeader(true);
		Player p = new DealerPlayer("Dealer", newDealer, mMainGamePanel);
		p.setDealer(true);
		((DealerPlayer) p).setGameSize(playersLeft);
		mMainGamePanel.setPlayer(p);
		p.play();

	}

	/**
	 * Reinitializes the game as a player after leader election.
	 *
	 * @param player
	 *            the player
	 * @param newDealer
	 *            the new dealer
	 */
	public void reInitGameAsPlayer(GamePlayerInfo player, GamePlayerInfo newDealer) {

		System.out.println("1reInitGameAsPlayer");
		if (mPlayerClientManager != null) {
			System.out.println("1mPlayerClientManager destroy");
			mPlayerClientManager.closeAllClientConnection();
			mPlayerClientManager.removeAll();
			mPlayerClientManager = null;
		}
		MainGamePanel mMainGamePanel = mGameClientSocketManager.getPanel();
		if (mGameClientSocketManager != null) {
			System.out.println("1mGameClientSocketManager destroy");
			mGameClientSocketManager.closeAllClientConnection();
			mGameClientSocketManager.removeAll();
			mGameClientSocketManager = null;
		}
		disconnect();
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("2reInitGameAsPlayer");
		player.setDealer(false);
		newDealer.setDealer(true);
		mMainGamePanel.setDealer(false);
		System.out.println("Before the bug 1");
		Player p = new SlavePlayer("REPlayer", player, newDealer, mMainGamePanel);
		List<Integer> mPlayerIDList = new ArrayList<Integer>();
		mPlayerIDList.add(p.getGamePlayerInfo().getNodeID());
		GameTablePanel gameTable = new GameTablePanel(mPlayerIDList, false, p);
		mMainGamePanel.setPlayer(p);
		mMainGamePanel.updateGameTable(gameTable);
		p.setDealer(false);
		p.play();
	}

	/**
	 * Resets the game.
	 *
	 * @param num
	 *            the num
	 */
	public synchronized void resetGameStart(int num) {
		this.GAME_START = num;
	}

	/**
	 * Sets the number of players in the game.
	 *
	 * @param gameSize
	 *            the new game size
	 */
	public void setGameSize(int gameSize) {
		this.GAME_START = gameSize;
	}

	/**
	 * Sets the client socket manager of the server.
	 *
	 * @param mGameClientSocketManager
	 *            the new game client socket manager
	 */
	public void setGameClientSocketManager(GameClientSocketManager mGameClientSocketManager) {
		this.mGameClientSocketManager = mGameClientSocketManager;
	}

}
