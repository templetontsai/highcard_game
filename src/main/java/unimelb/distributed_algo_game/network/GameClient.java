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
import unimelb.distributed_algo_game.network.utils.Utils;
import unimelb.distributed_algo_game.player.GamePlayerInfo;
import unimelb.distributed_algo_game.player.Player;
import unimelb.distributed_algo_game.pokers.Card;

// TODO: Auto-generated Javadoc
/**
 * The Class GameClient.
 *
 * @author Ting-Ying Tsai
 */
public final class GameClient implements Runnable, NetworkInterface {

	/** The m player. */
	private Player mPlayer = null;

	/** The m socket. */
	private Socket mSocket = null;

	/** The m object output stream. */
	private ObjectOutputStream mObjectOutputStream = null;

	/** The m object input stream. */
	private ObjectInputStream mObjectInputStream = null;

	/** The m lock. */
	private Object mLock;

	/** The boolean for the client thread. */
	private boolean isRunning = false;

	/** The send still alive timer. */
	private Timer sendStillAliveTimer = null;

	/** The check server timer. */
	private Timer checkServerTimer = null;

	/** The is game ready. */
	private boolean isGameReady = false;

	/** The m main game login client panel. */
	private MainGamePanel mMainGameLoginClientPanel = null;

	/** The m node id list. */
	private List<Integer> mNodeIDList = null;

	/** The m player info list. */
	private List<GamePlayerInfo> mPlayerInfoList = null;

	/** The m game client socket manager. */
	private GameClientSocketManager mGameClientSocketManager = null;

	/** The is replied. */
	private boolean isReplied = false;

	/** The ip address. */
	private String ipAddress = null;

	/** The port. */
	private String port = null;

	/** The is dealer cs. */
	private boolean isDealerCS = false;

	/** The player ss node id. */
	private int playerSSNodeID = -1;

	/** The cs timestamp start. */
	private long csTimestampStart = -1;

	/** The cs timestamp return. */
	private long csTimestampReturn = -1;

	/** The server time stamp. */
	private long serverTimeStamp = -1;

	/**
	 * Instantiates a new game client.
	 *
	 * @param mPlayer
	 *            the m player
	 */
	public GameClient(Player mPlayer) {
		if (mPlayer != null) {
			this.mPlayer = mPlayer;

		} else {
			System.out.println("Player can't be null");
			throw new NullPointerException();
		}
		mLock = new Object();

		mNodeIDList = new ArrayList<Integer>();
		mPlayerInfoList = new ArrayList<GamePlayerInfo>();

	}

	/**
	 * Constructor for the game client.
	 *
	 * @param mPlayer
	 *            the m player
	 * @param ipAddress
	 *            the ip address
	 * @param port
	 *            the port
	 * @param isDealerCS
	 *            the is dealer cs
	 */
	public GameClient(Player mPlayer, String ipAddress, String port, boolean isDealerCS) {
		if (mPlayer != null) {
			this.mPlayer = mPlayer;

		} else {
			System.out.println("Player can't be null");
			throw new NullPointerException();
		}
		mLock = new Object();

		mNodeIDList = new ArrayList<Integer>();
		mPlayerInfoList = new ArrayList<GamePlayerInfo>();
		this.ipAddress = ipAddress;
		this.port = port;
		this.isDealerCS = isDealerCS;
		this.csTimestampStart = Utils.getProcessTimestamp();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Runnable#run()
	 */
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
					Thread.sleep(2000);
					runState();

				}

				/**
				 * Close socket connection and data streams once the main thread
				 * is no longer running
				 */
				if (sendStillAliveTimer != null)
					sendStillAliveTimer.cancel();

				mObjectOutputStream.close();
				mObjectInputStream.close();
				mSocket.close();
				System.out.println("conection closed");

			} catch (IOException ioe) {
				if (!isDealerCS) {
					System.out.println("player cient socket closed due to player server socket is gone");
					mGameClientSocketManager.removeSocketClient(this);
				} else
					System.out.println("conection closed");

			} catch (InterruptedException e) {
				if (!isDealerCS) {
					System.out.println("player cient socket closed due to player server socket is gone");
					mGameClientSocketManager.removeSocketClient(this);
				} else
					System.out.println("conection closed");
			}

		}
	}

	/**
	 * This runs the game client as a slave to the server.
	 *
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	private void runState() throws IOException {

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

	/**
	 * Check message type.
	 *
	 * @param mBodyMessage
	 *            the m body message
	 */
	private void checkMessageType(BodyMessage mBodyMessage) {
		JSONObject mMessage = new JSONObject();
		MessageType messageType = mBodyMessage.getMessageType();
		switch (messageType) {
		case ACK:
			ACKCode ackCode = (ACKCode) mBodyMessage.getMessage();
			// System.out.println("ACKCode:" + ackCode);
			switch (ackCode) {
			case NODE_ID_RECEIVED:
				System.out.println("ACK Message received from node" + mBodyMessage.getGamePlayerInfo().getNodeID()
						+ " NODE_ID_RECEIVED");

				// Start the still alive timer beacon to the leader
				sendStillAliveTimer = new Timer();
				sendStillAliveTimer.scheduleAtFixedRate(new StillAliveTimerTask(), 0,
						NetworkInterface.STILL_ALIVE_TIME_OUT);
				break;

			case CARD_RECEIVED:
				break;
			case SERVER_STILL_ALIVE:
				if (checkServerTimer != null)
					checkServerTimer.cancel();
				checkServerTimer = new Timer();
				checkServerTimer.schedule(new checkServerStillAliveTimerTask(),
						NetworkInterface.STILL_ALIVE_ACK_TIME_OUT);
				// if(!isDealerCS)
				// System.out.println(mBodyMessage.getGamePlayerInfo().getPort()
				// + " am still alive");
				break;
			case CRT_RPY:
				System.out.println("CRT is replied");
				this.isReplied = true;
				break;
			case SRV_TIME_ACK:
				if (isDealerCS) {
					this.serverTimeStamp = mBodyMessage.getGamePlayerInfo().getServerTimeStamp();
					this.csTimestampReturn = Utils.getProcessTimestamp();
					long delta = this.serverTimeStamp + (this.csTimestampStart - this.csTimestampReturn) / 2;
					System.out.println("Server Time Received: " + this.serverTimeStamp);
					long csRequestedTimestamp = Utils.getProcessTimestamp() - delta;

					mGameClientSocketManager.setIsCRTRequested(true, csRequestedTimestamp);
					mGameClientSocketManager.broadcastCRT(csRequestedTimestamp);
				}
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
			// Client Socket1 to reply I am out of CRT, broadcast message to all
			// the deferred node in list
			mGameClientSocketManager.broadcastCRTisFree();

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
			if (isDealerCS) {
				System.out.println("Game is ready, initGameClientsConnection");
				mGameClientSocketManager.initGameClientsConnection();
			}
			isGameReady = ((Boolean) mBodyMessage.getMessage()).booleanValue();
			if (isGameReady) {

				mMainGameLoginClientPanel.showGameTable(true, mNodeIDList);
			}
			break;
		case BCT_NODE_LST:

			mPlayerInfoList = (List<GamePlayerInfo>) mBodyMessage.getMessage();
			updateNodeList(mPlayerInfoList);
			System.out.println("BCT_NODE_LST: node" + mPlayer.getGamePlayerInfo().getNodeID() + " receives player list:"
					+ mPlayerInfoList.size());

			for (GamePlayerInfo info : mPlayerInfoList) {

				mGameClientSocketManager.addSocketClient(info);
			}

			break;
		case BCT_NODE_UPT:
			mPlayerInfoList = (List<GamePlayerInfo>) mBodyMessage.getMessage();
			updateNodeList(mPlayerInfoList);
			System.out.println("BCT_NODE_UPT: node" + mPlayer.getGamePlayerInfo().getNodeID() + " update list");
			mMainGameLoginClientPanel.updateGameTable(mNodeIDList);
			break;

		case BCT_CRT:
			System.out.println(mBodyMessage.getMessage());
			mMessage.put("header", ClientConnectionState.CONNECTED);
			mMessage.put("body", new BodyMessage(this.mPlayer.getGamePlayerInfo(), MessageType.ACK, ACKCode.CRT_RPY));

			sendMessage(mMessage);
			break;
		case BCT_CRT_FREE:
			System.out.println(mBodyMessage.getMessage());
			break;
		case DSC:
			System.out.println(mBodyMessage.getMessage());
			break;
		case ELE:
			System.out.println("Received election message from " + this.mPlayer.getGamePlayerInfo().getNodeID());
			sendElectionMessage(mBodyMessage);
			break;
		case REINIT:
			System.out.println("Reinit received");
			// TODO start the process to become dealer
			System.out.println(" start the process to become dealer");
			System.out.println("The new dealer is node " + this.mPlayer.getGamePlayerInfo().getNodeID() + ","
					+ this.mPlayer.getGamePlayerInfo().getIPAddress() + ","
					+ this.mPlayer.getGamePlayerInfo().getPort());
			mGameClientSocketManager.reInitGameAsDealer(this.mPlayer.getGamePlayerInfo());
			break;

		default:

			System.out.println("Uknown Message Type");

		}
	}

	/**
	 * Send request server time.
	 */
	public void sendRequestServerTime() {
		this.csTimestampStart = Utils.getProcessTimestamp();
		JSONObject mMessage = new JSONObject();
		BodyMessage bodyMessage = new BodyMessage(this.mPlayer.getGamePlayerInfo(), MessageType.SRV_TIME,
				"Request Server Time");
		mMessage.put("header", ClientConnectionState.CONNECTED);
		mMessage.put("body", bodyMessage);

		sendMessage(mMessage);
	}

	/**
	 * This updates the node list.
	 *
	 * @param mPlayerInfoList
	 *            the m player info list
	 */
	private synchronized void updateNodeList(List<GamePlayerInfo> mPlayerInfoList) {
		mNodeIDList.clear();
		for (GamePlayerInfo info : mPlayerInfoList) {
			mNodeIDList.add(info.getNodeID());
		}
	}

	/**
	 * This method sends a still alive message to the dealer.
	 */
	private void sendStillAliveMessage() {
		if (isDealerCS) {
			JSONObject mMessage = new JSONObject();
			BodyMessage mBodyMessage = new BodyMessage(this.mPlayer.getGamePlayerInfo(), MessageType.ACK,
					ACKCode.NODE_STILL_ALIVE);
			mMessage.put("header", ClientConnectionState.CONNECTED);
			mMessage.put("body", mBodyMessage);
			sendMessage(mMessage);
		} else {
			JSONObject mMessage = new JSONObject();
			BodyMessage mBodyMessage = new BodyMessage(this.mPlayer.getGamePlayerInfo(), MessageType.ACK,
					ACKCode.CLIENT_STILL_ALIVE);
			mMessage.put("header", ClientConnectionState.CONNECTED);
			mMessage.put("body", mBodyMessage);
			sendMessage(mMessage);
		}

	}

	/**
	 * Creates a new still alive timer task.
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

			sendStillAliveMessage();

		}

	}

	/**
	 * This receives a still alive message from the client.
	 */
	final class checkServerStillAliveTimerTask extends TimerTask {

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.util.TimerTask#run()
		 */
		@Override
		public void run() {

			synchronized (mLock) {
				if (isDealerCS) {
					System.out.println("The dealer server socket left the game");
					checkServerTimer.cancel();
					startElection();
				} else {
					System.out.println("The player server socket left the game");

				}

			}
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see unimelb.distributed_algo_game.network.NetworkInterface#connect()
	 */
	/*
	 * Establishes connection with the server on the defined post on the local
	 * host
	 */
	public boolean connect() {

		try {
			System.out.println("Port is " + new Integer(this.port));
			mSocket = new Socket(this.ipAddress, new Integer(this.port));
			mObjectOutputStream = new ObjectOutputStream(mSocket.getOutputStream());
			mObjectInputStream = new ObjectInputStream(mSocket.getInputStream());
			isRunning = true;

		} catch (IOException ioe) {
			ioe.printStackTrace();
			mSocket = null;
			return false;
		}

		return true;
	}

	/**
	 * Changes state of the client in order to stop receiving messages from the
	 * server and be removed from the server thread pool.
	 */
	public void disconnect() {
		System.out.println("Disconnecting from the game");
		if (sendStillAliveTimer != null) {
			sendStillAliveTimer.cancel();
		}

		if (checkServerTimer != null) {
			checkServerTimer.cancel();
		}
		isRunning = false;
	}

	/**
	 * This method sends a generic message object to the game server.
	 *
	 * @param mGameSendDataObject
	 *            the m game send data object
	 */
	public void sendMessage(Object mGameSendDataObject) {

		try {

			if (mObjectOutputStream != null) {
				GamePlayerInfo info = (GamePlayerInfo) ((BodyMessage) ((JSONObject) mGameSendDataObject).get("body"))
						.getGamePlayerInfo();

				// System.out.println("Client send message, timeStamp: " +
				// info.getTimeStamp());

				mObjectOutputStream.writeObject(mGameSendDataObject);
				mObjectOutputStream.flush();
				mObjectOutputStream.reset();

			} else {
				System.out.println("mObjectOutputStream is null");
			}
		} catch (IOException ioe) {
			ioe.printStackTrace();
			isRunning = false;
			if (isDealerCS)
				System.out.println("dealer node has left game");
			else
				System.out.println("A player node has left game");

		}

	}

	/**
	 * This method receives messages sent by the server of generic object type.
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

			System.out.println("ClassNotFoundException");
		} catch (IOException ioe) {

			isRunning = false;
			if (isDealerCS)
				System.out.println("dealer node has left game");
			else
				System.out.println("A player node has left game");

		}

		return message;

	}

	/**
	 * This plays the game with the server.
	 */
	public void play() {

		if (isRunning) {

			this.csTimestampStart = Utils.getProcessTimestamp();
			JSONObject mMessage = new JSONObject();
			BodyMessage bodyMessage = new BodyMessage(this.mPlayer.getGamePlayerInfo(), MessageType.CON, "init");
			mMessage.put("header", ClientConnectionState.CONNECTED);
			mMessage.put("body", bodyMessage);
			System.out.println("client play");
			sendMessage(mMessage);

		}
	}

	/**
	 * Returns the details of the server the client connects to.
	 *
	 * @return the server details
	 */
	public String getServerDetails() {
		StringBuilder sb = new StringBuilder();
		sb.append(mPlayer.getGameServerInfo().getNodeID() + ":" + mPlayer.getGameServerInfo().getIPAddress() + ":"
				+ mPlayer.getGameServerInfo().getPort() + "\n");
		return sb.toString();

	}

	/**
	 * Re-establishing a connection with the server.
	 */
	public void reConnect() {
		mObjectOutputStream = null;
		mObjectInputStream = null;
		System.out.println("Attempting to connect to " + getServerDetails());
		connect();
		run();
		play();
	}

	/**
	 * Sets the client login panel.
	 *
	 * @param mainGameLoginClientPanel
	 *            the new panel
	 */
	public void setPanel(MainGamePanel mainGameLoginClientPanel) {

		this.mMainGameLoginClientPanel = mainGameLoginClientPanel;
	}

	/**
	 * Returns the client login panel.
	 *
	 * @return the login panel
	 */
	public MainGamePanel getLoginPanel() {
		return mMainGameLoginClientPanel;
	}

	/**
	 * Sends a card request message to all the players in the game.
	 */
	public void requestCard() {
		JSONObject mMessage = new JSONObject();
		BodyMessage bodyMessage = new BodyMessage(this.mPlayer.getGamePlayerInfo(), MessageType.CRD, "request a card");
		mMessage.put("header", ClientConnectionState.CONNECTED);
		mMessage.put("body", bodyMessage);

		sendMessage(mMessage);
	}

	/**
	 * Sets the client socket manager for this game client.
	 *
	 * @param mGameClientSocketManager
	 *            the new client socket manager
	 */
	public void setClientSocketManager(GameClientSocketManager mGameClientSocketManager) {
		this.mGameClientSocketManager = mGameClientSocketManager;
	}

	/**
	 * Returns the reply status of the mutual exclusion algorithm.
	 *
	 * @return the reply
	 */
	public boolean getReply() {
		return this.isReplied;
	}

	/**
	 * Starts a new leader election.
	 */
	public void startElection() {
		int pos = (getMyPositionInList() % mPlayerInfoList.size());
		int myNeighborID = mPlayerInfoList.get(pos).getNodeID();
		if (myNeighborID == mPlayer.getGameServerInfo().getNodeID())
			myNeighborID++;
		mGameClientSocketManager.startElection(myNeighborID);
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
		if (messageNodeID > this.mPlayer.getGamePlayerInfo().getNodeID()) {
			int pos = (getMyPositionInList() % mPlayerInfoList.size());
			int myNeighborID = mPlayerInfoList.get(pos).getNodeID();
			if (myNeighborID == mPlayer.getGameServerInfo().getNodeID())
				myNeighborID++;
			// System.out.println(mGameDealerInfo.getNodeID()+" cannot be the
			// new dealer");
			JSONObject mMessage = new JSONObject();
			BodyMessage bodyMessage = mBodyMessage;
			mMessage.put("header", ClientConnectionState.CONNECTED);
			mMessage.put("body", bodyMessage);

			mGameClientSocketManager.sendElectionMessage(mMessage, myNeighborID);

		} else if (messageNodeID == this.mPlayer.getGamePlayerInfo().getNodeID()) {
			// This means i have received my election message and I am the new
			// coordinator
			System.out.println("1Hell ya I'm in charge now ");

			JSONObject mMessage = new JSONObject();

			mBodyMessage = new BodyMessage(this.mPlayer.getGamePlayerInfo(), MessageType.ACK, ACKCode.LEADER_ELE_ACK);
			mMessage.put("header", ClientConnectionState.CONNECTED);
			mMessage.put("body", mBodyMessage);
			mGameClientSocketManager.sendCoordinatorMessage(mMessage);

		}

	}

	/**
	 * Returns the player of this game client.
	 *
	 * @return the player
	 */
	public Player getPlayer() {
		return mPlayer;
	}

	/**
	 * Sets the node ID for the game client server socket connection.
	 *
	 * @param playerSSNodeID
	 *            the new player ss node id
	 */
	public void setPlayerSSNodeID(int playerSSNodeID) {
		this.playerSSNodeID = playerSSNodeID;
	}

	/**
	 * Returns the ID node of the game client socket server connection.
	 *
	 * @return the player ss node id
	 */
	public int getPlayerSSNodeID() {
		return this.playerSSNodeID;
	}

	/**
	 * This method returns the player's position in the client list.
	 *
	 * @return the my position in list
	 */
	public int getMyPositionInList() {
		int pos = 0;
		int j = 0;
		for (GamePlayerInfo i : mPlayerInfoList) {
			if (i.getNodeID() == mPlayer.getGamePlayerInfo().getNodeID())
				pos = j;
			j++;
		}
		return pos + 1;
	}

	/**
	 * Gets the logic clock.
	 *
	 * @return the logic clock
	 */
	public synchronized long getLogicClock() {
		return this.serverTimeStamp + ((this.csTimestampStart - this.csTimestampReturn) / 2);
	}

}
