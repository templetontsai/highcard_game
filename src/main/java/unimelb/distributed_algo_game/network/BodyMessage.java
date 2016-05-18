package unimelb.distributed_algo_game.network;

import java.io.Serializable;

import unimelb.distributed_algo_game.player.GamePlayerInfo;

// TODO: Auto-generated Javadoc
/**
 * The Class BodyMessage.
 *
 * @author Lupiya This class is generic type to carry JSON data in messages
 */
public class BodyMessage implements Serializable {

	/** The node id. */
	// Initialize all the variables
	private int nodeID;

	/** The m game player info. */
	private GamePlayerInfo mGamePlayerInfo;

	/** The message type. */
	private MessageType messageType;

	/** The message. */
	private Object message;

	/** The timestamp. */
	private long timestamp;

	/**
	 * Acknowledgement codes enumeration.
	 *
	 * @author Lupiya
	 */
	public enum ACKCode {

		/** The node id received. */
		NODE_ID_RECEIVED(0),
		/** The card received. */
		CARD_RECEIVED(1),
		/** The node still alive. */
		NODE_STILL_ALIVE(2),
		/** The client still alive. */
		CLIENT_STILL_ALIVE(3),
		/** The server still alive. */
		SERVER_STILL_ALIVE(4),
		/** The crt rpy. */
		CRT_RPY(5),
		/** The leader ele ack. */
		LEADER_ELE_ACK(6),
		/** The srv time ack. */
		SRV_TIME_ACK(7);
		/** The code. */
		private int code;

		/**
		 * Instantiates a new ACK code.
		 *
		 * @param c
		 *            the c
		 */
		private ACKCode(int c) {
			code = c;
		}

		/**
		 * Gets the code.
		 *
		 * @return the code
		 */
		public int getCode() {
			return code;
		}
	}

	/**
	 * Message type enumerations.
	 *
	 * @author Lupiya
	 */

	public enum MessageType {

		/** The con. */
		CON(0),
		/** The ack. */
		ACK(1),
		/** The bct rst. */
		BCT_RST(2),
		/** The bct node lst. */
		BCT_NODE_LST(3),
		/** The bct client lst. */
		BCT_CLIENT_LST(4),
		/** The bct node upt. */
		BCT_NODE_UPT(5),
		/** The bct client upt. */
		BCT_CLIENT_UPT(6),
		/** The bct rdy. */
		BCT_RDY(7),
		/** The bct crd. */
		BCT_CRD(8),
		/** The bct crt. */
		BCT_CRT(9),
		/** The bct crt free. */
		BCT_CRT_FREE(10),

		/** The crd. */
		CRD(10),
		/** The dsc. */
		DSC(11),
		/** The bct. */
		BCT(12),
		/** The lst. */
		LST(13),
		/** The ele. */
		ELE(14),
		/** The cod. */
		COD(15),
		/** The reinit. */
		REINIT(16),
		/** The game srt. */
		GAME_SRT(17),
		/** The srv time. */
		SRV_TIME(18);

		/** The code. */
		private int code;

		/**
		 * Instantiates a new message type.
		 *
		 * @param c
		 *            the c
		 */
		private MessageType(int c) {
			code = c;
		}

		/**
		 * Gets the code.
		 *
		 * @return the code
		 */
		public int getCode() {
			return code;
		}
	}

	/**
	 * Main constructor for this class.
	 *
	 * @param nodeID
	 *            the node id
	 * @param messageType
	 *            the message type
	 * @param message
	 *            the message
	 */
	public BodyMessage(int nodeID, MessageType messageType, Object message) {
		this.nodeID = nodeID;
		this.messageType = messageType;
		this.message = message;
	}

	/**
	 * Second constructor for this class.
	 *
	 * @param gamePlayerInfo
	 *            the game player info
	 * @param messageType
	 *            the message type
	 * @param message
	 *            the message
	 */
	public BodyMessage(GamePlayerInfo gamePlayerInfo, MessageType messageType, Object message) {
		this.mGamePlayerInfo = gamePlayerInfo;
		this.messageType = messageType;
		this.message = message;
	}

	/**
	 * Returns the client ID.
	 *
	 * @return the node id
	 */
	public int getNodeID() {
		return nodeID;
	}

	/**
	 * Returns the message type.
	 *
	 * @return the message type
	 */
	public MessageType getMessageType() {
		return messageType;
	}

	/**
	 * Returns the generic message.
	 *
	 * @return the message
	 */
	public Object getMessage() {
		return message;
	}

	/**
	 * Sets the message type.
	 *
	 * @param messageType
	 *            the new message type
	 */
	public void setMessageType(MessageType messageType) {
		this.messageType = messageType;
	}

	/**
	 * Sets the message to be sent.
	 *
	 * @param message
	 *            the new message
	 */
	public void setMessage(Object message) {
		this.message = message;
	}

	/**
	 * Returns the player info of this message.
	 *
	 * @return the game player info
	 */
	public GamePlayerInfo getGamePlayerInfo() {
		return this.mGamePlayerInfo;
	}

	/**
	 * Sets the player info of this message.
	 *
	 * @param gamePlayerInfo
	 *            the new game player info
	 */
	public void setGamePlayerInfo(GamePlayerInfo gamePlayerInfo) {
		this.mGamePlayerInfo = gamePlayerInfo;
	}

	/**
	 * Sets the timestamp.
	 *
	 * @param timestamp
	 *            the new timestamp
	 */
	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	/**
	 * Gets the timestamp.
	 *
	 * @param timestamp
	 *            the timestamp
	 * @return the timestamp
	 */
	public long getTimestamp(long timestamp) {
		return this.timestamp;
	}
}
