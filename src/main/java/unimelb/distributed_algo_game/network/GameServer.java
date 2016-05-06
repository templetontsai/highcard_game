/*
 * 
 */
package unimelb.distributed_algo_game.network;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;

import org.json.simple.JSONObject;

import unimelb.distributed_algo_game.network.BodyMessage.MessageType;
import unimelb.distributed_algo_game.network.utils.Utils;
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
	private Object mLock;

	/** The connection state. */
	private ServerConnectionState mConnectionState;

	/** The m player client manager. */
	private PlayerClientManager mPlayerClientManager;
	
	/** The configuration file with addresses and ports**/
	private FileReaderWriter configFileReader;
	
	/** Maintains the list of the server addresses **/
	private List<String> serverDetails;

	/**
	 * Instantiates a new game server.
	 */
	protected GameServer() {
		mLock = new Object();
		mConnectionState = ServerConnectionState.DISCONNECTED;
		mPlayerClientManager = new PlayerClientManager(10);
		configFileReader = new FileReaderWriter();
		configFileReader.readConfig();
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
			nodeID = this.mPlayer.getID();
			mPlayerClientManager.setPlayer(this.mPlayer);
			//mPlayerClientManager.addPlayer(nodeID);
			mPlayerClientManager.addPlayer(mPlayer.getGamePlayerInfo());

		} else {
			System.out.println("Player can't be null");
			throw new NullPointerException();
		}

	}

	/**
	 * Sets the id.
	 *
	 */
	public void setId(int nodeID) {
		this.nodeID = nodeID;
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
					//Block the new connection to join in the middle of the game
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
	}

	/**
	 * Initializes the server socket and connection state of the server
	 */
	public synchronized boolean connect() {

		try {

			serverDetails = configFileReader.getClientDetails(mPlayer.getID());
			int port =  Integer.parseInt(serverDetails.get(1));
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
		mPlayerClientManager.notifyAllClients(object, ClientConnectionState.CONNECTED, MessageType.BCT);
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

	public int getID() {
		return nodeID;
	}

}
