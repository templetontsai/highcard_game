/*
 * 
 */
package unimelb.distributed_algo_game.network;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import unimelb.distributed_algo_game.network.NetworkInterface.ConnectionState;

// TODO: Auto-generated Javadoc
/**
 * The Class PlayerClientThread.
 *
 * @author Ting-Ying Tsai
 */
public class PlayerClientThread extends Thread implements ClientNetworkObserver {

	/** The m socket. */
	private Socket mSocket = null;

	/** The client id. */
	private int clientID = -1;

	/** The m game reveice data object. */
	private Object mGameReveiceDataObject = null;

	/** The m object output stream. */
	private ObjectOutputStream mObjectOutputStream = null;

	/** The m object input stream. */
	private ObjectInputStream mObjectInputStream = null;

	/** The m lock. */
	private Object mLock = null;

	/** The connection state. */
	private ConnectionState connectionState = null;

	/**
	 * Instantiates a new player client thread.
	 *
	 * @param mSocket
	 *            the m socket
	 * @param clientID
	 *            the client id
	 */
	public PlayerClientThread(Socket mSocket, int clientID) {
		if (mSocket != null) {
			this.mSocket = mSocket;
		} else
			throw new NullPointerException();
		this.clientID = clientID;
		mLock = new Object();
		connectionState = ConnectionState.DISCONNECT;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Thread#run()
	 */
	public void run() {

		connectionState = ConnectionState.CONNECT;
		try {
			mObjectInputStream = new ObjectInputStream(mSocket.getInputStream());
			mObjectOutputStream = new ObjectOutputStream(mSocket.getOutputStream());
		} catch (IOException ioe) {
			ioe.getStackTrace();
		}
		while (connectionState == ConnectionState.CONNECT) {
			// sendMessage("Hi, this is Gmae Server");
			receiveMessage();
			// System.out.println("Client running");
		}

		try {
			mObjectInputStream.close();
			mObjectOutputStream.close();
			mSocket.close();
			System.out.println("Client closed");
		} catch (IOException ioe) {
			// TODO Adding Error Handling
			ioe.printStackTrace();
		}
	}

	/**
	 * Send message.
	 *
	 * @param mGameSendDataObject
	 *            the m game send data object
	 */
	private void sendMessage(Object mGameSendDataObject) {

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
	 * Receive message.
	 */
	private void receiveMessage() {

		try {
			if (mObjectInputStream != null) {
				mGameReveiceDataObject = mObjectInputStream.readObject();
				System.out.println(mGameReveiceDataObject);
			}
		} catch (ClassNotFoundException e) {
			// TODO Adding Error Handling
			e.printStackTrace();
		} catch (IOException ioe) {
			// TODO Adding Error Handling
			ioe.printStackTrace();
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see unimelb.distributed_algo_game.network.ClientNetworkObserver#update()
	 */
	public void update() {
		sendMessage("Game is Over");
		// connectionState = ConnectionState.DISCONNECT;

	}

}
