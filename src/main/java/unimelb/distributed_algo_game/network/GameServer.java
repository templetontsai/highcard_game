/*
 * 
 */
package unimelb.distributed_algo_game.network;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import javax.swing.JPanel;

import unimelb.distributed_algo_game.network.BodyMessage.MessageType;
import unimelb.distributed_algo_game.network.NetworkInterface.ClientConnectionState;
import unimelb.distributed_algo_game.network.gui.MainGameLoginDealerPanel;
import unimelb.distributed_algo_game.player.Player;
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

	private MainGameLoginDealerPanel mMainGameLoginDealerPanel = null;
	// This number is the total player number but not including node 0 itself
	private final int GAME_START = 3;

	/**
	 * Instantiates a new game server.
	 */
	protected GameServer() {
		mLock = new Object();
		mConnectionState = ServerConnectionState.DISCONNECTED;
		mPlayerClientManager = new PlayerClientManager(10);
	}

	/**
	 * Gets the single instance of GameServer.
	 *
	 */
	public static GameServer getInstance() {
		if (instance == null) {
			instance = new GameServer();
		}
		return instance;
	}

	/**
	 * Sets the reference to the player that is acting as the server
	 */
	public void setPlayer(Player mPlayer) {
		if (mPlayer != null) {

			this.mPlayer = mPlayer;
			nodeID = this.mPlayer.getGamePlayerInfo().getNodeID();
			mPlayerClientManager.setPlayer(this.mPlayer);
			mPlayerClientManager.addPlayer(mPlayer.getGamePlayerInfo());

		} else {
			System.out.println("Player can't be null");
			throw new NullPointerException();
		}

	}

	/**
	 * Runs the main thread of the game server
	 */
	public void run() {

		// This runs if the player is the dealer/server
		if (mPlayer.isDealer()) {

			try {
				runLeaderState();
			} catch (IOException ioe) {
				// Display the details of the exception error
				ioe.printStackTrace();

			} finally {

				System.out.println("Connection Closed");
			}

			// This runs if the player is a client
		} else {
			try {
				runSlaveState();
			} catch (IOException ioe) {
				// Display the details of the exception error
				ioe.printStackTrace();

			} finally {

				System.out.println("Connection Closed");
			}

		}

	}

	/**
	 * This method is responsible for sending and receiving messages from the
	 * clients including managing the thread pool of clients
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
					System.out.println("a client connected");
					PlayerClientThread t = new PlayerClientThread(mSocket, this, mPlayer.getGamePlayerInfo());
					// Block the new connection to join in the middle of the
					// game
					if (mPlayerClientManager.isLockRound()) {
						t.setClientStatus(true);
					}

					t.setName("GameServer Socket Thread");
					t.start();
					// Wait till we get a vaild nodeID from the connection and
					// then add to the manager's list
					while (t.getClientNodeID() == -1)
						;
					mPlayerClientManager.addPlayer(t.getClientGamePlayerInfo());
					mPlayerClientManager.addClient(t.getClientNodeID(), t);
					mMainGameLoginDealerPanel.updatePlayerList(t.getClientNodeID());

					if (mPlayerClientManager.getPlayerIDList().size() == GAME_START) {
						// TODO make sure the receiving order is correct
						System.out.println("Game Start");
						mMainGameLoginDealerPanel.showGameTable(true, mPlayerClientManager.getPlayerIDList());
						broadcastPlayerList();
						broadcastGameReadyToClients(new Boolean(true));

					}

				}
				// Close server port once the server is no longer running
				mServerSocket.close();
			}
		}
	}

	/**
	 * This runs when the server isn't the main dealer of the game and is a
	 * slave to another server
	 */
	private void runSlaveState() throws IOException {
		// TODO slavestate for a cue to work
		/*
		 * mObjectOutputStream = new
		 * ObjectOutputStream(mSocket.getOutputStream()); mObjectInputStream =
		 * new ObjectInputStream(mSocket.getInputStream()); isRunning = true;
		 * 
		 * if (mServerSocket != null) { System.out.println(
		 * "Server Start, Waiting...."); synchronized (mLock) { while
		 * (mConnectionState == ServerConnectionState.CONNECTED) {
		 * 
		 * mSocket = mServerSocket.accept(); PlayerClientThread t = new
		 * PlayerClientThread(mSocket, 1); mPlayerClientManager.addClient(new
		 * Integer(1),t);//Communicate to know player id first t.start(); }
		 * 
		 * mServerSocket.close(); } }
		 */
		// Only runs if the socket is open
		if (mServerSocket != null) {
			System.out.println("Server Start, Waiting....");
			synchronized (mLock) {
				// Only runs if the server is in a connected state
				while (mConnectionState == ServerConnectionState.CONNECTED) {

					// Listen for messages from clients and add them to the
					// thread pool
					mSocket = mServerSocket.accept();
					System.out.println("a client connected");
					PlayerClientThread t = new PlayerClientThread(mSocket, this, mPlayer.getGamePlayerInfo());
					// Block the new connection to join in the middle of the
					// game
					if (mPlayerClientManager.isLockRound()) {

						t.setClientStatus(true);
					}

					t.setName("GameServer Socket Thread");
					t.start();
					// Wait till we get a vaild nodeID from the connection and
					// then add to the manager's list
					while (t.getClientNodeID() == -1)
						;
					mPlayerClientManager.addPlayer(t.getClientGamePlayerInfo());
					mPlayerClientManager.addClient(t.getClientNodeID(), t);
					mMainGameLoginDealerPanel.updatePlayerList(t.getClientNodeID());

				}
				// Close server port once the server is no longer running
				mServerSocket.close();
			}
		}
	}

	/**
	 * Initializes the server socket and connection state of the server
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
	 * Disconnects the server from the clients by changing the server state
	 */
	public synchronized void disconnect() {
		mConnectionState = ServerConnectionState.DISCONNECTED;
	}

	/**
	 * Sends a message to all the clients in the thread pool
	 */
	public void broadcastToClients(Object object) {
		mPlayerClientManager.notifyAllClients(object, ClientConnectionState.CONNECTED, MessageType.BCT_RST);
	}

	public void broadcastGameReadyToClients(Object object) {
		mPlayerClientManager.notifyAllClients(object, ClientConnectionState.CONNECTED, MessageType.BCT_RDY);
	}

	public void broadcastCards(Object object) {
		mPlayerClientManager.notifyAllClients(object, ClientConnectionState.CONNECTED, MessageType.BCT_CRD);
	}
	
	/**
	 * Sends a player list to the other client server sockets
	 */
	public void broadcastPlayerList() {
		mPlayerClientManager.sendPlayerIDList(ClientConnectionState.CONNECTED, MessageType.BCT_LST);
	}

	/**
	 * This methods sends a card from the deck to the player
	 */
	public void sendCard(Card card, int id) {
		mPlayerClientManager.sendMessageToClient(card, id, ClientConnectionState.CONNECTED, MessageType.CRD);
	}

	/**
	 * This retrieves a card from the dealer's card deck
	 */
	public synchronized Card getCard(int index) {
		return mPlayer.getCard(index);
	}

	public synchronized void updatePlayerCard(int nodeID, Card c) {
		mPlayerClientManager.updatePlayerCard(nodeID, c);
	}

	public synchronized void checkPlayerStatus() {
		mPlayerClientManager.checkPlayerStatus();
	}

	public synchronized void removeClient(int nodeID) {
		mPlayerClientManager.removeClient(nodeID);
		mPlayerClientManager.removePlayer(nodeID);

	}

	public void setPanel(MainGameLoginDealerPanel mMainGameLoginDealerPanel) {
		this.mMainGameLoginDealerPanel = mMainGameLoginDealerPanel;
	}

	public synchronized void updateCard(Card c, int nodeID) {
		this.mMainGameLoginDealerPanel.updateCard(c, nodeID);
	}
	public void dealerDrawnCard() {
		mMainGameLoginDealerPanel.updateCard(mPlayerClientManager.dealerDrawnCard(), nodeID);
		mPlayerClientManager.checkPlayerStatus();
	}
}
