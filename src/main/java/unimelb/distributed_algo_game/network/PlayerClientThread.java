/*
 * 
 */
package unimelb.distributed_algo_game.network;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import org.json.simple.JSONObject;

import unimelb.distributed_algo_game.network.NetworkInterface.ClientConnectionState;

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
	private ClientConnectionState connectionState = null;

	private JSONObject mMessage = null;

	private boolean isRunning = false;

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
		connectionState = ClientConnectionState.DISCONNECTED;
		mMessage = new JSONObject();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Thread#run()
	 */
	public void run() {

		
		isRunning = true;
		try {
			mObjectInputStream = new ObjectInputStream(mSocket.getInputStream());
			mObjectOutputStream = new ObjectOutputStream(mSocket.getOutputStream());
		} catch (IOException ioe) {
			ioe.getStackTrace();
		}
		
		JSONObject m;
		String body;
		ClientConnectionState clientConnectionState;
		
		while (isRunning) {
			
			m = (JSONObject) receiveMessage();
			
			if(m != null) {
				clientConnectionState = (ClientConnectionState) m.get("header");

				switch (clientConnectionState) {
				
				case CONNECTING:
				case CONNECTED:
					System.out.println("connected from client");
					body = "Connected Successful, ACK";
					mMessage.put("header", connectionState);
					mMessage.put("body", body);
					sendMessage(mMessage);
					break;
				case DISCONNECTING:
				case DISCONNECTED:
					System.out.println("disconnected from client");
					body = "hi from server";
					mMessage.put("header", connectionState);
					mMessage.put("body", body);
					isRunning = false;
					break;
				default:
					System.out.println("Uknown State");
					break;

				}
			}
			
			
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
	 * Receive message.
	 */
	public Object receiveMessage() {
		
		Object message = null;

		try {
			if (mObjectInputStream != null) {
				message = mObjectInputStream.readObject();
				//System.out.println(mGameReveiceDataObject);
			}
		} catch (ClassNotFoundException e) {
			// TODO Adding Error Handling
			e.printStackTrace();
		} catch (IOException ioe) {
			// TODO Adding Error Handling
			ioe.printStackTrace();
		}
		
		return message;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see unimelb.distributed_algo_game.network.ClientNetworkObserver#update()
	 */
	public void update() {
		// sendMessage("Game );
		// connectionState = ConnectionState.DISCONNECT;

	}

}
