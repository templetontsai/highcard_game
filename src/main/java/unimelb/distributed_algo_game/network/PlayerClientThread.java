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

	/** The game server object */
	private GameServer mGameServer = null;

	private int clientNodeID = -1;

	private boolean isClientLockRound;

	private boolean isClientStillAvle = false;
	
	private GamePlayerInfo mGameDealerInfo = null;
	

	private GamePlayerInfo mGameClientInfo = null;
	
	private Timer timer = null;

	private Card c;


	/**
	 * Instantiates a new player client thread.
	 *
	 * @param mSocket
	 *            the m socket
	 * @param clientID
	 *            the client id
	 */
	public PlayerClientThread(Socket mSocket, GameServer mGameServer, GamePlayerInfo mGameDealerInfo) {
		if (mSocket != null) {
			this.mSocket = mSocket;
		} else
			throw new NullPointerException();

		mLock = new Object();
		mMessage = new JSONObject();
		this.mGameServer = mGameServer;
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
				case ACK:
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
			} else {
				timer = new Timer();
				timer.schedule(new StillAliveTimerTask(), NetworkInterface.STILL_ALIVE_TIME_OUT);
			}

		}

		// Close the input and output streams to the server
		try {
			mObjectInputStream.close();
			mObjectOutputStream.close();

			mSocket.close();
			if(timer != null){
				timer.cancel();
				if(!mGameServer.getIsLeader()){
					System.out.println("It's morphin time!");
					startElection();
				}	
			}

			System.out.println("Client closed");
		} catch (IOException ioe) {
			// Print out the details of the exception error
			if(timer != null)
				timer.cancel();
			ioe.printStackTrace();
		}
	}

	/**
	 * This receives a still alive message from the client
	 *
	 */
	final class StillAliveTimerTask extends TimerTask {

		@Override
		public void run() {
			
			synchronized (mLock) {
				isClientStillAvle = false;
				//mGameServer.removeClient(clientNodeID);
				isRunning = false;
				//System.out.println("Node:" + clientNodeID + " has left the game");
				if(!mGameServer.getIsLeader()){
					System.out.println("It's morphin time!");
					startElection();
				}
			}
		}

	}
	
	/**
	 * This starts an election on the server
	 */
	public void startElection(){
		mGameServer.startElection();
	}
	
	/**
	 * Sends still alive message to the client
	 */
	private void sendStillAliveMessage() {
		JSONObject mMessage = new JSONObject();
		BodyMessage mBodyMessage = new BodyMessage(this.mGameDealerInfo, MessageType.ACK,
				ACKCode.STILL_ALIVE);
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

				//clientNodeID = mBodyMessage.getNodeID();
				this.mGameClientInfo = mBodyMessage.getGamePlayerInfo();
				clientNodeID = this.mGameClientInfo.getNodeID();
				
				connectionState = ClientConnectionState.CONNECTED;
				// Player specifies the card to
				//mBodyMessage = new BodyMessage(this.nodeID, MessageType.ACK, ACKCode.NODE_ID_RECEIVED);
				mBodyMessage = new BodyMessage(mGameDealerInfo, MessageType.ACK, ACKCode.NODE_ID_RECEIVED);
				mMessage.put("header", connectionState);
				mMessage.put("body", mBodyMessage);
				sendMessage(mMessage);
				synchronized (mLock) {
					isClientStillAvle = true;
				}

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
				mGameServer.broadcastCards(playerCard);
				mGameServer.updateCard(c, mBodyMessage.getGamePlayerInfo().getNodeID());
				isClientLockRound = true;
				

				break;
			case STILL_ALIVE:
				synchronized (mLock) {
					isClientStillAvle = true;
				}
				System.out.println("Node: " + this.mGameClientInfo.getNodeID() + " is still playing");
				break;
			default:
				System.out.println("Uknown ACK code");

			}
			break;
		// Used to send a card to the client after receiving a request message
		case CRD:

			connectionState = ClientConnectionState.CONNECTED;
			// Player specifies the card to
			c = mGameServer.getCard(1);
			mGameServer.updatePlayerCard(mBodyMessage.getGamePlayerInfo().getNodeID(), c);
			//mBodyMessage = new BodyMessage(this.nodeID, MessageType.CRD, c);
			mBodyMessage = new BodyMessage(mGameDealerInfo, MessageType.CRD, c);

			mMessage.put("header", connectionState);
			mMessage.put("body", mBodyMessage);
			sendMessage(mMessage);
			break;
		// Used to send a disconnect message
		case DSC:
			System.out.println(mBodyMessage.getMessage());
			//TODO put this in the right place later on, when it is false the game is no longer locked
			isClientLockRound = false;
			break;
		case LST:
			System.out.println("Received client list: "+mBodyMessage.getMessage());
			updateClientList(mBodyMessage);
			break;
		case ELE:
			System.out.println("Received election message from "+mBodyMessage.getNodeID());
			sendElectionMessage(mBodyMessage);
			break;
		case COD:
			System.out.println("Received coordinator message from "+mBodyMessage.getNodeID());
			setNewCoordinator(mBodyMessage);
			break;
		default:
			System.out.println("Uknown Message Type");

		}
	}
 
	/**
	 * This updates the current list of players in the game
	 * @param mBodyMessage
	 */
	public void updateClientList(BodyMessage mBodyMessage){
		
		String clients = (String)mBodyMessage.getMessage();
		String[] clientList = clients.split("\n");
		ArrayList<String> gameClients = new ArrayList();
		
		for(int i=0; i < clientList.length; i++){
			String[] clientDetails = clientList[i].split(":");
			//Only maintain list of clients not yourself
			if(!clientDetails[0].equals(Integer.toString(mGameDealerInfo.getNodeID()))){
			    System.out.println(clientDetails[0]+"-"+mGameDealerInfo.getNodeID());
				gameClients.add(clientList[i]);
			}
		}
		System.out.println("My neighbor is: "+gameClients.get(0));
		System.out.println("Total added clients: "+gameClients.size());
		mGameServer.updateServerList(gameClients);
	}
	
	/**
	 * This sends an election message to the node's neighbor after comparing the received node
	 * ID to it's own
	 * @param mBodyMessage
	 */
    public synchronized void sendElectionMessage(BodyMessage mBodyMessage){
    	int messageNodeID = Integer.parseInt((String)mBodyMessage.getMessage());
    	//Send message to the next node without changing it
		if(messageNodeID > this.mGameDealerInfo.getNodeID()){
			//System.out.println(mGameDealerInfo.getNodeID()+" cannot be the new dealer");
		}else if(messageNodeID < this.mGameDealerInfo.getNodeID()){
			//Replace the node ID in the message with own
			//System.out.println("I will become the new dealer "+this.mGameDealerInfo.getNodeID());
			mBodyMessage.setMessage(mGameDealerInfo.getStringNodeID());

		}else if(messageNodeID == this.mGameDealerInfo.getNodeID()){
			//This means i have received my election message and I am the new coordinator
			mGameServer.setPlayerDealer();
			mBodyMessage.setMessageType(MessageType.COD);
			mBodyMessage.setMessage(mGameDealerInfo);
			System.out.println("Hell ya I'm in charge now ");
		}
		
		JSONObject mMessage = new JSONObject();
		BodyMessage bodyMessage = mBodyMessage;
		mMessage.put("header", ClientConnectionState.CONNECTED);
		mMessage.put("body", bodyMessage);

		sendMessageToNext(mMessage);
		
	}
    
    /**
     * This sets the new coordinator of the game
     * @param mBodyMessage
     */
    public synchronized void setNewCoordinator(BodyMessage mBodyMessage){

    	GamePlayerInfo newDealer = (GamePlayerInfo)mBodyMessage.getMessage();
    	System.out.println("The new dealer is node "+newDealer.getNodeID());
		if(newDealer.getNodeID() != this.mGameDealerInfo.getNodeID()){
			//Update the new server details on the game client
			mGameServer.setGameServerLeader(newDealer);
			mGameServer.updateServerDetails();
			
			JSONObject mMessage = new JSONObject();
			BodyMessage bodyMessage = mBodyMessage;
			mBodyMessage.setMessageType(MessageType.COD);
			mMessage.put("header", ClientConnectionState.CONNECTED);
			mMessage.put("body", bodyMessage);
			sendMessageToNext(mMessage);
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
			// Print out the details of the exception error
			mGameServer.removeClient(this.mGameClientInfo.getNodeID());
			System.out.println("Connection lost in sendMessage, node: " + this.mGameDealerInfo.getNodeID());
			isRunning = false;
			if(timer != null)
				timer.cancel();
			ioe.printStackTrace();
		}

	}
	
	/**
	 * This sends a message to the next player in the logical ring
	 */
	public synchronized void sendMessageToNext(JSONObject mMessage){
		mGameServer.sendMessageToNext(mMessage);
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
			System.out.println("Connection lost in receiveMessage, node: " + this.mGameDealerInfo.getNodeID());
			isRunning = false;
			if(timer != null)
				timer.cancel();
			//ioe.printStackTrace();
		}

		return message;

	}
	
	/**
	 * This returns the client ID of this thread
	 * @return
	 */
	public synchronized int getClientNodeID() {
		return mGameClientInfo.getNodeID();
	}

	/**
	 * This returns the lock round status of this thread
	 * @return
	 */
	public synchronized boolean getClientStatus() {
		return isClientLockRound;
	}

	/**
	 * This sets the status of the client thread
	 * @param isClientLockRound
	 */
	public synchronized void setClientStatus(boolean isClientLockRound) {
		this.isClientLockRound = isClientLockRound;
	}
	
	/**
	 * This returns the player information of this thread
	 * @return
	 */
	public synchronized GamePlayerInfo getClientGamePlayerInfo() {
		return this.mGameClientInfo;
	}



}
