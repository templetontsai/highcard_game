/*
 * 
 */
package unimelb.distributed_algo_game.network;

// TODO: Auto-generated Javadoc
/**
 * This is the interface for creating network connectivity.
 *
 * @author Ting-Ying Tsai
 */
public interface NetworkInterface {

	/** The Constant PORT. */
	public static final int PORT = 10009;

	/** The Constant GameServerName. */
	public static final String GameServerName = "HighCard Game";

	/** The Constant STILL_ALIVE_TIME_OUT. */
	// ms
	public static final int STILL_ALIVE_TIME_OUT = 5000;

	/** The Constant STILL_ALIVE_ACK_TIME_OUT. */
	public static final int STILL_ALIVE_ACK_TIME_OUT = 10 * 1000;

	/**
	 * The Enum ServerConnectionState.
	 */
	public enum ServerConnectionState {
		/** The connecting. */
		INIT,
		/** The connecting. */
		CONNECTING,
		/** The connected. */
		CONNECTED,
		/** The disconnecting. */
		DISCONNECTING,
		/** The disconnect. */
		DISCONNECTED,

		/** Ack. */
		ACK
	};

	/**
	 * The Enum ClientConnectionState.
	 */
	public enum ClientConnectionState {
		/** The connecting. */
		INIT,
		/** The connecting. */
		CONNECTING,
		/** The connected. */
		CONNECTED,
		/** The disconnecting. */
		DISCONNECTING,
		/** The disconnect. */
		DISCONNECTED,

	};

	/**
	 * Connect.
	 *
	 * @return true, if successful
	 */
	public boolean connect();

	/**
	 * Disconnect.
	 */
	public void disconnect();

}
