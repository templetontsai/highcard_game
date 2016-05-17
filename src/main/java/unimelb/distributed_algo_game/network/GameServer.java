/*
 * 
 */
package unimelb.distributed_algo_game.network;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

import javax.swing.JPanel;

import org.json.simple.JSONObject;

import unimelb.distributed_algo_game.network.BodyMessage.MessageType;
import unimelb.distributed_algo_game.network.NetworkInterface.ClientConnectionState;
import unimelb.distributed_algo_game.network.gui.MainGameFrameGUI;
import unimelb.distributed_algo_game.network.gui.MainGamePanel;
import unimelb.distributed_algo_game.player.DealerPlayer;
import unimelb.distributed_algo_game.player.SlavePlayer;
import unimelb.distributed_algo_game.player.GamePlayerInfo;
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

	private MainGamePanel mMainGameLoginDealerPanel = null;
	// This number is the total player number but not including node 0 itself
	private int GAME_START = 3;

	private GameClientSocketManager mGameClientSocketManager = null;

	private GameClient mGameClient = null;

	private boolean isRequested = false;
	private long requestedTimestamp = -1;

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
					PlayerServerThread t2 = new PlayerServerThread(this, mPlayer.getGamePlayerInfo());

					t.setName("GameServer Socket Thread");
					t.start();

					// Wait till we get a valid nodeID from the connection and
					// then add to the manager's list
					while (t.getClientNodeID() == -1)
						;
					mPlayerClientManager.addClient(t.getClientNodeID(), t);
					mPlayerClientManager.addNode(t.getClientGamePlayerInfo());
					/*mPlayerServerManager.addPlayer(t.getClientGamePlayerInfo());
					mPlayerServerManager.addClient(t.getClientNodeID(), t2);
					t2.setGameClientInfo(t.getClientGamePlayerInfo());
					t2.setName("GameServer Client Socket Thread");
					t2.connect();
					t2.start();*/

					mMainGameLoginDealerPanel.updatePlayerList(t.getClientNodeID());
					broadcastNodeList();
					if (mPlayerClientManager.getPlayerIDList().size() == GAME_START) {

						System.out.println("Game Start");

						try {
							Thread.sleep(2000);
						} catch (InterruptedException e) {
							// TODO Adding error handling
							e.printStackTrace();
						}
						broadcastClientList();
						broadcastGameReadyToNodes(new Boolean(true));
						
						mMainGameLoginDealerPanel.showGameTable(true, mPlayerClientManager.getPlayerIDList());
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
					System.out.println("a client connected in slave state");
					PlayerClientThread t = new PlayerClientThread(mSocket, this, mPlayer.getGamePlayerInfo());

					t.setName("GameServer Socket Thread");
					t.start();
					// Wait till we get a vaild nodeID from the connection and
					// then add to the manager's list
					while (t.getClientNodeID() == -1)
						;
					mPlayerClientManager.addClient(t.getClientNodeID(), t);
					mPlayerClientManager.addNode(t.getClientGamePlayerInfo());
					mGameClientSocketManager.addSocketClient(t.getClientGamePlayerInfo());

				}
				System.out.println("Slave Connection closed");
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
		try {
			mServerSocket.close();
		} catch (IOException ioe) {
			ioe.printStackTrace();
			mServerSocket = null;
		}
		System.out.println("Server state is " + mConnectionState);
	}

	/**
	 * Sends a message to all the clients in the thread pool
	 */
	public void broadcastGameResultToNodes(Object object) {

		mPlayerClientManager.notifyAllClients(object, ClientConnectionState.CONNECTED, MessageType.BCT_RST);
	}

	public void broadcastGameReadyToNodes(Object object) {
		mPlayerClientManager.notifyAllClients(object, ClientConnectionState.CONNECTED, MessageType.BCT_RDY);
	}

	public void broadcastNodeCard(Object object) {
		mPlayerClientManager.notifyAllClients(object, ClientConnectionState.CONNECTED, MessageType.BCT_CRD);

	}

	public void broadcastNodeList() {
		mPlayerClientManager.sendNodeList(ClientConnectionState.CONNECTED, MessageType.BCT_LST);
	}

	public void broadcastUpdateNodeList() {
		mPlayerClientManager.sendNodeList(ClientConnectionState.CONNECTED, MessageType.BCT_UPT);
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

	/**
	 * This updates the player card of the player
	 * 
	 * @param nodeID
	 * @param c
	 */
	public synchronized void updatePlayerCard(int nodeID, Card c) {
		mPlayerClientManager.updatePlayerCard(nodeID, c);
	}

	public synchronized void removeNode(int nodeID) {
		mPlayerClientManager.removeNode(nodeID);
	}

	/**
	 * This sets the player as the dealer of the game
	 */
	public void setPlayerDealer() {
		mPlayer.setDealer(true);
	}

	public void setPanel(MainGamePanel mMainGameLoginDealerPanel) {
		this.mMainGameLoginDealerPanel = mMainGameLoginDealerPanel;
	}

	/**
	 * This returns the leader state of the player
	 * 
	 * @return
	 */
	public boolean getIsLeader() {
		return mPlayer.isDealer();
	}

	/**
	 * This update the sever details for the game client
	 */
	public void setGameServerLeader(GamePlayerInfo gameServerInfo) {
		mPlayer.setGameServerInfo(gameServerInfo);
	}

	/**
	 * This updates all the servers with the current list of players
	 * 
	 * @param gameClients
	 */
	public void updateServerList(ArrayList<String> gameClients) {
		// TODO Auto-generated method stub
		mGameClientSocketManager.updateServerList(gameClients);
	}

	/**
	 * This starts a leader election by creating a thread connecting to the next
	 * player
	 */
	public void startElection() {

		GamePlayerInfo nextPlayer = mGameClientSocketManager.getNextNeighbor();
		if (nextPlayer != null) {
			mGameClientSocketManager.startElection();
		} else {
			System.out.println("Not enough players to elect a new leader");
		}
	}

	public void connectToNeighbor() {
		GamePlayerInfo nextPlayer = mGameClientSocketManager.getNextNeighbor();
		if (nextPlayer != null) {
			PlayerServerThread t = new PlayerServerThread(this, mPlayer.getGamePlayerInfo());
			mGameClientSocketManager.addPlayer(nextPlayer);
			mGameClientSocketManager.addClient(nextPlayer.getNodeID(), t);

			t.setGameClientInfo(nextPlayer);
			t.setName("GameServer Election Socket Thread");
			t.connect();
			t.start();

		} else {
			System.out.println("No neighbor defined");
		}
	}

	/**
	 * This sends a message to the server's neighbor server
	 * 
	 * @param mMessage
	 */
	public void sendMessageToNext(JSONObject mMessage) {
		mGameClientSocketManager.sendMessageToNeighbor(mMessage);
	}

	/**
	 * This returns the player object of this server
	 * 
	 * @return
	 */
	public Player getPlayer() {
		return mPlayer;
	}

	/**
	 * This sets the game client object for this player server
	 * 
	 * @param mGameClient
	 */
	public void setGameClient(GameClient mGameClient) {
		this.mGameClient = mGameClient;
	}

	/**
	 * This returns the server details of the game client
	 * 
	 * @return
	 */
	public String getServerDetails() {
		return mGameClient.getServerDetails();
	}

	/**
	 * This disconnects the client connection of the player
	 */
	public void disconnectClient() {
		mGameClient.disconnect();
	}

	/**
	 * This reconnects a client to the server
	 */
	public void reconnectClient() {
		((SlavePlayer) mPlayer).rePlay();
	}

	public synchronized void updateCard(Card c, int nodeID) {
		this.mMainGameLoginDealerPanel.updateCard(c, nodeID);
	}

	public void dealerDrawnCard() {

		Card c = mPlayerClientManager.dealerDrawnCard();
		if (c != null) {
			mMainGameLoginDealerPanel.updateCard(c, nodeID);
			mMainGameLoginDealerPanel.declareWinner(mPlayerClientManager.checkWinner());
		}

	}

	public boolean getReply() {
		return mGameClientSocketManager.isAllCRTReplied();
	}

	public void setIsRequested(boolean isRequested, long requestedTimestamp) {
		this.isRequested = isRequested;
		this.requestedTimestamp = requestedTimestamp;
	}

	public long getRequestedTimestamp() {
		return this.requestedTimestamp;
	}

	public boolean isRequested() {
		return this.isRequested;
	}

	public void updateGameTable() {
		if (mMainGameLoginDealerPanel == null) {
			System.out.println("mMainGameLoginDealerPanel == null");

		} else {
			mMainGameLoginDealerPanel.updateGameTable(mPlayerClientManager.getPlayerIDList());
		}
	}

	public synchronized int getNumofNodes() {
		return mPlayerClientManager.getPlayerIDList().size();
	}

	public void startServer() {
		mConnectionState = ServerConnectionState.CONNECTED;
		DealerPlayer p = new DealerPlayer("Dealer", mPlayer.getGamePlayerInfo(), mMainGameLoginDealerPanel);

		MainGameFrameGUI mainGui = new MainGameFrameGUI("High Card Game", p.getGamePlayerInfo().getNodeID());
		MainGamePanel mainPanel = new MainGamePanel(mainGui, true);
		((DealerPlayer) mPlayer).restartServer(mainPanel);
		/*
		 * p.setDealer(true); this.mPlayer = p; System.out.println(
		 * "The player is "+p.isDealer()+"-"+getIsLeader());
		 * ((DealerPlayer)mPlayer).restartServer();
		 * 
		 * if(mMainGameLoginDealerPanel!=null)
		 * mMainGameLoginDealerPanel.updateGameTable(mPlayerClientManager.
		 * getPlayerIDList());
		 * 
		 * broadcastPlayerList(); broadcastClientList();
		 */

	}
	
	public GamePlayerInfo getNextPlayer(){
		return mGameClientSocketManager.getNextNeighbor();
	}

	public void resetGameStart(int num) {
		this.GAME_START = num;
	}

	public void broadcastClientList() {
		mGameClientSocketManager.sendClientList(ClientConnectionState.CONNECTED, MessageType.LST);
	}
	
	public boolean isDealer() {
		return mPlayer.isDealer();
	}
}
