package unimelb.distributed_algo_game.network;

import java.util.ArrayList;
import java.util.List;

import org.json.simple.JSONObject;

import unimelb.distributed_algo_game.network.BodyMessage.MessageType;
import unimelb.distributed_algo_game.network.NetworkInterface.ClientConnectionState;
import unimelb.distributed_algo_game.network.gui.MainGamePanel;
import unimelb.distributed_algo_game.player.GamePlayerInfo;
import unimelb.distributed_algo_game.player.Player;

public class GameClientSocketManager {

	private List<GameClient> mListClients = null;
	private Player mPlayer = null;
	private GameServer mGameServer = null;
	private boolean isReplied = false;
	private MainGamePanel mMainGamePanel;

	public GameClientSocketManager(Player mPlayer) {
		mListClients = new ArrayList<GameClient>();
		this.mPlayer = mPlayer;

	}

	public void setGameServer(GameServer mGameServer) {
		this.mGameServer = mGameServer;
	}

	public void startElection() {
		System.out.println("Current size of players is " + mListClients.size());
		if (mListClients != null && mListClients.size() > 0) {
			for (GameClient c : mListClients) {
				JSONObject mMessage = new JSONObject();
				BodyMessage mBodyMessage = new BodyMessage(mPlayer.getGamePlayerInfo(), MessageType.ELE,
						Integer.toString(mPlayer.getGamePlayerInfo().getNodeID()));
				mMessage.put("header", ClientConnectionState.CONNECTED);
				mMessage.put("body", mBodyMessage);

				System.out.println("Sending start election message to " + c.getPlayerSSNodeID());
				c.sendMessage(mMessage);
			}
		}
	}

	public void sendElectionMessage(JSONObject mMessage) {
		if (mListClients != null && mListClients.size() > 0) {
			for (GameClient c : mListClients) {
				System.out.println("Sending election message to " + c.getPlayerSSNodeID());
				c.sendMessage(mMessage);
			}
		}
	}

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
	
	public synchronized void removeAll() {
		mListClients.clear();
	}

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

	public void removeSocketClient(GameClient gameClient) {

		mListClients.remove(gameClient);
		System.out.println("removing gameclient, Socket Client Size: " + mListClients.size());

	}

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

	public synchronized void closeAllClientConnection() {
		if (mListClients != null && mListClients.size() > 0) {

			for (GameClient client : mListClients) {

				client.disconnect();

			}

		}
	}

	public void reInitGameAsDealer(GamePlayerInfo newDealer) {
		closeAllClientConnection();
		mGameServer.reInitGameAsDealer(newDealer);
	}

	
	public MainGamePanel getPanel() {
		return this.mMainGamePanel;
	}
	
	public void setPanel(MainGamePanel mMainPanel) {
		this.mMainGamePanel = mMainPanel;
	}


}
