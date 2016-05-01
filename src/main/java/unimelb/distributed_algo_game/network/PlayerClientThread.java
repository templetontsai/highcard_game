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

	/** The JSON body message */
	private JSONObject mMessage = null;

	/** The boolean for running the client thread */
	private boolean isRunning = false;

	/** The game server object */
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

	/**
	 * Runs the main method of the client thread
	 */
	public void run() {

		
		isRunning = true;
		try {
			//Receives input and sends message using server socket
			mObjectInputStream = new ObjectInputStream(mSocket.getInputStream());
			mObjectOutputStream = new ObjectOutputStream(mSocket.getOutputStream());
		} catch (IOException ioe) {
			ioe.getStackTrace();
		}
		
		JSONObject m;
		BodyMessage bodyMessage;
		ClientConnectionState clientConnectionState;
		
		//Main loop to run the client thread
		while (isRunning) {
			
			//Receive JSON message object from server
			m = (JSONObject)receiveMessage();

			   
			//Only process the message if it's not null
			if(m != null) {
				//Get the client connection state and body from the message
				clientConnectionState = (ClientConnectionState) m.get("header");
				bodyMessage = (BodyMessage)m.get("body");
				System.out.println(m);

				switch (clientConnectionState) {
				
				//Process the message based on the connection state
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

		//Close the input and output streams to the server
		try {
			mObjectInputStream.close();
			mObjectOutputStream.close();
			mSocket.close();
			System.out.println("Client closed");
		} catch (IOException ioe) {
			//Print out the details of the exception error
			ioe.printStackTrace();
		}
	}
	
	/**
	 * This method checks the type of JSON body message and carries out the 
	 * necessary action for each message type
	 * @param mBodyMessage
	 */
	private void checkMessageType(BodyMessage mBodyMessage) {
		MessageType messagType = mBodyMessage.getMessageType();
		switch(messagType) {
		//Used to acknowledge the server is still alive
		case ACK:
			System.out.println(mBodyMessage.getMessage());
			break;
		//Used to send a card to the client after receiving a request message
		case CRD:
			ClientConnectionState connectionState = ClientConnectionState.CONNECTED;
			//Player specifies the card to 
			mBodyMessage = new BodyMessage(this.clientID, MessageType.CRD, mGameServer.getCard(1));
			
			mMessage.put("header", connectionState);
			mMessage.put("body", mBodyMessage);
			sendMessage(mMessage);
			break;
		//Used to send send a broadcast message
		case BCT:
			System.out.println(mBodyMessage.getMessage());
			break;
	    //Used to send a disconnect message
		case DSC:
			System.out.println(mBodyMessage.getMessage());
			break;
		
		}
	}

	/**
	 * Sends message to a client.
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
			//Print out the details of the exception error
			ioe.printStackTrace();
		}

	}

	/**
	 * Receive message from the client.
	 */
	public synchronized Object receiveMessage() {
		
		Object message = null;

		try {
			if (mObjectInputStream != null) {
				message = mObjectInputStream.readObject();
				System.out.println(message);
			
			}
		} catch (ClassNotFoundException e) {
			//Print out the details of the exception error
			e.printStackTrace();
		} catch (IOException ioe) {
			//Print out the details of the exception error
			ioe.printStackTrace();
		}
		
		return message;

	}

	/**
	 * Used to send an update message
	 */
	public void update() {
		// sendMessage("Game );
		// connectionState = ConnectionState.DISCONNECT;

	}

}
