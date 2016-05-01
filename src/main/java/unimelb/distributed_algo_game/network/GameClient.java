/*
 * 
 */
package unimelb.distributed_algo_game.network;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import org.json.simple.JSONObject;

import unimelb.distributed_algo_game.network.BodyMessage.MessageType;
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

	/**The JSON Message object to be sent. */
	private JSONObject mMessage = null;

	/** The boolean for the client thread */
	private boolean isRunning = false;

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
				/** Read input stream from the server and write output stream to the server */
				mObjectOutputStream = new ObjectOutputStream(mSocket.getOutputStream());
				mObjectInputStream = new ObjectInputStream(mSocket.getInputStream());
				isRunning = true;

				/** Main while loop for the thread */
				while (isRunning) {
					/** Distinguish the function of the leader in game client and slave to the server */
					if (mPlayer.isDealer()) {
						runLeaderState();
					} else {
						runSlaveState();
					}

					Thread.sleep(100);
				}
				
				/** Close socket connection and data streams once the main thread is no longer running */
				System.out.println("conection closing...");
				mObjectOutputStream.close();
				mObjectInputStream.close();
				mSocket.close();
			} catch (IOException ioe) {
				// TODO Adding error handling
				ioe.printStackTrace();
			} catch (InterruptedException e) {
				// TODO Adding error handling
				e.printStackTrace();
			}

		}
	}

	/** This runs the game client as a slave to the server */
	private void runSlaveState() throws IOException {
		// TODO here
		mMessage = new JSONObject();
		
		
		
		
		
		mMessage = (JSONObject) receiveMessage();
		
		if(mMessage != null) {
			clientConnectionState = (ClientConnectionState)mMessage.get("header");
			BodyMessage bodyMessage = (BodyMessage) mMessage.get("body");
			switch (clientConnectionState) {

			case CONNECTING:
				
				break;
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
		MessageType messageType = mBodyMessage.getMessageType();
		switch(messageType) {
		case ACK:
			System.out.println(mBodyMessage.getMessage());
			break;
		case CRD:
			System.out.println((Card)mBodyMessage.getMessage());
			break;
		case BCT:
			System.out.println(mBodyMessage.getMessage());
			break;
		case DSC:
			System.out.println(mBodyMessage.getMessage());
			break;
		
		}
	}


	/** Runs the game client as the leader of the game */
	private void runLeaderState() throws IOException {
		//Reads the JSON object to determine the action of the message
		JSONObject m = new JSONObject();
		String body = "";
		BodyMessage mBodyMessage;
		

		switch (clientConnectionState) {

		case CONNECTING:
		case CONNECTED:
			//Ensure that the server is still running the game
			if (serverConnectionState != null) {
				switch (serverConnectionState) {
				//Acknowledgement that the server is still alive
				case ACK:
					System.out.println(((String) m.get("body")));
					mBodyMessage = new BodyMessage(1, MessageType.CRD, "get card request");
					mMessage.put("header", clientConnectionState);
					mMessage.put("body", body);
					sendMessage(mMessage);
					break;

				}
			}

			break;
		case DISCONNECTING:
		//Stop running the thread if the server disconnects from the client
		case DISCONNECTED:
			body = "hi from server";
			// mMessage.put("header", connectionState);
			// mMessage.put("body", body);
			isRunning = false;
			break;
		default:
			System.out.println("Uknown State");
			break;

		}

	}

	/*
	 * Establishes connection with the server on the defined post on the local host
	 */
	public boolean connect() {

		try {

			mSocket = new Socket("localhost", NetworkInterface.PORT);
			clientConnectionState = ClientConnectionState.CONNECTING;

		} catch (IOException ioe) {
			ioe.printStackTrace();
			mSocket = null;
			return false;
		}

		return true;
	}

	/**
	 * Changes state of the client in order to stop receiving messages from the server and be 
	 * removed from the server thread pool
	 */
	public void disconnect() {
		clientConnectionState = ClientConnectionState.DISCONNECTING;
	}

	/**
	 * This method sends a generic message object to the game server
	 */
	public void sendMessage(Object mGameSendDataObject) {

		try {
			if (mObjectOutputStream != null) {
				// System.out.println("Sending message from Server");
				mObjectOutputStream.writeObject(mGameSendDataObject);
				mObjectOutputStream.flush();
			}
		} catch (IOException ioe) {
			// TODO Adding Error Handling
			ioe.printStackTrace();
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
				System.out.println("Message is " + message);
		

			}
		} catch (ClassNotFoundException e) {
			//Print the details of the exception error
			e.printStackTrace();
		} catch (IOException ioe) {
			//Print the details of the exception error
			ioe.printStackTrace();
		}
		// System.out.println("Client Received "+message);
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
	
	public void play() {
		
		if(isRunning) {
			JSONObject mMessage = new JSONObject();
			BodyMessage bodyMessage = new BodyMessage(1, MessageType.CRD, "request a card");
			mMessage.put("header", clientConnectionState);
			mMessage.put("body", mMessage);
			
			sendMessage(mMessage);
		}
		
	
	}

}
