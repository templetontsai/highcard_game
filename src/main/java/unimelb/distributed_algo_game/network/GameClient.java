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
import unimelb.distributed_algo_game.network.gui.MainGamePanel;
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


	private Timer timer = null;
	
	private Timer checkServerTimer = null;

	private boolean isGameReady = false;

	private MainGamePanel mMainGameLoginClientPanel = null;

	private List<Integer> mNodeIDList = null;

	private List<GamePlayerInfo> mPlayerInfoList = null;

	private GameClientSocketManager mGameClientSocketManager = null;
	
	private boolean isReplied = false;
	
	private String ipAddress = null;
	
	private String port = null;

	private boolean isDealer = false;
	/**
	 * Instantiates a new game client.
	 */
	public GameClient(Player mPlayer) {
		if (mPlayer != null) {
			this.mPlayer = mPlayer;

		} else {
			System.out.println("Player can't be null");
			throw new NullPointerException();
		}
		mLock = new Object();
		clientConnectionState = ClientConnectionState.DISCONNECTED;
		mNodeIDList = new ArrayList<Integer>();
		mPlayerInfoList = new ArrayList<GamePlayerInfo>();

	}

	public GameClient(Player mPlayer, String ipAddress, String port, boolean isDealer) {
		if (mPlayer != null) {
			this.mPlayer = mPlayer;

		} else {
			System.out.println("Player can't be null");
			throw new NullPointerException();
		}
		mLock = new Object();
		clientConnectionState = ClientConnectionState.DISCONNECTED;
		mNodeIDList = new ArrayList<Integer>();
		mPlayerInfoList = new ArrayList<GamePlayerInfo>();
		this.ipAddress = ipAddress;
		this.port = port;
		this.isDealer = isDealer;
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

				mObjectOutputStream.close();
				mObjectInputStream.close();
				mSocket.close();
				System.out.println("conection closed");

			} catch (IOException ioe) {
				// TODO Adding error handling
				// ioe.printStackTrace();

			} catch (InterruptedException e) {
				// TODO Adding error handling
				// e.printStackTrace();

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
				System.out.println("ACK Message received from node" + mBodyMessage.getGamePlayerInfo().getNodeID());
				this.clientConnectionState = ClientConnectionState.CONNECTED;
				// Start the still alive timer beacon to the leader
				timer = new Timer();
				timer.scheduleAtFixedRate(new StillAliveTimerTask(), 0, NetworkInterface.STILL_ALIVE_TIME_OUT);
				break;

			case CARD_RECEIVED:
				break;
			case STILL_ALIVE:
				if (checkServerTimer != null)
					checkServerTimer.cancel();
				checkServerTimer = new Timer();
				checkServerTimer.schedule(new checkServerStillAliveTimerTask(), NetworkInterface.STILL_ALIVE_ACK_TIME_OUT);
				break;
			case CRT_RPY:
				System.out.println("CRT is replied");
				this.isReplied = true;
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
			for (Integer i : mNodeIDList) {
				Card card = playerCard.get(i);

				if (card != null && i != mPlayer.getGamePlayerInfo().getNodeID())
					mMainGameLoginClientPanel.updateCard(card, i);
			}
			break;
		case BCT_RST:
			int winnerID = (Integer) mBodyMessage.getMessage();
			mMainGameLoginClientPanel.declareWinner(winnerID);
			break;
		case BCT_RDY:
			System.out.println("Game is ready to play, start request card from dealer");
			// Init client socket manager here to connect to all the other nodes
			// before the game starts
			mGameClientSocketManager.setClientList(mPlayerInfoList);
			this.mGameClientSocketManager.initGameClientsConnection();

			isGameReady = ((Boolean) mBodyMessage.getMessage()).booleanValue();
			if (isGameReady) {
				
				mMainGameLoginClientPanel.showGameTable(true, mNodeIDList);
			}
			break;
		case BCT_LST:

			mPlayerInfoList = (List<GamePlayerInfo>) mBodyMessage.getMessage();
			updateNodeList(mPlayerInfoList);
			System.out.println("node" + mPlayer.getGamePlayerInfo().getNodeID() + " receives player list");

			break;
		case BCT_UPT:
			mPlayerInfoList = (List<GamePlayerInfo>) mBodyMessage.getMessage();
			updateNodeList(mPlayerInfoList);
			System.out.println("update list");
			mMainGameLoginClientPanel.updateGameTable(mNodeIDList);
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
		case ELE:
			System.out.println("Received election message from " + mBodyMessage.getNodeID());
			sendElectionMessage(mBodyMessage);
			break;
		case COD:
			System.out.println("Received coordinator message from " + mBodyMessage.getNodeID());
			setNewCoordinator(mBodyMessage);
			break;
		default:

			System.out.println("Uknown Message Type");

		}
	}

	private synchronized void updateNodeList(List<GamePlayerInfo> mPlayerInfoList) {
		mNodeIDList.clear();
		for (GamePlayerInfo info : mPlayerInfoList) {
			mNodeIDList.add(info.getNodeID());
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
	
	/**
	 * This receives a still alive message from the client
	 *
	 */
	final class checkServerStillAliveTimerTask extends TimerTask {

		@Override
		public void run() {

			synchronized (mLock) {
                System.out.println("The server left the game");
				checkServerTimer.cancel();
				startElection();
			}
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

			mSocket = new Socket(this.ipAddress, new Integer(this.port));
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

			timer.cancel();
			isRunning = false;
			mGameClientSocketManager.removeSocketClient(this);
			System.out.println("node has left game");

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
			
			System.out.println("ClassNotFoundException");
		} catch (IOException ioe) {
			
			isRunning = false;
			System.out.println("node has left game");
			
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
	 * Returns the details of the server the client connects to
	 * 
	 * @return
	 */
	public String getServerDetails() {
		return mPlayer.getGameServerInfo().getIPAddress() + ":" + mPlayer.getGameServerInfo().getPort();
	}

	/**
	 * Re-establishing a connection with the server
	 */
	public void reConnect() {
		mObjectOutputStream = null;
		mObjectInputStream = null;
		System.out.println("Attempting to connect to " + getServerDetails());
		connect();
		run();
		play();
	}

	public void setPanel(MainGamePanel mainGameLoginClientPanel) {

		this.mMainGameLoginClientPanel = mainGameLoginClientPanel;
	}

	public MainGamePanel getLoginPanel() {
		return mMainGameLoginClientPanel;
	}

	public void requestCard() {
		JSONObject mMessage = new JSONObject();
		BodyMessage bodyMessage = new BodyMessage(this.mPlayer.getGamePlayerInfo(), MessageType.CRD, "request a card");
		mMessage.put("header", ClientConnectionState.CONNECTED);
		mMessage.put("body", bodyMessage);

		sendMessage(mMessage);
	}

	public void setClientSocketManager(GameClientSocketManager mGameClientSocketManager) {
		this.mGameClientSocketManager = mGameClientSocketManager;
	}
	public boolean getReply() {
		return this.isReplied;
	}
	
	/**
	 * Starts a new leader election
	 */
	public void startElection(){
		System.out.println("Starting election");
		mGameClientSocketManager.startElection();
	}
	
	/**
	 * This sends an election message to the node's neighbor after comparing the
	 * received node ID to it's own
	 * 
	 * @param mBodyMessage
	 */
	public synchronized void sendElectionMessage(BodyMessage mBodyMessage) {
		int messageNodeID = Integer.parseInt((String) mBodyMessage.getMessage());
		// Send message to the next node without changing it
		if (messageNodeID > this.mPlayer.getGamePlayerInfo().getNodeID()) {
			// System.out.println(mGameDealerInfo.getNodeID()+" cannot be the
			// new dealer");
			JSONObject mMessage = new JSONObject();
			BodyMessage bodyMessage = mBodyMessage;
			mMessage.put("header", ClientConnectionState.CONNECTED);
			mMessage.put("body", bodyMessage);

			mGameClientSocketManager.sendElectionMessage(mMessage);
		} else if (messageNodeID < this.mPlayer.getGamePlayerInfo().getNodeID()) {
			// Don't forward to reduce number of messages

		} else if (messageNodeID == this.mPlayer.getGamePlayerInfo().getNodeID()) {
			// This means i have received my election message and I am the new
			// coordinator
			//mGameServer.setPlayerDealer();
			//mGameServer.disconnect();

			mBodyMessage.setMessageType(MessageType.COD);
			mBodyMessage.setMessage(this.mPlayer.getGamePlayerInfo());
			System.out.println("Hell ya I'm in charge now ");

			JSONObject mMessage = new JSONObject();
			BodyMessage bodyMessage = mBodyMessage;
			mMessage.put("header", ClientConnectionState.CONNECTED);
			mMessage.put("body", bodyMessage);

			mGameClientSocketManager.sendElectionMessage(mMessage);
		}

	}
	
	/**
	 * This sets the new coordinator of the game
	 * 
	 * @param mBodyMessage
	 */
	public synchronized void setNewCoordinator(BodyMessage mBodyMessage) {

		GamePlayerInfo newDealer = (GamePlayerInfo) mBodyMessage.getMessage();
		System.out.println("The new dealer is node " + newDealer.getNodeID());
		if (newDealer.getNodeID() != this.mPlayer.getGamePlayerInfo().getNodeID()) {
			// Update the new server details on the game client
			//mGameServer.setGameServerLeader(newDealer);

			JSONObject mMessage = new JSONObject();
			BodyMessage bodyMessage = mBodyMessage;
			mBodyMessage.setMessageType(MessageType.COD);
			mMessage.put("header", ClientConnectionState.CONNECTED);
			mMessage.put("body", bodyMessage);
			mGameClientSocketManager.sendElectionMessage(mMessage);
		}
	}
	
	public Player getPlayer(){
		return mPlayer;
	}
}
