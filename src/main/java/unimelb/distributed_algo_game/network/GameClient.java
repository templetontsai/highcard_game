/*
 * 
 */
package unimelb.distributed_algo_game.network;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.json.simple.JSONObject;

import unimelb.distributed_algo_game.network.BodyMessage.ACKCode;
import unimelb.distributed_algo_game.network.BodyMessage.MessageType;
import unimelb.distributed_algo_game.network.gui.MainGameLoginClientPanel;
import unimelb.distributed_algo_game.player.GamePlayerInfo;
import unimelb.distributed_algo_game.player.NetworkObserver;
import unimelb.distributed_algo_game.player.Player;
import unimelb.distributed_algo_game.pokers.Card;

// TODO: Auto-generated Javadoc
/**
 * The Class GameClient.
 *
 * @author Ting-Ying Tsai
 */
public final class GameClient implements Runnable, NetworkInterface {

	/** The instance. */
	private static GameClient instance = null;

	/** The m player. */
	private Player mPlayer = null;

	/** The m socket. */
	private Socket mSocket = null;

	/** The game send data object. */
	private Object gameSendDataObject = null;

	/** The game reveice data object. */
	private Object gameReveiceDataObject = null;

	/** The observers. */
	private List<NetworkObserver> observers = new ArrayList<NetworkObserver>();

	/** The m object output stream. */
	private ObjectOutputStream mObjectOutputStream = null;

	/** The m object input stream. */
	private ObjectInputStream mObjectInputStream = null;

	/** The m lock. */
	private Object mLock;

	/** The connection state. */
	private ClientConnectionState clientConnectionState;

	/** The connection state. */
	private ServerConnectionState serverConnectionState;

	/** The boolean for the client thread */
	private boolean isRunning = false;

	private int serverPort;

	private String serverIPAddress;

	private Timer timer = null;

	private boolean isGameReady = false;

	private MainGameLoginClientPanel mMainGameLoginClientPanel = null;

	private List<Integer> playerIDList = null;

	/**
	 * Instantiates a new game client.
	 */
	protected GameClient() {
		mLock = new Object();
		clientConnectionState = ClientConnectionState.DISCONNECTED;

	}

	/**
	 * Gets the single instance of GameClient.
	 *
	 * @return single instance of GameClient
	 */
	public static GameClient getInstance() {
		if (instance == null) {
			instance = new GameClient();
		}
		return instance;
	}

	/**
	 * Sets the player.
	 *
	 * @param mPlayer
	 *            the new player
	 */
	public void setPlayer(Player mPlayer) {
		if (mPlayer != null) {
			this.mPlayer = mPlayer;

		} else {
			System.out.println("Player can't be null");
			throw new NullPointerException();
		}

	}

	/*
	 * Runs the thread for the game client
	 */
	public void run() {

		if (mSocket != null) {

			try {
				/**
				 * Read input stream from the server and write output stream to
				 * the server
				 */

				isRunning = true;

				/** Main while loop for the thread */
				while (isRunning) {
					/**
					 * Distinguish the function of the leader in game client and
					 * slave to the server
					 */
					if (mPlayer.isDealer()) {
						runLeaderState();
					} else {
						runSlaveState();
					}

					Thread.sleep(100);
				}

				/**
				 * Close socket connection and data streams once the main thread
				 * is no longer running
				 */
				System.out.println("conection closing...");
				mObjectOutputStream.close();
				mObjectInputStream.close();
				mSocket.close();
				timer.cancel();
			} catch (IOException ioe) {
				// TODO Adding error handling
				ioe.printStackTrace();
				timer.cancel();
			} catch (InterruptedException e) {
				// TODO Adding error handling
				e.printStackTrace();
				timer.cancel();
			}

		}
	}

	/** This runs the game client as a slave to the server */
	private void runSlaveState() throws IOException {

		JSONObject mMessage = (JSONObject) receiveMessage();

		if (mMessage != null) {
			ClientConnectionState connectionState = (ClientConnectionState) mMessage.get("header");
			BodyMessage bodyMessage = (BodyMessage) mMessage.get("body");
			switch (connectionState) {

			case ACK:

			case CONNECTING:
			case CONNECTED:

				checkMessageType(bodyMessage);

				break;
			case DISCONNECTING:
			case DISCONNECTED:

				isRunning = false;
				break;
			default:
				System.out.println("Uknown State");
			}
		} else {
			System.out.println("No longer communicating with " + mPlayer.getGameServerInfo().getIPAddress() + ":"
					+ mPlayer.getGameServerInfo().getPort());
		}

	}

	private void checkMessageType(BodyMessage mBodyMessage) {
		JSONObject mMessage = new JSONObject();
		MessageType messageType = mBodyMessage.getMessageType();
		switch (messageType) {
		case ACK:
			ACKCode ackCode = (ACKCode) mBodyMessage.getMessage();
			switch (ackCode) {
			case NODE_ID_RECEIVED:
				System.out.println(
						"ACK Message received from leader node" + mBodyMessage.getGamePlayerInfo().getNodeID());
				this.clientConnectionState = ClientConnectionState.CONNECTED;
				// Start the still alive timer beacon to the leader
				timer = new Timer();
				timer.scheduleAtFixedRate(new StillAliveTimerTask(), 0, NetworkInterface.STILL_ALIVE_TIME_OUT);
				break;

			case CARD_RECEIVED:
				break;
			default:
				System.out.println("Uknown ACK code");

			}

			break;
		case CRD:
			// TODO update this on GUI when GUI is ready
			System.out.println("The card you get is ");
			Card c = (Card) mBodyMessage.getMessage();
			c.showCard();
			mMainGameLoginClientPanel.updateCard(c, mPlayer.getGamePlayerInfo().getNodeID());
			// Notify the dealer the card has been received
			mMessage.put("header", ClientConnectionState.CONNECTED);
			mMessage.put("body",
					new BodyMessage(this.mPlayer.getGamePlayerInfo(), MessageType.ACK, ACKCode.CARD_RECEIVED));

			sendMessage(mMessage);

			break;
		case BCT_CRD:

			Map<Integer, Card> playerCard = (HashMap<Integer, Card>) mBodyMessage.getMessage();
			for (Integer i : playerIDList) {
				Card card = playerCard.get(i);

				if (card != null && i != mPlayer.getGamePlayerInfo().getNodeID())// no
																					// need
																					// to
																					// update
																					// my
																					// own
																					// card
					mMainGameLoginClientPanel.updateCard(card, i);
			}
			break;
		case BCT_RST:
			int winnerID = (Integer) mBodyMessage.getMessage();
			mMainGameLoginClientPanel.declareWinner(winnerID);
			break;
		case BCT_RDY:
			System.out.println("Game is ready to play, start request card from dealer");
			isGameReady = ((Boolean) mBodyMessage.getMessage()).booleanValue();
			if (isGameReady) {
				mMainGameLoginClientPanel.showGameTable(true, playerIDList);
			}
			break;
		case BCT_LST:

			playerIDList = (List<Integer>) mBodyMessage.getMessage();

			System.out.println("node" + mPlayer.getGamePlayerInfo().getNodeID() + " receives player list");
			System.out.println(mBodyMessage.getMessage());

			break;
		case BCT_UPT:
			playerIDList = (List<Integer>) mBodyMessage.getMessage();

			mMainGameLoginClientPanel.updateGameTable(playerIDList, false);
			break;
		case BCT_CRT:
			System.out.println(mBodyMessage.getMessage());
			mMessage.put("header", ClientConnectionState.CONNECTED);
			mMessage.put("body", new BodyMessage(this.mPlayer.getGamePlayerInfo(), MessageType.ACK, ACKCode.CRT_RPY));

			sendMessage(mMessage);
			break;
		case DSC:
			System.out.println(mBodyMessage.getMessage());
			break;
		default:
			
			System.out.println("Uknown Message Type");

		}
	}

	private void sendStillAliveMessage() {
		JSONObject mMessage = new JSONObject();
		BodyMessage mBodyMessage = new BodyMessage(this.mPlayer.getGamePlayerInfo(), MessageType.ACK,
				ACKCode.STILL_ALIVE);
		mMessage.put("header", ClientConnectionState.CONNECTED);
		mMessage.put("body", mBodyMessage);
		sendMessage(mMessage);
	}

	final class StillAliveTimerTask extends TimerTask {

		@Override
		public void run() {

			sendStillAliveMessage();

		}

	}

	/** Runs the game client as the leader of the game */
	private void runLeaderState() throws IOException {
		// Reads the JSON object to determine the action of the message
		JSONObject mMessage = new JSONObject();
		BodyMessage mBodyMessage;

		System.out.println("Leader state is now running in game client");
		switch (clientConnectionState) {

		case CONNECTING:
		case CONNECTED:
			// Ensure that the server is still running the game
			if (serverConnectionState != null) {
				switch (serverConnectionState) {
				// Acknowledgement that the server is still alive
				case ACK:

					mBodyMessage = new BodyMessage(this.mPlayer.getGamePlayerInfo(), MessageType.CRD,
							"get card request");
					mMessage.put("header", clientConnectionState);
					mMessage.put("body", mBodyMessage);
					sendMessage(mMessage);
					break;

				}
			}

			break;
		case DISCONNECTING:
			// Stop running the thread if the server disconnects from the client
		case DISCONNECTED:

			isRunning = false;
			break;
		default:
			System.out.println("Uknown State");
			break;

		}

	}

	/*
	 * Establishes connection with the server on the defined post on the local
	 * host
	 */
	public boolean connect() {

		try {

			mSocket = new Socket(serverIPAddress, serverPort);
			mObjectOutputStream = new ObjectOutputStream(mSocket.getOutputStream());
			mObjectInputStream = new ObjectInputStream(mSocket.getInputStream());
			clientConnectionState = ClientConnectionState.INIT;

		} catch (IOException ioe) {
			ioe.printStackTrace();
			mSocket = null;
			return false;
		}

		return true;
	}

	/**
	 * Changes state of the client in order to stop receiving messages from the
	 * server and be removed from the server thread pool
	 */
	public void disconnect() {
		System.out.println("Disconnecting from the game");
		clientConnectionState = ClientConnectionState.DISCONNECTED;
		isRunning = false;
		try {
			System.out.println("conection closing...");
			mObjectOutputStream.close();
			mObjectInputStream.close();
			mSocket.close();
			timer.cancel();
		} catch (IOException ioe) {
			// TODO Adding error handling
			ioe.printStackTrace();
			timer.cancel();
		}

	}

	/**
	 * This method sends a generic message object to the game server
	 */
	public void sendMessage(Object mGameSendDataObject) {

		try {

			if (mObjectOutputStream != null) {
				GamePlayerInfo info = (GamePlayerInfo) ((BodyMessage) ((JSONObject) mGameSendDataObject).get("body"))
						.getGamePlayerInfo();
				if (info != null) {
					// System.out.println("Client send message, timeStamp: " +
					// info.getTimeStamp());
					((BodyMessage) ((JSONObject) mGameSendDataObject).get("body")).getGamePlayerInfo().setTimeStamp();
					mObjectOutputStream.writeObject(mGameSendDataObject);
					mObjectOutputStream.flush();
					mObjectOutputStream.reset();
				}

			} else {
				System.out.println("mObjectOutputStream is null");
			}
		} catch (IOException ioe) {
			// TODO Adding Error Handling
			isRunning = false;
			System.out.println("Leader has gone haywire");
			ioe.printStackTrace();
			timer.cancel();
		}

	}

	/**
	 * This method receives messages sent by the server of generic object type
	 */
	public Object receiveMessage() {

		Object message = null;

		try {
			if (mObjectInputStream != null) {
				message = mObjectInputStream.readObject();
			}
		} catch (EOFException e) {

			return null;

		} catch (ClassNotFoundException e) {
			// Print the details of the exception error
			e.printStackTrace();
		} catch (IOException ioe) {
			// Print the details of the exception error
			isRunning = false;
			System.out.println("Leader has gone haywire");
			ioe.printStackTrace();
		}

		return message;

	}

	/**
	 * Attaches the player to the network observer
	 */
	public void attachPlayer(NetworkObserver observer) {
		observers.add(observer);
	}

	/**
	 * Removes the player from the network observer
	 */
	public void dettachPlayer(NetworkObserver observer) {
		observers.remove(observer);
	}

	/**
	 * Notify all observers.
	 */
	private void notifyAllObservers() {
		for (NetworkObserver observer : observers) {
			observer.update();
		}

	}

	/**
	 * This plays the game with the server
	 */
	public void play() {

		if (this.clientConnectionState == ClientConnectionState.INIT) {

			JSONObject mMessage = new JSONObject();
			BodyMessage bodyMessage = new BodyMessage(this.mPlayer.getGamePlayerInfo(), MessageType.CON, "init");
			mMessage.put("header", ClientConnectionState.CONNECTED);
			mMessage.put("body", bodyMessage);
			System.out.println("client play");
			sendMessage(mMessage);

		}
	}

	/**
	 * This sets the port and IP address for the server stored in the player
	 * object
	 */
	public void setServerDetails() {

		serverPort = Integer.parseInt(mPlayer.getGameServerInfo().getPort());
		serverIPAddress = mPlayer.getGameServerInfo().getIPAddress();
	}

	/**
	 * Returns the details of the server the client connects to
	 * 
	 * @return
	 */
	public String getServerDetails() {
		return mPlayer.getGameServerInfo().getIPAddress() + ":" + mPlayer.getGameServerInfo().getPort();
	}


	public void setPanel(MainGameLoginClientPanel mainGameLoginClientPanel) {
		this.mMainGameLoginClientPanel = mainGameLoginClientPanel;
	}

	public MainGameLoginClientPanel getLoginPanel() {
		return mMainGameLoginClientPanel;
	}

	public void requestCard() {
		JSONObject mMessage = new JSONObject();
		BodyMessage bodyMessage = new BodyMessage(this.mPlayer.getGamePlayerInfo(), MessageType.CRD, "request a card");
		mMessage.put("header", ClientConnectionState.CONNECTED);
		mMessage.put("body", bodyMessage);

		sendMessage(mMessage);
	}

}
