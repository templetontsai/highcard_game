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
	private ConnectionState connectionState;

	/**
	 * Instantiates a new game client.
	 */
	protected GameClient() {
		mLock = new Object();
		connectionState = ConnectionState.DISCONNECT;
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

				while (connectionState == ConnectionState.CONNECT) {
					synchronized(mLock) {
						if(gameSendDataObject != null) {
							mObjectOutputStream.writeObject(gameSendDataObject);
						} 
						try {
							gameReveiceDataObject = mObjectInputStream.readObject();
							
						} catch (ClassNotFoundException e) {
							System.out.println("writing undefined class");
							e.printStackTrace();
						}
						
					}
					//reset send data object
					gameSendDataObject = null;
					
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
			connectionState = ConnectionState.CONNECT;

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
		connectionState = ConnectionState.DISCONNECT;
	}



	/**
	 * Send data.
	 *
	 * @param object
	 *            the object
	 */
	public synchronized void sendData(Object object) {

		if (object != null)
			gameSendDataObject = object;
		else {
			System.out.println("Can't send null object");
			throw new NullPointerException();
		}

	}

	/**
	 * Receive data.
	 *
	 * @return the object
	 */
	public synchronized Object receiveData() {

		if (gameReveiceDataObject != null) {
			return gameReveiceDataObject;
		} else {
			System.out.println("gameReveiceDataObject is null");
			throw new NullPointerException();
		}

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
