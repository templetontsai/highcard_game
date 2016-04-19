package unimelb.distributed_algo_game.network;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import unimelb.distributed_algo_game.network.NetworkInterface.ConnectionState;

/**
 * @author Ting-Ying Tsai
 *
 */
public class PlayerClientThread extends Thread implements ClientNetworkObserver {
	private Socket mSocket = null;
	private int clientID = -1;
	private Object mGameReveiceDataObject = null;
	private ObjectOutputStream mObjectOutputStream = null;
	private ObjectInputStream mObjectInputStream = null;
	private Object mLock = null;
	private ConnectionState connectionState = null;

	public PlayerClientThread(Socket mSocket, int clientID) {
		if (mSocket != null) {
			this.mSocket = mSocket;
		} else
			throw new NullPointerException();
		this.clientID = clientID;
		mLock = new Object();
		connectionState = ConnectionState.DISCONNECT;
	}

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

	public void update() {
		sendMessage("Game is Over");
		// connectionState = ConnectionState.DISCONNECT;

	}

}
