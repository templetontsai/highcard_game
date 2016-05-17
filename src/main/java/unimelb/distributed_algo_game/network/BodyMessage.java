package unimelb.distributed_algo_game.network;

import java.io.Serializable;

import unimelb.distributed_algo_game.player.GamePlayerInfo;

/**
 * 
 * @author Lupiya This class is generic type to carry JSON data in messages
 */
public class BodyMessage implements Serializable {
	private int nodeID;
	private GamePlayerInfo mGamePlayerInfo;
	private MessageType messageType;
	private Object message;

	public enum ACKCode {
		NODE_ID_RECEIVED(0), CARD_RECEIVED(1), CLIENT_STILL_ALIVE(2), SERVER_STILL_ALIVE(3), CRT_RPY(4);
		/** The code. */
		private int code;

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

	

	public enum MessageType {
		CON(0), ACK(1), BCT_RST(2), BCT_NODE_LST(3), BCT_CLIENT_LST(4), BCT_NODE_UPT(5), BCT_CLIENT_UPT(6), BCT_RDY(7), BCT_CRD(8), BCT_CRT(9),

		CRD(10), DSC(11), BCT(12), LST(13), ELE(14), COD(15);

		/** The code. */
		private int code;

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
	 * Main constructor for this class
	 */
	public BodyMessage(int nodeID, MessageType messageType, Object message) {
		this.nodeID = nodeID;
		this.messageType = messageType;
		this.message = message;
	}

	public BodyMessage(GamePlayerInfo gamePlayerInfo, MessageType messageType, Object message) {
		this.mGamePlayerInfo = gamePlayerInfo;
		this.messageType = messageType;
		this.message = message;
	}

	/**
	 * Returns the client ID
	 */
	public int getNodeID() {
		return nodeID;
	}

	/**
	 * Returns the message type
	 */
	public MessageType getMessageType() {
		return messageType;
	}

	/**
	 * Returns the generic message
	 */
	public Object getMessage() {
		return message;
	}

	/**
	 * Sets the message type
	 */
	public void setMessageType(MessageType messageType) {
		this.messageType = messageType;
	}

	/**
	 * Sets the message to be sent
	 */
	public void setMessage(Object message) {
		this.message = message;
	}

	public GamePlayerInfo getGamePlayerInfo() {
		return this.mGamePlayerInfo;
	}

	public void setGamePlayerInfo(GamePlayerInfo gamePlayerInfo) {
		this.mGamePlayerInfo = gamePlayerInfo;
	}
}
