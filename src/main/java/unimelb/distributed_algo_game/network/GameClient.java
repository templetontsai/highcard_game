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

import unimelb.distributed_algo_game.player.NetworkObserver;
import unimelb.distributed_algo_game.player.Player;

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
	
	private JSONObject mMessage = null;
	
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
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Runnable#run()
	 */
	public void run() {

		if (mSocket != null) {

			try {
				mObjectOutputStream = new ObjectOutputStream(mSocket.getOutputStream());
				mObjectInputStream = new ObjectInputStream(mSocket.getInputStream());
				isRunning = true;
				JSONObject m;
				String body;

				while (isRunning) {
					
					m = (JSONObject) receiveMessage();
					if(m != null) {
						serverConnectionState = (ServerConnectionState) m.get("header");

						switch (clientConnectionState) {
						
						case CONNECTING:
						case CONNECTED:
							if(serverConnectionState != null) {
								switch(serverConnectionState) {
								
								case ACK:
									System.out.println(((String)m.get("body")));
									body = "Ack from Server";
									//mMessage.put("header", clientConnectionState);
									//mMessage.put("body", body);
									//sendMessage(mMessage);
									break;
								
								}
							}
						
					
							break;
						case DISCONNECTING:
						case DISCONNECTED:
							body = "hi from server";
							//mMessage.put("header", connectionState);
							//mMessage.put("body", body);
							isRunning = false;
							break;
						default:
							System.out.println("Uknown State");
							break;

						}
					}
					
				
					
					Thread.sleep(100);
	
				}
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see unimelb.distributed_algo_game.network.NetworkInterface#connect()
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see unimelb.distributed_algo_game.network.NetworkInterface#disconnect()
	 */
	public void disconnect() {
		clientConnectionState = ClientConnectionState.DISCONNECTING;
	}



	/**
	 * Send data.
	 *
	 * @param object
	 *            the object
	 */
	public void sendMessage(Object mGameSendDataObject) {

		try {
			if (mObjectOutputStream != null) {
				System.out.println("Sending message from Server");
				mObjectOutputStream.writeObject(mGameSendDataObject);
			}
		} catch (IOException ioe) {
			// TODO Adding Error Handling
			ioe.printStackTrace();
		}

	}

	/**
	 * Receive data.
	 *
	 * @return the object
	 */
	/**
	 * Receive message.
	 */
	public  Object receiveMessage() {
		
		Object message = null;

		try {
			if (mObjectInputStream != null) {
				message = mObjectInputStream.readObject();
				System.out.println("Message is "+message);
			}
		} catch (ClassNotFoundException e) {
			// TODO Adding Error Handling
			e.printStackTrace();
		} catch (IOException ioe) {
			// TODO Adding Error Handling
			ioe.printStackTrace();
		}
		System.out.println("Client Received "+message);
		return message;

	}

	/**
	 * Attach player.
	 *
	 * @param observer
	 *            the observer
	 */
	public void attachPlayer(NetworkObserver observer) {
		observers.add(observer);
	}

	/**
	 * Dettach player.
	 *
	 * @param observer
	 *            the observer
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

}
