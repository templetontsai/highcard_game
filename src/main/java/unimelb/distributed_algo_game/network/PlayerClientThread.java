/*
 * 
 */
package unimelb.distributed_algo_game.network;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import java.util.HashMap;
import java.util.Map;

import java.util.ArrayList;

import java.util.Timer;
import java.util.TimerTask;

import org.json.simple.JSONObject;

import unimelb.distributed_algo_game.network.BodyMessage.ACKCode;
import unimelb.distributed_algo_game.network.BodyMessage.MessageType;
import unimelb.distributed_algo_game.network.NetworkInterface.ClientConnectionState;
import unimelb.distributed_algo_game.network.gui.MainGamePanel;
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

	/** The JSON body message */
	private JSONObject mMessage = null;

	/** The boolean for running the client thread */
	private boolean isRunning = false;


	private PlayerClientManager mPlayerClientManager = null;

	private int clientNodeID = -1;

	private boolean isClientLockRound;

	private boolean isClientStillAvle = false;

	private GamePlayerInfo mGameDealerInfo = null;

	private GamePlayerInfo mGameClientInfo = null;
	
	private Timer checkNodeStillAliveTimer = null;
	
	private Timer checkClientStillAliveTimer = null;
	
	private Timer serverStillAliveTimer = null;

	private Card c;
	


	/**
	 * Instantiates a new player client thread.
	 *
	 * @param mSocket
	 *            the m socket
	 * @param clientID
	 *            the client id
	 */
	public PlayerClientThread(Socket mSocket, PlayerClientManager mPlayerClientManager, GamePlayerInfo mGameDealerInfo) {
		if (mSocket != null) {
			this.mSocket = mSocket;
		} else
			throw new NullPointerException();

		mLock = new Object();
		mMessage = new JSONObject();
		this.mPlayerClientManager = mPlayerClientManager;
		this.mGameDealerInfo = mGameDealerInfo;
		this.mGameClientInfo = new GamePlayerInfo();
	}

	/**
	 * Runs the main method of the client thread
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

			mObjectInputStream.close();
			mObjectOutputStream.close();

			mSocket.close();

			
			updateListAndGUI();
			System.out.println("Client closed");
		} catch (IOException ioe) {
			System.out.println("Client closed");
			updateListAndGUI();
			
		}
	}
	
	final class checkNodeStillAliveTimerTask extends TimerTask {

		@Override
		public void run() {
			
			synchronized (mLock) {
				isRunning = false;
			}
		}

	}
	/**
	 * This receives a still alive message from the client
	 *
	 */
	final class checkClientStillAliveTimerTask extends TimerTask {

		@Override
		public void run() {
			
			synchronized (mLock) {
				isRunning = false;
			}
		}

	}
	
	private void updateListAndGUI() {
		if(mPlayerClientManager.isDealer()) {
			
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
	
	final class StillAliveTimerTask extends TimerTask {

		@Override
		public void run() {
			System.out.println("sending still alve from " + mGameDealerInfo.getNodeID() + " to " + mGameClientInfo.getNodeID());
			sendStillAliveMessage();

		}

	}

	/**
	 * Sends still alive message to the client
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
				System.out.println("CON");

				this.mGameClientInfo = mBodyMessage.getGamePlayerInfo();
				clientNodeID = this.mGameClientInfo.getNodeID();

				connectionState = ClientConnectionState.CONNECTED;

				mBodyMessage = new BodyMessage(mGameDealerInfo, MessageType.ACK, ACKCode.NODE_ID_RECEIVED);
				mMessage.put("header", connectionState);
				mMessage.put("body", mBodyMessage);
				sendMessage(mMessage);
				// Start the still alive timer beacon to the leader
				serverStillAliveTimer = new Timer();
				serverStillAliveTimer.scheduleAtFixedRate(new StillAliveTimerTask(), 0, NetworkInterface.STILL_ALIVE_TIME_OUT);

			}

			break;
		// Used to acknowledge the server is still alive
		case ACK:
			ACKCode ackCode = (ACKCode) message;

			switch (ackCode) {
			case NODE_ID_RECEIVED:
				System.out.println("NODE_ID_RECEIVED ACK Message received from node" + mBodyMessage.getNodeID());
				
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
				checkNodeStillAliveTimer.schedule(new checkNodeStillAliveTimerTask(), NetworkInterface.STILL_ALIVE_ACK_TIME_OUT);

				System.out.println("Node: " + this.mGameClientInfo.getNodeID() + " is still playing");
				break;
			case CLIENT_STILL_ALIVE:
				if (checkClientStillAliveTimer != null)
					checkClientStillAliveTimer.cancel();
				checkClientStillAliveTimer = new Timer();
				checkClientStillAliveTimer.schedule(new checkClientStillAliveTimerTask(), NetworkInterface.STILL_ALIVE_ACK_TIME_OUT);

				System.out.println("Node: " + this.mGameClientInfo.getNodeID() + " is still playing");
				break;
			default:
				System.out.println("Uknown ACK code");

			}
			break;
		// Used to send a card to the client after receiving a request message
		case CRD:

			connectionState = ClientConnectionState.CONNECTED;
			// Player specifies the card to, blocking for a bit to test ricart algo
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
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
			while (mPlayerClientManager.isRequested() && mPlayerClientManager.getRequestedTimestamp() > (long) mBodyMessage.getMessage())
				;// wait till out of critical session

			mMessage.put("header", ClientConnectionState.CONNECTED);
			mMessage.put("body", new BodyMessage(mGameDealerInfo, MessageType.ACK, ACKCode.CRT_RPY));

			sendMessage(mMessage);
			break;
		default:

			System.out.println("Uknown Message Type");

		}
	}

	/**
	 * Sends message to a client.
	 *
	 * @param mGameSendDataObject
	 *            the m game send data object
	 */
	public void sendMessage(Object mGameSendDataObject) {

		try {
			if (mObjectOutputStream != null && mGameSendDataObject != null) {

				mObjectOutputStream.writeObject(mGameSendDataObject);
				mObjectOutputStream.flush();
				// TODO object has to be reset, otherwise the client won't
				// receive any new reference of object.
				// However, this might cause issue if the packet is lost in
				// between communication
				mObjectOutputStream.reset();
			}
		} catch (IOException ioe) {

			isRunning = false;
			System.out.println("Connection lost in sendMessage, node: " + this.mGameClientInfo.getNodeID());

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

			isRunning = false;
			System.out.println("Connection lost in receiveMessage client, node: " + this.mGameClientInfo.getNodeID());

		}

		return message;

	}

	/**
	 * This returns the client ID of this thread
	 * 
	 * @return
	 */
	public synchronized int getClientNodeID() {
		return mGameClientInfo.getNodeID();
	}

	/**
	 * This returns the lock round status of this thread
	 * 
	 * @return
	 */
	public synchronized boolean getClientStatus() {
		return isClientLockRound;
	}

	/**
	 * This sets the status of the client thread
	 * 
	 * @param isClientLockRound
	 */
	public synchronized void setClientStatus(boolean isClientLockRound) {
		this.isClientLockRound = isClientLockRound;
	}

	/**
	 * This returns the player information of this thread
	 * 
	 * @return
	 */
	public synchronized GamePlayerInfo getClientGamePlayerInfo() {
		return this.mGameClientInfo;
	}

}
