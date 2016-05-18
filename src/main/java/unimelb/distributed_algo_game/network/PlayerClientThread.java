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
import unimelb.distributed_algo_game.network.NetworkInterface.ClientConnectionState;
import unimelb.distributed_algo_game.player.GamePlayerInfo;
import unimelb.distributed_algo_game.pokers.Card;

// TODO: Auto-generated Javadoc
/**
 * The Class PlayerClientThread.
 *
 * @author Ting-Ying Tsai
 */
public class PlayerClientThread extends Thread {

	/** The m socket. */
	private Socket mSocket = null;

	/** The m object output stream. */
	private ObjectOutputStream mObjectOutputStream = null;

	/** The m object input stream. */
	private ObjectInputStream mObjectInputStream = null;

	/** The m lock. */
	private Object mLock = null;

	/** The JSON body message. */
	private JSONObject mMessage = null;

	/** The boolean for running the client thread. */
	private boolean isRunning = false;

	/** The m player client manager. */
	private PlayerClientManager mPlayerClientManager = null;

	/** The client node id. */
	private int clientNodeID = -1;

	/** The is client lock round. */
	private boolean isClientLockRound;

	/** The m game dealer info. */
	private GamePlayerInfo mGameDealerInfo = null;

	/** The m game client info. */
	private GamePlayerInfo mGameClientInfo = null;

	/** The check node still alive timer. */
	private Timer checkNodeStillAliveTimer = null;

	/** The check client still alive timer. */
	private Timer checkClientStillAliveTimer = null;

	/** The server still alive timer. */
	private Timer serverStillAliveTimer = null;

	/** The c. */
	private Card c = null;

	/** The is dealer ss. */
	private boolean isDealerSS = false;

	/** The new dealer. */
	private GamePlayerInfo newDealer = null;

	/** The is cs waiting. */
	private boolean isCSWaiting = false;

	/**
	 * Instantiates a new player client thread.
	 *
	 * @param mSocket
	 *            the m socket
	 * @param mPlayerClientManager
	 *            the m player client manager
	 * @param mGameDealerInfo
	 *            the m game dealer info
	 */
	public PlayerClientThread(Socket mSocket, PlayerClientManager mPlayerClientManager,
			GamePlayerInfo mGameDealerInfo) {
		if (mSocket != null) {
			this.mSocket = mSocket;
		} else
			throw new NullPointerException();

		mLock = new Object();
		mMessage = new JSONObject();
		this.mPlayerClientManager = mPlayerClientManager;
		this.mGameDealerInfo = mGameDealerInfo;
		this.mGameClientInfo = new GamePlayerInfo();
		this.isDealerSS = mPlayerClientManager.isDealerSS();

	}

	/**
	 * Runs the main method of the client thread.
	 */
	public void run() {

		isRunning = true;
		try {
			// Receives input and sends message using server socket
			mObjectInputStream = new ObjectInputStream(mSocket.getInputStream());
			mObjectOutputStream = new ObjectOutputStream(mSocket.getOutputStream());
		} catch (IOException ioe) {
			ioe.getStackTrace();
		}

		JSONObject m;
		BodyMessage bodyMessage;
		ClientConnectionState clientConnectionState;

		// Main loop to run the client thread
		while (isRunning) {

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
				case CONNECTING:
				case CONNECTED:

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
			if (serverStillAliveTimer != null)
				serverStillAliveTimer.cancel();

			mObjectInputStream.close();
			mObjectOutputStream.close();

			mSocket.close();
			if (isDealerSS)
				updateListAndGUI();
			System.out.println("Client closed");
		} catch (IOException ioe) {
			System.out.println("Client closed");
			if (isDealerSS)
				updateListAndGUI();

		}
	}

	/**
	 * Starts a timer task to check if a node is still alive.
	 *
	 * @author Lupiya
	 */
	final class checkNodeStillAliveTimerTask extends TimerTask {

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.util.TimerTask#run()
		 */
		@Override
		public void run() {

			synchronized (mLock) {
				isRunning = false;
			}
		}

	}

	/**
	 * This receives a still alive message from the client.
	 */
	final class checkClientStillAliveTimerTask extends TimerTask {

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.util.TimerTask#run()
		 */
		@Override
		public void run() {

			synchronized (mLock) {
				isRunning = false;
			}
		}

	}

	/**
	 * Updates the list used by the GUI.
	 */
	private void updateListAndGUI() {
		if (mPlayerClientManager.isDealer()) {

			mPlayerClientManager.removeNode(clientNodeID);
			mPlayerClientManager.broadcastUpdateNodeList();

			mPlayerClientManager.resetGameStart(mPlayerClientManager.getNumOfNodes());
			mPlayerClientManager.updateGameTable();

			System.out.println("Node:" + clientNodeID + " has left the game");
		} else {
			mPlayerClientManager.removeNode(clientNodeID);
			mPlayerClientManager.broadcastUpdateNodeList();
		}
	}

	/**
	 * Starts a still alive message timer task.
	 *
	 * @author Lupiya
	 */
	final class StillAliveTimerTask extends TimerTask {

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.util.TimerTask#run()
		 */
		@Override
		public void run() {
			// if(!isDealerSS)
			// System.out.println("sending still alive from " +
			// mGameDealerInfo.getNodeID() + " to " +
			// mGameClientInfo.getNodeID());
			sendStillAliveMessage();

		}

	}

	/**
	 * Sends still alive message to the client.
	 */
	private void sendStillAliveMessage() {
		JSONObject mMessage = new JSONObject();
		BodyMessage mBodyMessage = new BodyMessage(this.mGameDealerInfo, MessageType.ACK, ACKCode.SERVER_STILL_ALIVE);
		mMessage.put("header", ClientConnectionState.CONNECTED);
		mMessage.put("body", mBodyMessage);
		sendMessage(mMessage);
	}

	/**
	 * This method checks the type of JSON body message and carries out the
	 * necessary action for each message type.
	 *
	 * @param mBodyMessage
	 *            the m body message
	 */
	private synchronized void checkMessageType(BodyMessage mBodyMessage) {
		ClientConnectionState connectionState;
		MessageType messagType = mBodyMessage.getMessageType();
		Object message = mBodyMessage.getMessage();
		// System.out.println(messagType.toString() + " from" +
		// mBodyMessage.getGamePlayerInfo().getPort());
		switch (messagType) {
		case CON:
			// if the game is started already, don't respond to the message
			if (isDealerSS) {
				if (!getClientStatus()) {
					System.out.println("CON: DSS");

					this.mGameClientInfo = mBodyMessage.getGamePlayerInfo();
					clientNodeID = this.mGameClientInfo.getNodeID();

					connectionState = ClientConnectionState.CONNECTED;
					mGameDealerInfo.setServerTimeStamp(mPlayerClientManager.getServerTime());

					mBodyMessage = new BodyMessage(mGameDealerInfo, MessageType.ACK, ACKCode.NODE_ID_RECEIVED);
					mMessage.put("header", connectionState);
					mMessage.put("body", mBodyMessage);
					sendMessage(mMessage);

					// Start the still alive timer beacon to the leader

					serverStillAliveTimer = new Timer();
					serverStillAliveTimer.scheduleAtFixedRate(new StillAliveTimerTask(), 0,
							NetworkInterface.STILL_ALIVE_TIME_OUT);

				}
			} else {
				System.out.println("CON: PSS");

				this.mGameClientInfo = mBodyMessage.getGamePlayerInfo();
				clientNodeID = this.mGameClientInfo.getNodeID();

				connectionState = ClientConnectionState.CONNECTED;

				mBodyMessage = new BodyMessage(mGameDealerInfo, MessageType.ACK, ACKCode.NODE_ID_RECEIVED);
				mMessage.put("header", connectionState);
				mMessage.put("body", mBodyMessage);
				sendMessage(mMessage);
				// Start the still alive timer beacon to the leader

				serverStillAliveTimer = new Timer();
				serverStillAliveTimer.scheduleAtFixedRate(new StillAliveTimerTask(), 0,
						NetworkInterface.STILL_ALIVE_TIME_OUT);
			}

			break;
		// Used to acknowledge the server is still alive
		case ACK:
			ACKCode ackCode = (ACKCode) message;
			// System.out.println(ackCode.toString() + " from" +
			// mBodyMessage.getGamePlayerInfo().getPort());
			switch (ackCode) {
			case NODE_ID_RECEIVED:
				System.out.println("NODE_ID_RECEIVED ACK Message received from node" + mBodyMessage.getNodeID()
						+ " numOfNode: " + mPlayerClientManager.getNumOfNodes());

				break;
			case CARD_RECEIVED:
				Map<Integer, Card> playerCard = new HashMap<Integer, Card>(1);
				playerCard.put(mBodyMessage.getGamePlayerInfo().getNodeID(), c);
				mPlayerClientManager.broadcastNodeCard(playerCard);
				mPlayerClientManager.updateCard(c, mBodyMessage.getGamePlayerInfo().getNodeID());
				isClientLockRound = true;

				break;
			case NODE_STILL_ALIVE:
				if (checkNodeStillAliveTimer != null)
					checkNodeStillAliveTimer.cancel();
				checkNodeStillAliveTimer = new Timer();
				checkNodeStillAliveTimer.schedule(new checkNodeStillAliveTimerTask(),
						NetworkInterface.STILL_ALIVE_ACK_TIME_OUT);

				// System.out.println("Node: " +
				// this.mGameClientInfo.getNodeID() + " is still alive");
				break;
			case CLIENT_STILL_ALIVE:
				if (checkClientStillAliveTimer != null)
					checkClientStillAliveTimer.cancel();
				checkClientStillAliveTimer = new Timer();
				checkClientStillAliveTimer.schedule(new checkClientStillAliveTimerTask(),
						NetworkInterface.STILL_ALIVE_ACK_TIME_OUT);

				// System.out.println("Client: " +
				// this.mGameClientInfo.getNodeID() + " is still alive");
				break;
			case LEADER_ELE_ACK:
				System.out.println("LEADER_ELE_ACK");
				newDealer = (GamePlayerInfo) mBodyMessage.getGamePlayerInfo();
				System.out.println("The new dealer is node " + newDealer.getNodeID() + "," + newDealer.getIPAddress()
						+ "," + newDealer.getPort());
				mBodyMessage = new BodyMessage(mGameDealerInfo, MessageType.REINIT, "REINIT");

				mMessage.put("header", ClientConnectionState.CONNECTED);
				mMessage.put("body", mBodyMessage);
				sendMessage(mMessage);

				break;
			default:
				System.out.println("Uknown ACK code");

			}
			break;
		// Used to send a card to the client after receiving a request message
		case CRD:

			connectionState = ClientConnectionState.CONNECTED;

			c = mPlayerClientManager.getCard(1);
			mPlayerClientManager.updatePlayerCard(mBodyMessage.getGamePlayerInfo().getNodeID(), c);
			// mBodyMessage = new BodyMessage(this.nodeID, MessageType.CRD, c);
			mBodyMessage = new BodyMessage(mGameDealerInfo, MessageType.CRD, c);

			mMessage.put("header", connectionState);
			mMessage.put("body", mBodyMessage);
			sendMessage(mMessage);
			break;
		// Used to send a disconnect message
		case DSC:
			System.out.println(mBodyMessage.getMessage());
			// TODO put this in the right place later on, when it is false the
			// game is no longer locked
			isClientLockRound = false;
			break;
		case BCT_CRT:
			if (!mPlayerClientManager.isRequested()) {
				mMessage.put("header", ClientConnectionState.CONNECTED);
				mMessage.put("body", new BodyMessage(mGameDealerInfo, MessageType.ACK, ACKCode.CRT_RPY));
				sendMessage(mMessage);
				System.out.println("Received BCT_CRT and not requested for CST, Reply right away");
			} else {
				long broadcastTimestamp = (long) mBodyMessage.getMessage();
				System.out.println("Received broadcast timestamp: " + broadcastTimestamp
						+ "start to compare the timestamp with my requested timestamp");
				long requestedCSTimestamp = mPlayerClientManager.getRequestedTimestamp();
				if (broadcastTimestamp > requestedCSTimestamp) {
					// defer the reply and store in queue
					System.out.println("I request first add to queue and broadcast to them once I am done");
					mPlayerClientManager.addCRTRequestedQueue(mBodyMessage.getGamePlayerInfo().getNodeID());
				} else {
					mMessage.put("header", ClientConnectionState.CONNECTED);
					mMessage.put("body", new BodyMessage(mGameDealerInfo, MessageType.ACK, ACKCode.CRT_RPY));
					sendMessage(mMessage);
					System.out.println("node" + mBodyMessage.getGamePlayerInfo().getNodeID()
							+ "request CRT before me, Reply right away");

				}

			}

			break;
		case ELE:
			System.out.println("Received election message from " + mBodyMessage.getGamePlayerInfo().getNodeID());
			sendElectionMessage(mBodyMessage);
			break;
		case SRV_TIME:
			JSONObject mMessage = new JSONObject();
			mGameDealerInfo.setServerTimeStamp(mPlayerClientManager.getServerTime());
			BodyMessage bodyMessage = new BodyMessage(mGameDealerInfo, MessageType.ACK, ACKCode.SRV_TIME_ACK);
			mMessage.put("header", ClientConnectionState.CONNECTED);
			mMessage.put("body", bodyMessage);

			sendMessage(mMessage);
			break;

		default:

			System.out.println("Uknown Message Type");

		}
	}

	/**
	 * Checks if is CS waiting.
	 *
	 * @return true, if is CS waiting
	 */
	public synchronized boolean isCSWaiting() {
		return isCSWaiting;
	}

	/**
	 * Sets the checks if is cs waiting.
	 *
	 * @param isCSWaiting
	 *            the new checks if is cs waiting
	 */
	public synchronized void setIsCSWaiting(boolean isCSWaiting) {
		this.isCSWaiting = isCSWaiting;
	}

	/**
	 * This sends an election message to the node's neighbor after comparing the
	 * received node ID to it's own.
	 *
	 * @param mBodyMessage
	 *            the m body message
	 */
	public synchronized void sendElectionMessage(BodyMessage mBodyMessage) {
		int messageNodeID = Integer.parseInt((String) mBodyMessage.getMessage());
		// Send message to the next node without changing it
		if (messageNodeID > this.mGameDealerInfo.getNodeID()) {
			JSONObject mMessage = new JSONObject();
			BodyMessage bodyMessage = mBodyMessage;
			mMessage.put("header", ClientConnectionState.CONNECTED);
			mMessage.put("body", bodyMessage);

			sendMessage(mMessage);
		} else if (messageNodeID < this.mGameDealerInfo.getNodeID()) {
			// Don't forward to reduce number of messages

		} else if (messageNodeID == this.mGameDealerInfo.getNodeID()) {
			// This means i have received my election message and I am the new
			// coordinator

			mBodyMessage.setMessageType(MessageType.COD);
			mBodyMessage.setMessage(this.mGameDealerInfo);
			System.out.println("2Hell ya I'm in charge now ");

			JSONObject mMessage = new JSONObject();
			BodyMessage bodyMessage = mBodyMessage;
			mMessage.put("header", ClientConnectionState.CONNECTED);
			mMessage.put("body", bodyMessage);

			sendMessage(mMessage);
		}

	}

	/**
	 * Closes all the thread connections.
	 */
	public synchronized void closeConnection() {
		if (checkNodeStillAliveTimer != null) {
			checkNodeStillAliveTimer.cancel();
		}
		if (checkClientStillAliveTimer != null) {
			checkClientStillAliveTimer.cancel();
		}

		if (serverStillAliveTimer != null) {
			serverStillAliveTimer.cancel();
		}

		isRunning = false;
	}

	/**
	 * Sends message to a client.
	 *
	 * @param mGameSendDataObject
	 *            the m game send data object
	 */
	// public synchronized void sendMessage(Object mGameSendDataObject) {
	public synchronized void sendMessage(Object mGameSendDataObject) {

		try {
			if (mObjectOutputStream != null && mGameSendDataObject != null) {
				/*
				 * System.out.println("Client send message type " +
				 * ((BodyMessage) ((JSONObject)
				 * mGameSendDataObject).get("body")).getMessageType().toString()
				 * ); System.out.println("Client send message type " +
				 * ((BodyMessage) ((JSONObject)
				 * mGameSendDataObject).get("body")).getMessage().toString());
				 */
				mObjectOutputStream.writeObject(mGameSendDataObject);
				mObjectOutputStream.flush();
				// TODO object has to be reset, otherwise the client won't
				// receive any new reference of object.
				// However, this might cause issue if the packet is lost in
				// between communication
				mObjectOutputStream.reset();

				if (((BodyMessage) ((JSONObject) mGameSendDataObject).get("body"))
						.getMessageType() == MessageType.ACK.REINIT) {
					mPlayerClientManager.reInitGameAsPlayer(this.mGameDealerInfo, newDealer);
				}
			}
		} catch (IOException ioe) {

			isRunning = false;
			System.out.println("Connection lost in sendMessage, node: " + this.mGameClientInfo.getNodeID());
			ioe.printStackTrace();

		}

	}

	/**
	 * Receive message from the client.
	 *
	 * @return the object
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

			isRunning = false;
			ioe.printStackTrace();
			System.out.println("Connection lost in receiveMessage client, node: " + this.mGameClientInfo.getNodeID());

		}

		return message;

	}

	/**
	 * This returns the client ID of this thread.
	 *
	 * @return the client node id
	 */
	public synchronized int getClientNodeID() {
		return mGameClientInfo.getNodeID();
	}

	/**
	 * This returns the lock round status of this thread.
	 *
	 * @return the client status
	 */
	public synchronized boolean getClientStatus() {
		return isClientLockRound;
	}

	/**
	 * This sets the status of the client thread.
	 *
	 * @param isClientLockRound
	 *            the new client status
	 */
	public synchronized void setClientStatus(boolean isClientLockRound) {
		this.isClientLockRound = isClientLockRound;
	}

	/**
	 * This returns the player information of this thread.
	 *
	 * @return the client game player info
	 */
	public synchronized GamePlayerInfo getClientGamePlayerInfo() {
		return this.mGameClientInfo;
	}

}
