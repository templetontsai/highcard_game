package unimelb.distributed_algo_game.network;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONObject;

import unimelb.distributed_algo_game.network.BodyMessage.MessageType;
import unimelb.distributed_algo_game.network.NetworkInterface.ClientConnectionState;
import unimelb.distributed_algo_game.network.utils.Utils;
import unimelb.distributed_algo_game.player.GamePlayerInfo;
import unimelb.distributed_algo_game.player.Player;

public class GameClientSocketManager {

	private List<GameClient> mListClients = null;
	private Player mPlayer = null;
	private boolean isReplied = false;

	public GameClientSocketManager(Player mPlayer) {
		mListClients = new ArrayList<GameClient>();
		this.mPlayer = mPlayer;
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
			for (GameClient c : mListClients) {
				isReplied = c.getReply();
			}
		} else {

			isReplied = true;
		}

		return isReplied;
	}

	public void addSocketClient(GamePlayerInfo gameClientInfo) {
		System.out.println("Socket Client Size:" + mListClients.size());
		if (gameClientInfo.getNodeID() != this.mPlayer.getGamePlayerInfo().getNodeID()
				&& gameClientInfo.getNodeID() != 0) {
			GameClient client = new GameClient(this.mPlayer, gameClientInfo.getIPAddress(), gameClientInfo.getPort(),
					false);
			client.setPlayerSSNodeID(gameClientInfo.getNodeID());
			// Adding to the first gameclient and pass ref for manager later run
			// this manager thread once it gets the list of nodes in the network
			// mGameClientSocketManager.addSocketClient(gameClient);
			client.setClientSocketManager(this);
			mListClients.add(client);
			System.out.println("Socket Client Size:" + mListClients.size());
		}
	}

	public void removeSocketClient(GameClient gameClient) {

		mListClients.remove(gameClient);
		System.out.println("removing gameclient, Socket Client Size: " + mListClients.size());

	}

	public void initGameClientsConnection() {
		if (mListClients != null) {

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
		if (mListClients.size() >= 1) {
			for (GameClient client : mListClients) {
				this.isReplied = client.getReply();
			}

		} else {

			this.isReplied = true;
		}

		return this.isReplied;
	}

	public synchronized void broadcastClientsList() {
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
