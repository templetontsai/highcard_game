package unimelb.distributed_algo_game.network;

import unimelb.distributed_algo_game.player.NetworkObserver;

/**
 * This is the interface for creating network connectivity
 * @author Ting-Ying Tsai
 *
 */
public interface NetworkInterface {
	public static final int PORT = 10009;
	public static final String GameServerName = "HighCard Game";
	public enum ConnectionState
	{
		CONNECT, DISCONNECT
	};
	
	public boolean connect();
	public void disconnect();

}
