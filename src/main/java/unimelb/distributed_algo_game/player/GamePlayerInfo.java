package unimelb.distributed_algo_game.player;

import java.io.Serializable;

import unimelb.distributed_algo_game.network.utils.Utils;

// TODO: Auto-generated Javadoc
/**
 * The Class GamePlayerInfo.
 */
public class GamePlayerInfo implements Serializable {

	/** The m game player info. */
	// 0: nodeid, 1: ip address, 2: port
	private String mGamePlayerInfo[] = { "-1", "", "" };

	/** The time stamp. */
	private long timeStamp = -1;

	/** The is dealer. */
	private boolean isDealer = false;

	/** The cs return time stamp. */
	private long csReturnTimeStamp = -1;

	/**
	 * Instantiates a new game player info.
	 */
	public GamePlayerInfo() {

	}

	/**
	 * Instantiates a new game player info.
	 *
	 * @param mGamePlayerInfo
	 *            the m game player info
	 * @param isDealer
	 *            the is dealer
	 */
	public GamePlayerInfo(String mGamePlayerInfo[], boolean isDealer) {
		this.mGamePlayerInfo = mGamePlayerInfo;
		this.isDealer = isDealer;
	}

	/**
	 * Gets the IP address.
	 *
	 * @return the IP address
	 */
	public String getIPAddress() {
		return mGamePlayerInfo[1];
	}

	/**
	 * Gets the port.
	 *
	 * @return the port
	 */
	public String getPort() {
		return mGamePlayerInfo[2];
	}

	/**
	 * Gets the node id.
	 *
	 * @return the node id
	 */
	public int getNodeID() {
		return new Integer(mGamePlayerInfo[0]);
	}

	/**
	 * Gets the string node id.
	 *
	 * @return the string node id
	 */
	public String getStringNodeID() {
		return mGamePlayerInfo[0];
	}

	/**
	 * Gets the player details.
	 *
	 * @return the player details
	 */
	public String getPlayerDetails() {
		return mGamePlayerInfo[0] + "," + mGamePlayerInfo[1] + "," + mGamePlayerInfo[2];
	}

	/**
	 * Sets the server time stamp.
	 *
	 * @param timestamp
	 *            the new server time stamp
	 */
	public void setServerTimeStamp(long timestamp) {
		this.timeStamp = timestamp;
	}

	/**
	 * Gets the server time stamp.
	 *
	 * @return the server time stamp
	 */
	public long getServerTimeStamp() {
		return this.timeStamp;
	}

	/**
	 * Sets the CS return time stamp.
	 *
	 * @param csReturnTimeStamp
	 *            the new CS return time stamp
	 */
	public void setCSReturnTimeStamp(long csReturnTimeStamp) {
		this.csReturnTimeStamp = csReturnTimeStamp;
	}

	/**
	 * Gets the CS return time stamp.
	 *
	 * @return the CS return time stamp
	 */
	public long getCSReturnTimeStamp() {
		return this.csReturnTimeStamp;
	}

	/**
	 * Sets the dealer.
	 *
	 * @param isDealer
	 *            the new dealer
	 */
	public void setDealer(boolean isDealer) {
		this.isDealer = isDealer;
	}

	/**
	 * Checks if is dealer.
	 *
	 * @return true, if is dealer
	 */
	public boolean isDealer() {
		return this.isDealer;
	}
}
