package unimelb.distributed_algo_game.network;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.json.simple.JSONObject;

import unimelb.distributed_algo_game.network.BodyMessage.ACKCode;
import unimelb.distributed_algo_game.network.BodyMessage.MessageType;
import unimelb.distributed_algo_game.network.GameClient.StillAliveTimerTask;
import unimelb.distributed_algo_game.network.NetworkInterface.ClientConnectionState;
import unimelb.distributed_algo_game.player.GamePlayerInfo;
import unimelb.distributed_algo_game.pokers.Card;

/**
 * This thread is responsible for managing communication with the other servers
 * 
 * @author Lupiya
 *
 */
public class PlayerServerThread extends Thread {

	/** The m socket. */
	private Socket mSocket = null;

	/** The m object output stream. */
	private ObjectOutputStream mObjectOutputStream = null;

	/** The m object input stream. */
	private ObjectInputStream mObjectInputStream = null;

	/** The game server object */
	private GameServer mGameServer = null;

	/** The m lock. */
	private Object mLock = null;

	/** The JSON body message */
	private JSONObject mMessage = null;

	/** The boolean for running the client server thread */
	private boolean isRunning = false;

	/** The is client locked in a game round boolean */
	private boolean isClientLockRound;

	/** The is the client still alive in the game boolean */
	private boolean isClientStillAlive = false;

	/** The player information of the client server */
	private GamePlayerInfo mGameClientInfo = null;

	/** The player information of this server */
	private GamePlayerInfo mGameServerInfo = null;

	/** The connection state of the client of this thread */
	private ClientConnectionState clientConnectionState = null;

	/** The node ID of this thread */
	private int clientNodeID = -1;

	/** The timer to send periodic still alive messages to the client server */
	private Timer timer = null;

	private long timeStamp = -1;

	private boolean isReplied = false;

	/**
	 * Constructor for this thread
	 * 
	 * @param mGameServer
	 * @param mGameServerInfo
	 */
	public PlayerServerThread(GameServer mGameServer, GamePlayerInfo mGameServerInfo) {

		mLock = new Object();
		mMessage = new JSONObject();
		this.mGameServer = mGameServer;
		this.mGameServerInfo = mGameServerInfo;
		this.clientNodeID = mGameServerInfo.getNodeID();
		this.mGameClientInfo = mGameServerInfo;
		this.clientConnectionState = ClientConnectionState.DISCONNECTED;
	}

	/**
	 * Established connection with the client's server
	 */
	public void connect() {
		try {
			mSocket = new Socket(mGameClientInfo.getIPAddress(), Integer.parseInt(mGameClientInfo.getPort()));
			System.out.println("Server details: " + mGameClientInfo.getIPAddress() + " " + mGameClientInfo.getPort()
					+ "-" + mGameServer);
			this.clientConnectionState = ClientConnectionState.INIT;
		} catch (IOException ioe) {
			System.out.println("Can't reach the client's server");
			ioe.printStackTrace();
			mSocket = null;
		}
	}

	/**
	 * Runs the main method of the client thread
	 */
	public void run() {

		isRunning = true;
		try {
			mObjectOutputStream = new ObjectOutputStream(mSocket.getOutputStream());
			mObjectInputStream = new ObjectInputStream(mSocket.getInputStream());
			System.out.println("Slave Server connection is running");
		} catch (IOException ioe) {
			System.out.println("Can't reach the client's server");
			ioe.printStackTrace();
		}

		timer = new Timer();
		timer.scheduleAtFixedRate(new StillAliveTimerTask(), 5 * 1000, NetworkInterface.STILL_ALIVE_TIME_OUT);

		JSONObject m;
		BodyMessage bodyMessage;

		while (isRunning) {
			init();
			// Receive JSON message object from server
			m = (JSONObject) receiveMessage();

			// Only process the message if it's not null
			if (m != null) {

				// Get the client connection state and body from the message
				clientConnectionState = (ClientConnectionState) m.get("header");
				bodyMessage = (BodyMessage) m.get("body");

				switch (clientConnectionState) {

				// Process the message based on the connection state
				case INIT:
				case ACK:
				case CONNECTING:
				case CONNECTED:
					// System.out.println("connected from client");
					checkMessageType(bodyMessage);
					break;
				case DISCONNECTING:
				case DISCONNECTED:
					// System.out.println("disconnected from client");

					isRunning = false;
					break;
				default:
					System.out.println("Uknown State");
					break;

				}
			}
		}
		// Close the input and output streams to the server
		try {
			mObjectInputStream.close();
			mObjectOutputStream.close();

			mSocket.close();
			if (timer != null) {
				timer.cancel();
			}

			System.out.println("Client closed");
		} catch (IOException ioe) {
			// Print out the details of the exception error
			if (timer != null)
				timer.cancel();
			ioe.printStackTrace();
		}
	}

	/**
	 * Begins an election by sending a message containing this server's node ID
	 */
	public synchronized void startElection() {
		JSONObject mMessage = new JSONObject();
		BodyMessage mBodyMessage = new BodyMessage(mGameServerInfo.getNodeID(), MessageType.ELE,
				Integer.toString(mGameServerInfo.getNodeID()));
		mMessage.put("header", ClientConnectionState.CONNECTED);
		mMessage.put("body", mBodyMessage);
		boolean isConnectionActive = false;
		System.out.println("Sending election message");
		sendMessage(mMessage);
	}

	/**
	 * Sends a still alive message from the server to the client's server
	 */
	private void sendStillAliveMessage() {
		JSONObject mMessage = new JSONObject();
		BodyMessage mBodyMessage = new BodyMessage(mGameServerInfo, MessageType.ACK, ACKCode.STILL_ALIVE);
		mMessage.put("header", ClientConnectionState.CONNECTED);
		mMessage.put("body", mBodyMessage);
		sendMessage(mMessage);
	}

	/**
	 * Initiates a time task to send periodic still alive messages to the
	 * client's server
	 */
	final class StillAliveTimerTask extends TimerTask {

		@Override
		public void run() {

			sendStillAliveMessage();

		}

	}

	/**
	 * Initializes a connection with the client's server
	 */
	public void init() {
		if (this.clientConnectionState == ClientConnectionState.INIT) {
			JSONObject mMessage = new JSONObject();
			BodyMessage bodyMessage = new BodyMessage(mGameServerInfo, MessageType.CON, "init");
			mMessage.put("header", ClientConnectionState.CONNECTED);
			mMessage.put("body", bodyMessage);

			sendMessage(mMessage);
		}
	}

	/**
	 * This method sends a generic message object to the game server
	 */
	public void sendMessage(Object mGameSendDataObject) {

		try {

			if (mObjectOutputStream != null) {

				mObjectOutputStream.writeObject(mGameSendDataObject);
				mObjectOutputStream.flush();
				// mObjectOutputStream.reset();

			} else {
				System.out.println("Server output stream is null");
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
	 * Receive message from the client.
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
			// Print out the details of the exception error
			e.printStackTrace();
		} catch (IOException ioe) {
			// Print out the details of the exception error
			mGameServer.removeClient(this.mGameClientInfo.getNodeID());
			System.out.println("Connection lost in receiveMessage server, node: " + this.mGameServerInfo.getNodeID());
			isRunning = false;
			// ioe.printStackTrace();
		}

		return message;

	}

	/**
	 * This method checks the type of JSON body message and carries out the
	 * necessary action for each message type
	 * 
	 * @param mBodyMessage
	 */
	private synchronized void checkMessageType(BodyMessage mBodyMessage) {
		ClientConnectionState connectionState;
		MessageType messagType = mBodyMessage.getMessageType();
		Object message = mBodyMessage.getMessage();

		switch (messagType) {
		case CON:
			// if the game is started already, don't respond to the message
			if (!getClientStatus()) {

				// clientNodeID = mBodyMessage.getNodeID();
				this.mGameClientInfo = mBodyMessage.getGamePlayerInfo();
				clientNodeID = this.mGameClientInfo.getNodeID();

				connectionState = ClientConnectionState.CONNECTED;
				// Player specifies the card to
				// mBodyMessage = new BodyMessage(this.nodeID, MessageType.ACK,
				// ACKCode.NODE_ID_RECEIVED);
				mBodyMessage = new BodyMessage(mGameServerInfo, MessageType.ACK, ACKCode.NODE_ID_RECEIVED);
				mMessage.put("header", connectionState);
				mMessage.put("body", mBodyMessage);
				sendMessage(mMessage);
				synchronized (mLock) {
					isClientStillAlive = true;
				}

			}

			break;
		// Used to acknowledge the server is still alive
		case ACK:
			ACKCode ackCode = (ACKCode) message;

			switch (ackCode) {
			case NODE_ID_RECEIVED:
				System.out.println("NODE_ID_RECEIVED ACK Message received from node" + mBodyMessage.getNodeID());
				connectionState = ClientConnectionState.CONNECTED;
				break;
			case CARD_RECEIVED:
				// System.out.println("CARD_RECEIVED ACK Message received from
				// node" + mBodyMessage.getNodeID());
				isClientLockRound = true;

				break;
			case STILL_ALIVE:
				synchronized (mLock) {
					isClientStillAlive = true;
				}
				// System.out.println("Node: " +
				// this.mGameClientInfo.getNodeID() + " is still playing");
				break;
			case CRT_RPY:
				System.out.println("CRT is replied");
				this.isReplied = true;
				break;
			default:
				System.out.println("Uknown ACK code");

			}
			break;
		// Used to send a card to the client after receiving a request message
		case CRD:

			connectionState = ClientConnectionState.CONNECTED;
			// Player specifies the card to
			Card c = mGameServer.getCard(1);
			mGameServer.updatePlayerCard(mBodyMessage.getGamePlayerInfo().getNodeID(), c);
			// mBodyMessage = new BodyMessage(this.nodeID, MessageType.CRD, c);
			mBodyMessage = new BodyMessage(mGameServerInfo, MessageType.CRD, c);

			mMessage.put("header", connectionState);
			mMessage.put("body", mBodyMessage);
			sendMessage(mMessage);
			break;
		// Used to send send a broadcast message
		case BCT:
			System.out.println(mBodyMessage.getMessage());
			isClientLockRound = false;
			break;
		// Used to send a disconnect message
		case DSC:
			System.out.println(mBodyMessage.getMessage());
			break;
		case LST:
			System.out.println(mBodyMessage.getMessage());
			break;
		case ELE:
			System.out.println("Received election message from " + mBodyMessage.getNodeID());
			//sendElectionMessage(mBodyMessage);
			break;
		case COD:
			System.out.println("Received coordinator message from " + mBodyMessage.getNodeID());
			//setNewCoordinator(mBodyMessage);
			break;
		case BCT_CRT:
			System.out.println(mBodyMessage.getMessage());
			while (mGameServer.isRequested())
				;

			mMessage.put("header", ClientConnectionState.CONNECTED);
			mMessage.put("body", new BodyMessage(mGameServerInfo, MessageType.ACK, ACKCode.CRT_RPY));

			sendMessage(mMessage);

			break;
		case BCT_UPT:
			//playerIDList = (List<Integer>) mBodyMessage.getMessage();

			//mMainGameLoginClientPanel.updateGameTable(playerIDList);
			break;

		default:
			
			System.out.println("Uknown Message Type");

		}
	}



	/**
	 * This sets player information of this thread
	 * 
	 * @param gameClientInfo
	 */
	public void setGameClientInfo(GamePlayerInfo gameClientInfo) {
		this.mGameClientInfo = gameClientInfo;
	}

	/**
	 * This sets the server information of this thread
	 * 
	 * @param gameServerInfo
	 */
	public void setGameServerInfo(GamePlayerInfo gameServerInfo) {
		this.mGameServerInfo = gameServerInfo;
	}

	/**
	 * This returns the node ID of this thread
	 * 
	 * @return
	 */
	public synchronized int getClientNodeID() {
		return mGameClientInfo.getNodeID();
	}

	/**
	 * This returns the client status of this thread
	 * 
	 * @return
	 */
	public synchronized boolean getClientStatus() {
		return isClientLockRound;
	}

	/**
	 * This sets the client status of this thread
	 * 
	 * @param isClientLockRound
	 */
	public synchronized void setClientStatus(boolean isClientLockRound) {
		this.isClientLockRound = isClientLockRound;
	}

	/**
	 * This returns the game player information of this thread
	 * 
	 * @return
	 */
	public synchronized GamePlayerInfo getClientGamePlayerInfo() {
		return this.mGameClientInfo;
	}

	public boolean getReply() {
		return this.isReplied;
	}

}
