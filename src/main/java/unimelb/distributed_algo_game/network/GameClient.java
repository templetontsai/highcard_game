package unimelb.distributed_algo_game.network;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import unimelb.distributed_algo_game.player.NetworkObserver;
import unimelb.distributed_algo_game.player.Player;

/**
 * @author Ting-Ying Tsai
 *
 */
public final class GameClient implements Runnable, NetworkInterface {

	private static GameClient instance = null;
	private Player mPlayer = null;
	private Socket mSocket = null;
	private Object gameSendDataObject = null;
	private Object gameReveiceDataObject = null;
	private List<NetworkObserver> observers = new ArrayList<NetworkObserver>();
	private ObjectOutputStream mObjectOutputStream = null;
	private ObjectInputStream mObjectInputStream = null;
	private Object mLock;
	private ConnectionState connectionState;

	protected GameClient() {
		mLock = new Object();
		connectionState = ConnectionState.DISCONNECT;
	}

	public static GameClient getInstance() {
		if (instance == null) {
			instance = new GameClient();
		}
		return instance;
	}

	public void setPlayer(Player mPlayer) {
		if (mPlayer != null) {
			this.mPlayer = mPlayer;
		} else {
			System.out.println("Player can't be null");
			throw new NullPointerException();
		}

	}

	public void run() {

		if (mSocket != null) {

			try {
				mObjectOutputStream = new ObjectOutputStream(mSocket.getOutputStream());
				mObjectInputStream = new ObjectInputStream(mSocket.getInputStream());

				while (connectionState == ConnectionState.CONNECT) {

					System.out.println("Client Connected, say hi to server");
					// Object sendObj = mPlayer;
					// System.out.println(((HumanPlayer)sendObj).getName());
					// getData();
					// objectOutputStream.writeObject(gameSendDataObject);
					mObjectOutputStream.writeObject("Hey from Client");
					// gameSendDataObject = null;
					getData();

				}
				System.out.println("conection closing...");
				mObjectOutputStream.close();
				mObjectInputStream.close();
				mSocket.close();
			} catch (IOException ioe) {
				// TODO Adding error handling
				ioe.printStackTrace();
			}

		}
	}

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

	public void disconnect() {
		connectionState = ConnectionState.DISCONNECT;
	}

	private void getData() {

		if (mSocket != null) {
			try {

				synchronized (mLock) {
					gameReveiceDataObject = mObjectInputStream.readObject();
					System.out.println(gameReveiceDataObject);
					// if(gameReveiceDataObject != null)
					// notifyAllObservers();
				}

			} catch (IOException ioe) {
				System.out.println("Error when getting input stream");
				ioe.printStackTrace();
			} catch (ClassNotFoundException e) {
				System.out.println("Object class is not supported");
				e.printStackTrace();
			}
		}

	}

	public void sendData(Object object) {

		synchronized (mLock) {
			if (object != null)
				gameSendDataObject = object;
			else
				System.out.println("Can't send null object");
		}
	}

	public synchronized Object receiveData() {

		if (gameReveiceDataObject != null) {
			System.out.println(gameReveiceDataObject);
			return gameReveiceDataObject;
		} else {
			System.out.println("gameReveiceDataObject is null");
		}

		return null;
	}

	public void attachPlayer(NetworkObserver observer) {
		observers.add(observer);
	}

	public void dettachPlayer(NetworkObserver observer) {
		observers.remove(observer);
	}

	private void notifyAllObservers() {
		for (NetworkObserver observer : observers) {
			observer.update();
		}

	}

}
