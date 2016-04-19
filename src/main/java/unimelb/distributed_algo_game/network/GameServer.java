/*
 * 
 */
package unimelb.distributed_algo_game.network;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import unimelb.distributed_algo_game.player.Player;

// TODO: Auto-generated Javadoc
/**
 * The Class GameServer.
 *
 * @author Ting-Ying Tsai
 */
public final class GameServer implements Runnable, NetworkInterface {

	/** The instance. */
	private static GameServer instance = null;

	/** The id. */
	private int id = -1;

	/** The m player. */
	private Player mPlayer = null;

	/** The m socket. */
	private Socket mSocket = null;

	/** The m server socket. */
	private ServerSocket mServerSocket = null;

	/** The m lock. */
	private Object mLock;

	/** The connection state. */
	private ConnectionState mConnectionState;

	/** The m player client manager. */
	private PlayerClientManager mPlayerClientManager;

	/**
	 * Instantiates a new game server.
	 */
	protected GameServer() {
		mLock = new Object();
		mConnectionState = ConnectionState.DISCONNECT;
		mPlayerClientManager = new PlayerClientManager(10);
	}

	/**
	 * Gets the single instance of GameServer.
	 *
	 * @return single instance of GameServer
	 */
	public static GameServer getInstance() {
		if (instance == null) {
			instance = new GameServer();
		}
		return instance;
	}

	/**
	 * Sets the id.
	 *
	 * @param id
	 *            the new id
	 */
	public void setId(int id) {
		this.id = id;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Runnable#run()
	 */
	public void run() {
		try {
			if (mServerSocket != null) {
				System.out.println("Server Start, Waiting....");
				synchronized (mLock) {
					while (mConnectionState == ConnectionState.CONNECT) {

						mSocket = mServerSocket.accept();
						PlayerClientThread t = new PlayerClientThread(mSocket, 1);
						mPlayerClientManager.addClient(t);
						t.start();

						mPlayerClientManager.notifyAllClients();

					}

					mServerSocket.close();
				}
			}
		} catch (IOException ioe) {
			// TODO Adding error handling
			ioe.printStackTrace();

		} finally {

			System.out.println("Connection Closed");
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see unimelb.distributed_algo_game.network.NetworkInterface#connect()
	 */
	public synchronized boolean connect() {

		try {

			mServerSocket = new ServerSocket(NetworkInterface.PORT);
			mConnectionState = ConnectionState.CONNECT;

		} catch (IOException ioe) {
			ioe.printStackTrace();
			mServerSocket = null;
			return false;
		}
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see unimelb.distributed_algo_game.network.NetworkInterface#disconnect()
	 */
	public synchronized void disconnect() {
		mConnectionState = ConnectionState.DISCONNECT;
	}
}
