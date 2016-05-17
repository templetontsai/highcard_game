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
	private List<GamePlayerInfo> mClientList = null;
	private Player mPlayer = null;

	public GameClientSocketManager(Player mPlayer) {
		mListClients = new ArrayList<GameClient>();
		mClientList = new ArrayList<GamePlayerInfo>();
		this.mPlayer = mPlayer;
	}

	public void broadcastToAllServers() {
		for (GameClient c : mListClients) {
			// c.sendMessage(mGameSendDataObject);
		}
	}
	
	public void broadcastCRT(long timestamp) {
		if(mListClients != null && mListClients.size() > 0) {
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
		if(mListClients != null && mListClients.size() > 0) {
			for (GameClient c : mListClients) {
				isReplied = c.getReply();
			}
		} else {
			
			isReplied =  true;
		}
		
		return isReplied;
	}

	public void addSocketClient(GameClient gameClient) {
		mListClients.add(gameClient);
	}

	public void removeSocketClient(GameClient gameClient) {
		mListClients.remove(gameClient);
	
	}

	public void setClientList(List<GamePlayerInfo> mClientList) {
		this.mClientList = mClientList;
	}

	public void initGameClientsConnection() {
		if (mClientList != null) {
			int index = 0;
			for (GamePlayerInfo info : mClientList) {
				if (info.getNodeID() != this.mPlayer.getGamePlayerInfo().getNodeID() && info.getNodeID() != 0) {
					GameClient client = new GameClient(this.mPlayer, info.getIPAddress(), info.getPort());
					client.setClientSocketManager(this);
					client.connect();
					Thread t = new Thread(client);
					t.setName("Slave Player Socket Thread" + info.getNodeID());
					t.start();
					client.play();
					mListClients.add(client);
				}
				index++;
			}
		}
	}

}
