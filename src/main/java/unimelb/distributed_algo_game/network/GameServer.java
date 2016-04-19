package unimelb.distributed_algo_game.network;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import unimelb.distributed_algo_game.player.Player;

/**
 * @author Ting-Ying Tsai
 *
 */
public final class GameServer implements Runnable, NetworkInterface {

	private static GameServer instance = null;
	private int id = -1;

	private Player mPlayer = null;
	private Socket mSocket = null;
	private ServerSocket mServerSocket = null;
	private Object mLock;
	private ConnectionState mCconnectionState;
	private PlayerClientManager mPlayerClientManager;

	protected GameServer() {
		mLock = new Object();
		mCconnectionState = ConnectionState.DISCONNECT;
		mPlayerClientManager = new PlayerClientManager(10);
	}

	public static GameServer getInstance() {
		if (instance == null) {
			instance = new GameServer();
		}
		return instance;
	}

	public void setId(int id) {
		this.id = id;
	}

	public void run() {
		try {
			if (mServerSocket != null) {
				System.out.println("Server Start, Waiting....");
				synchronized (mLock) {
					while (mCconnectionState == ConnectionState.CONNECT) {

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

	public synchronized boolean connect() {

		try {

			mServerSocket = new ServerSocket(NetworkInterface.PORT);
			mCconnectionState = ConnectionState.CONNECT;

		} catch (IOException ioe) {
			ioe.printStackTrace();
			mServerSocket = null;
			return false;
		}
		return true;
	}

	public synchronized void disconnect() {
		mCconnectionState = ConnectionState.DISCONNECT;
	}
}
