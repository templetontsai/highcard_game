package unimelb.distributed_algo_game.player;

import java.io.Serializable;

import unimelb.distributed_algo_game.network.utils.Utils;

public class GamePlayerInfo implements Serializable {
	// 0: nodeid, 1: ip address, 2: port
	private String mGamePlayerInfo[] = { "-1", "", "" };
	private long timeStamp = -1;
	private boolean isDealer = false;
	private long csReturnTimeStamp = -1;

	public GamePlayerInfo() {

	}

	public GamePlayerInfo(String mGamePlayerInfo[], boolean isDealer) {
		this.mGamePlayerInfo = mGamePlayerInfo;
		this.isDealer = isDealer;
	}

	public String getIPAddress() {
		return mGamePlayerInfo[1];
	}

	public String getPort() {
		return mGamePlayerInfo[2];
	}

	public int getNodeID() {
		return new Integer(mGamePlayerInfo[0]);
	}

	public String getStringNodeID() {
		return mGamePlayerInfo[0];
	}

	public String getPlayerDetails() {
		return mGamePlayerInfo[0] + "," + mGamePlayerInfo[1] + "," + mGamePlayerInfo[2];
	}

	public void setServerTimeStamp(long timestamp) {
		this.timeStamp = timestamp;
	}

	public long getServerTimeStamp() {
		return this.timeStamp;
	}

	public void setCSReturnTimeStamp(long csReturnTimeStamp) {
		this.csReturnTimeStamp = csReturnTimeStamp;
	}

	public long getCSReturnTimeStamp() {
		return this.csReturnTimeStamp;
	}

	public void setDealer(boolean isDealer) {
		this.isDealer = isDealer;
	}

	public boolean isDealer() {
		return this.isDealer;
	}
}
