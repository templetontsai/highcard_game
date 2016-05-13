package unimelb.distributed_algo_game.network;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import org.json.simple.JSONObject;

import unimelb.distributed_algo_game.network.BodyMessage.ACKCode;
import unimelb.distributed_algo_game.network.BodyMessage.MessageType;
import unimelb.distributed_algo_game.network.GameClient.StillAliveTimerTask;
import unimelb.distributed_algo_game.network.NetworkInterface.ClientConnectionState;
import unimelb.distributed_algo_game.player.GamePlayerInfo;
import unimelb.distributed_algo_game.pokers.Card;

public class PlayerServerThread extends Thread{

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
	
	private boolean isClientLockRound;

	private boolean isClientStillAlive = false;
		
	private GamePlayerInfo mGameClientInfo = null;
	
	private GamePlayerInfo mGameServerInfo = null;
	
	private ClientConnectionState clientConnectionState = null;
	
	private int clientNodeID = -1;
	
	public PlayerServerThread(GameServer mGameServer, GamePlayerInfo mGameServerInfo){
		
		mLock = new Object();
		mMessage = new JSONObject();
		this.mGameServer = mGameServer;
		this.mGameServerInfo = mGameServerInfo;
		this.clientNodeID = mGameServerInfo.getNodeID();
		this.mGameClientInfo = mGameServerInfo;
		this.clientConnectionState = ClientConnectionState.DISCONNECTED;
	}
	
	public void connect(){
		try{
		    mSocket = new Socket(mGameClientInfo.getIPAddress(), Integer.parseInt(mGameClientInfo.getPort()));
		    System.out.println("Server details: "+mGameClientInfo.getIPAddress()+" "+mGameClientInfo.getPort()+"-"+mGameServer);
		    this.clientConnectionState = ClientConnectionState.INIT;
		}catch(IOException ioe){
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
		try{
			mObjectOutputStream = new ObjectOutputStream(mSocket.getOutputStream());
			mObjectInputStream = new ObjectInputStream(mSocket.getInputStream());
			System.out.println("Server connection is "+mObjectOutputStream);
		}catch(IOException ioe){
			System.out.println("Can't reach the client's server");
			ioe.printStackTrace();
		}
		
		Timer timer = new Timer();
		timer.scheduleAtFixedRate(new StillAliveTimerTask(), 0, NetworkInterface.STILL_ALIVE_TIME_OUT);	
		
		JSONObject m;
		BodyMessage bodyMessage;
		
	    while(isRunning){
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
	}
	
	public void startElection(){
		JSONObject mMessage = new JSONObject();
		BodyMessage mBodyMessage = new BodyMessage(mGameServerInfo.getNodeID(), MessageType.ELE,
				Integer.toString(mGameServerInfo.getNodeID()));
		mMessage.put("header", ClientConnectionState.CONNECTED);
		mMessage.put("body", mBodyMessage);
		boolean isConnectionActive = false;
		System.out.println("Waiting to send election message");
		while(!isConnectionActive){
			if(mObjectOutputStream!=null)
				isConnectionActive = true;
		}
		System.out.println("Sending election message");
		sendMessage(mMessage);
	}
	
	private void sendStillAliveMessage() {
		JSONObject mMessage = new JSONObject();
		BodyMessage mBodyMessage = new BodyMessage(mGameServerInfo, MessageType.ACK,
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
	
	public void init(){
		if(this.clientConnectionState == ClientConnectionState.INIT){
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
				mObjectOutputStream.reset();

			} else {
				System.out.println("Server output stream is null");
			}
		} catch (IOException ioe) {
			// TODO Adding Error Handling
			isRunning = false;
			System.out.println("Leader has gone haywire");
			ioe.printStackTrace();
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
			System.out.println("Connection lost in receiveMessage, node: " + this.mGameServerInfo.getNodeID());
			isRunning = false;
			//ioe.printStackTrace();
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

				//clientNodeID = mBodyMessage.getNodeID();
				this.mGameClientInfo = mBodyMessage.getGamePlayerInfo();
				clientNodeID = this.mGameClientInfo.getNodeID();
				
				connectionState = ClientConnectionState.CONNECTED;
				// Player specifies the card to
				//mBodyMessage = new BodyMessage(this.nodeID, MessageType.ACK, ACKCode.NODE_ID_RECEIVED);
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
				mGameServer.checkPlayerStatus();

				break;
			case STILL_ALIVE:
				synchronized (mLock) {
					isClientStillAlive = true;
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
			Card c = mGameServer.getCard(1);
			mGameServer.updatePlayerCard(mBodyMessage.getGamePlayerInfo().getNodeID(), c);
			//mBodyMessage = new BodyMessage(this.nodeID, MessageType.CRD, c);
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
			mBodyMessage.setGamePlayerInfo(mGameClientInfo);
			sendElectionMessage(mBodyMessage);
			break;
		case COD:
			mBodyMessage.setGamePlayerInfo(mGameClientInfo);
			setNewCoordinator(mBodyMessage);
			break;
		default:
			System.out.println("Uknown Message Type");

		}
	}
	
	/**
	 * This sends an election message to the node's neighbor SC
	 * @param mBodyMessage
	 */
    public void sendElectionMessage(BodyMessage mBodyMessage){
    	int messageNodeID = Integer.parseInt((String)mBodyMessage.getMessage());
    	//System.out.println("My ID is "+mGameServerInfo.getNodeID()+" and other is "+messageNodeID);
		if(messageNodeID > this.mGameServerInfo.getNodeID()){
			//Send message to the next node without changing it
			//System.out.println(mGameServerInfo.getNodeID()+" cannot be the new dealer");
		}else if(messageNodeID < this.mGameServerInfo.getNodeID()){
			//Replace the node ID in the message with own
			mBodyMessage.setMessage(mGameServerInfo.getStringNodeID());
           // System.out.println("I will become the next dealer "+mGameServerInfo.getStringNodeID());
			
		}else if(messageNodeID == this.mGameServerInfo.getNodeID()){
			//This means i have received my election message and I am the new coordinator
		    mGameServer.setPlayerDealer();
			mBodyMessage.setMessageType(MessageType.COD);
			mBodyMessage.setMessage(mGameServerInfo);
			System.out.println("Hell ya I'm in charge now ");
		}
		
		JSONObject mMessage = new JSONObject();
		BodyMessage bodyMessage = mBodyMessage;
		mMessage.put("header", ClientConnectionState.CONNECTED);
		mMessage.put("body", bodyMessage);

		sendMessage(mMessage);
	}
    
    /**
     * This sets the new coordinator of the game
     * @param mBodyMessage
     */
    public void setNewCoordinator(BodyMessage mBodyMessage){

    	GamePlayerInfo newDealer = (GamePlayerInfo)mBodyMessage.getMessage();
    	System.out.println("The new dealer is node "+newDealer.getNodeID()+" Game Server is "+mGameServer);
		if(newDealer.getNodeID() != this.mGameServerInfo.getNodeID()){
			mGameServer.setGameServerLeader(newDealer);
			
			System.out.println("The new dealer is "+newDealer.getNodeID());
			
			JSONObject mMessage = new JSONObject();
			BodyMessage bodyMessage = mBodyMessage;
			mBodyMessage.setMessageType(MessageType.COD);
			mMessage.put("header", ClientConnectionState.CONNECTED);
			mMessage.put("body", bodyMessage);
			sendMessage(mMessage);
		}
	}
	
	public void setGameClientInfo(GamePlayerInfo gameClientInfo){
		this.mGameClientInfo = gameClientInfo;
	}
	
	public void setGameServerInfo(GamePlayerInfo gameServerInfo){
		this.mGameServerInfo = gameServerInfo;
	}
	
	public synchronized int getClientNodeID() {
		return mGameClientInfo.getNodeID();
	}

	public synchronized boolean getClientStatus() {
		return isClientLockRound;
	}

	public synchronized void setClientStatus(boolean isClientLockRound) {
		this.isClientLockRound = isClientLockRound;
	}
	
	public synchronized GamePlayerInfo getClientGamePlayerInfo() {
		return this.mGameClientInfo;
	}
}
