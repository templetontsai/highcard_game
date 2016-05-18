package unimelb.distributed_algo_game.network;

import java.util.ArrayList;
import java.util.List;

import org.json.simple.JSONObject;

import unimelb.distributed_algo_game.network.BodyMessage.MessageType;
import unimelb.distributed_algo_game.network.NetworkInterface.ClientConnectionState;
import unimelb.distributed_algo_game.network.gui.MainGamePanel;
import unimelb.distributed_algo_game.player.GamePlayerInfo;
import unimelb.distributed_algo_game.player.Player;

/**
 * Client socket manager class
 * @author Lupiya
 *
 */
public class GameClientSocketManager {

	//Initialize all the client socket manager vaiables
	private List<GameClient> mListClients = null;
	private Player mPlayer = null;
	private GameServer mGameServer = null;
	private boolean isReplied = false;
	private MainGamePanel mMainGamePanel;

	/**
	 * Default constructor for client socket manager
	 * @param mPlayer
	 */
	public GameClientSocketManager(Player mPlayer) {
		mListClients = new ArrayList<GameClient>();
		this.mPlayer = mPlayer;

	}

	/**
	 * This sets the game server reference
	 * @param mGameServer
	 */
	public void setGameServer(GameServer mGameServer) {
		this.mGameServer = mGameServer;
	}

	/**
	 * This sends an election message to the neighbor of the current player
	 * @param neighborID
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

				if(c.getPlayerSSNodeID()==neighborID){
					System.out.println("Sending start election message to " + c.getPlayerSSNodeID());
				    c.sendMessage(mMessage);
				}
			}
		}
	}

	/**
	 * This forwards a message to a player's neighbor
	 * @param mMessage
	 */
	public void sendElectionMessage(JSONObject mMessage, int neighborID) {
		if (mListClients != null && mListClients.size() > 0) {
			for (GameClient c : mListClients) {
				
				if(c.getPlayerSSNodeID()==neighborID){
					System.out.println("Sending forward election message to " + c.getPlayerSSNodeID());
				    c.sendMessage(mMessage);
				}else{
					System.out.println("Can't forward election message to " + c.getPlayerSSNodeID());
				}
			}
		}
	}
	
	/**
	 * Broadcasts a coordinator message to all the players to announce the new leader
	 * @param mMessage
	 */
	public void sendCoordinatorMessage(JSONObject mMessage){
		if (mListClients != null && mListClients.size() > 0) {
			for (GameClient c : mListClients) {
				c.sendMessage(mMessage);
			}
		}
	}

	/**
	 * This broadcasts a card request to all the other players in a game
	 * @param timestamp
	 */
	public void broadcastCRT(long timestamp) {
		System.out.println("broadcastCRT, mListClients: " + mListClients.size());
		System.out.println(mPlayer.getGamePlayerInfo().getPort());
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
	 * This confirms that all players have replied to the player's card request
	 * @return
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
	 * This removes all the clients from the list
	 */
	public synchronized void removeAll() {
		mListClients.clear();
	}

	/**
	 * This creates a new client server socket thread
	 * @param gameClientInfo
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
	 * This removes a player socket server thread
	 * @param gameClient
	 */
	public void removeSocketClient(GameClient gameClient) {

		mListClients.remove(gameClient);
		System.out.println("removing gameclient, Socket Client Size: " + mListClients.size());

	}

	/**
	 * This initializes the client socket manager
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
	 * This returns the replied status of all the clients
	 * @return
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
	 * Broadcasts the client list to all the players in the game
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
	 * Closes all the existing client connections
	 */
	public synchronized void closeAllClientConnection() {
		if (mListClients != null && mListClients.size() > 0) {

			for (GameClient client : mListClients) {

				client.disconnect();

			}

		}
	}

	/**
	 * Reinitializes the game as the dealer
	 * @param newDealer
	 */
	public void reInitGameAsDealer(GamePlayerInfo newDealer) {
		closeAllClientConnection();
		mGameServer.reInitGameAsDealer(newDealer);
	}

	/**
	 * Returns the game panel 
	 * @return
	 */
	public MainGamePanel getPanel() {
		return this.mMainGamePanel;
	}
	
	/**
	 * Sets the game panel
	 * @param mMainPanel
	 */
	public void setPanel(MainGamePanel mMainPanel) {
		this.mMainGamePanel = mMainPanel;
	}
    
}
