package unimelb.distributed_algo_game.network;

import java.util.ArrayList;
import java.util.List;

import org.json.simple.JSONObject;

import unimelb.distributed_algo_game.network.BodyMessage.MessageType;
import unimelb.distributed_algo_game.network.NetworkInterface.ClientConnectionState;
import unimelb.distributed_algo_game.network.gui.MainGamePanel;
import unimelb.distributed_algo_game.network.utils.Utils;
import unimelb.distributed_algo_game.player.GamePlayerInfo;
import unimelb.distributed_algo_game.player.Player;

// TODO: Auto-generated Javadoc
/**
 * Client socket manager class.
 *
 * @author Lupiya
 */
public class GameClientSocketManager {

	/** The m list clients. */
	// Initialize all the client socket manager vaiables
	private List<GameClient> mListClients = null;

	/** The m player. */
	private Player mPlayer = null;

	/** The m game server. */
	private GameServer mGameServer = null;

	/** The is replied. */
	private boolean isReplied = false;

	/** The m main game panel. */
	private MainGamePanel mMainGamePanel;

	/** The m game client dealer. */
	private GameClient mGameClientDealer = null;

	/**
	 * Default constructor for client socket manager.
	 *
	 * @param mPlayer
	 *            the m player
	 */
	public GameClientSocketManager(Player mPlayer) {
		mListClients = new ArrayList<GameClient>();
		this.mPlayer = mPlayer;

	}

	/**
	 * This sets the game server reference.
	 *
	 * @param mGameServer
	 *            the new game server
	 */
	public void setGameServer(GameServer mGameServer) {
		this.mGameServer = mGameServer;
	}

	/**
	 * This sends an election message to the neighbor of the current player.
	 *
	 * @param neighborID
	 *            the neighbor id
	 */
	public void startElection(int neighborID) {
		System.out.println("Current size of players is " + mListClients.size());
		if (mListClients != null && mListClients.size() > 0) {
			for (GameClient c : mListClients) {
				JSONObject mMessage = new JSONObject();
				BodyMessage mBodyMessage = new BodyMessage(mPlayer.getGamePlayerInfo(), MessageType.ELE,
						Integer.toString(mPlayer.getGamePlayerInfo().getNodeID()));
				mMessage.put("header", ClientConnectionState.CONNECTED);
				mMessage.put("body", mBodyMessage);

				if (c.getPlayerSSNodeID() == neighborID) {
					System.out.println("Sending start election message to " + c.getPlayerSSNodeID());
					c.sendMessage(mMessage);
				}
			}
		}
	}

	/**
	 * This forwards a message to a player's neighbor.
	 *
	 * @param mMessage
	 *            the m message
	 * @param neighborID
	 *            the neighbor id
	 */
	public void sendElectionMessage(JSONObject mMessage, int neighborID) {
		if (mListClients != null && mListClients.size() > 0) {
			for (GameClient c : mListClients) {

				if (c.getPlayerSSNodeID() == neighborID) {
					System.out.println("Sending forward election message to " + c.getPlayerSSNodeID());
					c.sendMessage(mMessage);
				} else {
					System.out.println("Can't forward election message to " + c.getPlayerSSNodeID());
				}
			}
		}
	}

	/**
	 * Broadcasts a coordinator message to all the players to announce the new
	 * leader.
	 *
	 * @param mMessage
	 *            the m message
	 */
	public void sendCoordinatorMessage(JSONObject mMessage) {
		if (mListClients != null && mListClients.size() > 0) {
			for (GameClient c : mListClients) {
				c.sendMessage(mMessage);
			}
		}
	}

	/**
	 * This broadcasts a card request to all the other players in a game.
	 *
	 * @param timestamp
	 *            the timestamp
	 */
	public void broadcastCRT(long timestamp) {
		System.out.println("broadcastCRT, mListClients: " + mListClients.size());
		System.out.println(mPlayer.getGamePlayerInfo().getPort());
		mGameServer.setIsCRTRequested(true, Utils.getProcessTimestamp());
		if (mListClients != null && mListClients.size() > 0) {
			for (GameClient c : mListClients) {
				JSONObject mMessage = new JSONObject();
				BodyMessage bodyMessage = new BodyMessage(mPlayer.getGamePlayerInfo(), MessageType.BCT_CRT, timestamp);
				mMessage.put("header", ClientConnectionState.CONNECTED);
				mMessage.put("body", bodyMessage);

				c.sendMessage(mMessage);
			}
		}
	}

	/**
	 * This confirms that all players have replied to the player's card request.
	 *
	 * @return the reply
	 */
	public boolean getReply() {
		boolean isReplied = false;
		if (mListClients != null && mListClients.size() > 0) {
			// System.out.println("isReplied: " + mListClients.size());
			for (GameClient c : mListClients) {
				isReplied = c.getReply();
				// System.out.println("isReplied: " + isReplied);
			}
		} else {

			isReplied = true;
		}

		return isReplied;
	}

	/**
	 * This removes all the clients from the list.
	 */
	public synchronized void removeAll() {
		mListClients.clear();
	}

	/**
	 * This creates a new client server socket thread.
	 *
	 * @param gameClientInfo
	 *            the game client info
	 */
	public void addSocketClient(GamePlayerInfo gameClientInfo) {
		System.out.println("1Socket Client Size:" + mListClients.size());

		if (gameClientInfo.getNodeID() != this.mPlayer.getGamePlayerInfo().getNodeID() && !gameClientInfo.isDealer()) {

			System.out.println("I am node: " + this.mPlayer.getGamePlayerInfo().getNodeID()
					+ " adding my socket client node: " + gameClientInfo.getNodeID());
			GameClient client = new GameClient(this.mPlayer, gameClientInfo.getIPAddress(), gameClientInfo.getPort(),
					false);
			client.setPlayerSSNodeID(gameClientInfo.getNodeID());
			// Adding to the first gameclient and pass ref for manager later run
			// this manager thread once it gets the list of nodes in the network
			// mGameClientSocketManager.addSocketClient(gameClient);
			client.setClientSocketManager(this);
			mListClients.add(client);
			System.out.println("2Socket Client Size:" + mListClients.size());
		}

		System.out.println("3Socket Client Size:" + mListClients.size());
	}

	/**
	 * Sets the socket client to dealer.
	 *
	 * @param mCameClient
	 *            the new socket client to dealer
	 */
	public void setSocketClientToDealer(GameClient mCameClient) {
		this.mGameClientDealer = mCameClient;
	}

	/**
	 * Send request server time.
	 */
	public void sendRequestServerTime() {
		this.mGameClientDealer.sendRequestServerTime();
	}

	/**
	 * Sets the is crt requested.
	 *
	 * @param isRequested
	 *            the is requested
	 * @param requestedTimestamp
	 *            the requested timestamp
	 */
	public void setIsCRTRequested(boolean isRequested, long requestedTimestamp) {
		mGameServer.setIsCRTRequested(isRequested, requestedTimestamp);
	}

	/**
	 * Broadcast cr tis free.
	 */
	public void broadcastCRTisFree() {
		mGameServer.broadcastCRTisFree();
	}

	/**
	 * This removes a player socket server thread.
	 *
	 * @param gameClient
	 *            the game client
	 */
	public void removeSocketClient(GameClient gameClient) {

		mListClients.remove(gameClient);
		System.out.println("removing gameclient, Socket Client Size: " + mListClients.size());

	}

	/**
	 * This initializes the client socket manager.
	 */
	public synchronized void initGameClientsConnection() {
		System.out.println("1 initGameClientsConnection Socket Client Size:" + mListClients.size());
		if (mListClients != null && mListClients.size() > 0) {

			for (GameClient client : mListClients) {

				client.connect();
				Thread t = new Thread(client);
				t.setName("Slave Player Socket Thread" + client.getPlayer().getGamePlayerInfo().getNodeID());
				t.start();
				client.play();

			}

		}
	}

	/**
	 * This returns the replied status of all the clients.
	 *
	 * @return true, if is all crt replied
	 */
	public synchronized boolean isAllCRTReplied() {
		if (mListClients != null && mListClients.size() > 0) {
			for (GameClient client : mListClients) {
				this.isReplied = client.getReply();
			}

		} else {

			this.isReplied = true;
		}

		return this.isReplied;
	}

	/**
	 * Broadcasts the client list to all the players in the game.
	 */
	public synchronized void broadcastClientsList() {
		if (mListClients != null && mListClients.size() > 0) {
			for (GameClient client : mListClients) {
				JSONObject mMessage = new JSONObject();

				BodyMessage bodyMessage = new BodyMessage(mPlayer.getGamePlayerInfo(), MessageType.BCT_CLIENT_LST,
						client.getServerDetails());
				mMessage.put("header", ClientConnectionState.CONNECTED);
				mMessage.put("body", bodyMessage);
				client.sendMessage(mMessage);
			}
		}

	}

	/**
	 * Closes all the existing client connections.
	 */
	public synchronized void closeAllClientConnection() {
		if (mListClients != null && mListClients.size() > 0) {

			for (GameClient client : mListClients) {

				client.disconnect();

			}

		}
	}

	/**
	 * Reinitializes the game as the dealer.
	 *
	 * @param newDealer
	 *            the new dealer
	 */
	public void reInitGameAsDealer(GamePlayerInfo newDealer) {
		closeAllClientConnection();
		mGameServer.reInitGameAsDealer(newDealer);
	}

	/**
	 * Returns the game panel.
	 *
	 * @return the panel
	 */
	public MainGamePanel getPanel() {
		return this.mMainGamePanel;
	}

	/**
	 * Sets the game panel.
	 *
	 * @param mMainPanel
	 *            the new panel
	 */
	public void setPanel(MainGamePanel mMainPanel) {
		this.mMainGamePanel = mMainPanel;
	}

}
