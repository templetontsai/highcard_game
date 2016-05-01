/*
 * 
 */
package unimelb.distributed_algo_game.network;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import org.json.simple.JSONObject;

import unimelb.distributed_algo_game.network.BodyMessage.MessageType;
import unimelb.distributed_algo_game.network.NetworkInterface.ClientConnectionState;
import unimelb.distributed_algo_game.network.NetworkInterface.ServerConnectionState;

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
	private ServerConnectionState serverConnectionState = null;

	private JSONObject mMessage = null;

	private boolean isRunning = false;

	private GameServer mGameServer = null;
	/**
	 * Instantiates a new player client thread.
	 *
	 * @param mSocket
	 *            the m socket
	 * @param clientID
	 *            the client id
	 */
	public PlayerClientThread(Socket mSocket, int clientID, GameServer mGameServer) {
		if (mSocket != null) {
			this.mSocket = mSocket;
		} else
			throw new NullPointerException();
		this.clientID = clientID;
		mLock = new Object();
		serverConnectionState = ServerConnectionState.DISCONNECTED;
		mMessage = new JSONObject();
		this.mGameServer = mGameServer;
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
		BodyMessage bodyMessage;
		ClientConnectionState clientConnectionState;
		
		
		while (isRunning) {
			
			m = (JSONObject)receiveMessage();

			   
			
			if(m != null) {
				clientConnectionState = (ClientConnectionState) m.get("header");
				bodyMessage = (BodyMessage)m.get("body");
				System.out.println(m);

				switch (clientConnectionState) {
				
				case CONNECTING:
				case CONNECTED:
					//System.out.println("connected from client");
					checkMessageType(bodyMessage);
					break;
				case DISCONNECTING:
				case DISCONNECTED:
					//System.out.println("disconnected from client");
					
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
	
	private void checkMessageType(BodyMessage mBodyMessage) {
		MessageType messagType = mBodyMessage.getMessageType();
		switch(messagType) {
		case ACK:
			System.out.println(mBodyMessage.getMessage());
			break;
		case CRD:
			ClientConnectionState connectionState = ClientConnectionState.CONNECTED;
			//Player specifies the card to 
			mBodyMessage = new BodyMessage(this.clientID, MessageType.CRD, mGameServer.getCard(1));
			
			mMessage.put("header", connectionState);
			mMessage.put("body", mBodyMessage);
			sendMessage(mMessage);
			break;
		case BCT:
			System.out.println(mBodyMessage.getMessage());
			break;
		case DSC:
			System.out.println(mBodyMessage.getMessage());
			break;
		
		}
	}

	/**
	 * Send message.
	 *
	 * @param mGameSendDataObject
	 *            the m game send data object
	 */
	public synchronized void sendMessage(Object mGameSendDataObject) {

		try {
			if (mObjectOutputStream != null && mGameSendDataObject != null) {
				System.out.println("Sending message from Server");
				mObjectOutputStream.writeObject(mGameSendDataObject);
				mObjectOutputStream.flush();
			}
		} catch (IOException ioe) {
			// TODO Adding Error Handling
			ioe.printStackTrace();
		}

	}

	/**
	 * Receive message.
	 */
	public synchronized Object receiveMessage() {
		
		Object message = null;

		try {
			if (mObjectInputStream != null) {
				message = mObjectInputStream.readObject();
				System.out.println(message);
			
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
