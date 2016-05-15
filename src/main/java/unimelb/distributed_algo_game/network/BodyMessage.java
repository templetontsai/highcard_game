package unimelb.distributed_algo_game.network;

import java.io.Serializable;

import unimelb.distributed_algo_game.player.GamePlayerInfo;

/**
 * 
 * @author Lupiya
 *This class is generic type to carry JSON data in messages
 */
public class BodyMessage implements Serializable{
	private int nodeID;
	private GamePlayerInfo mGamePlayerInfo;
	private MessageType messageType;
	private Object message;
	
	public enum ACKCode {
		NODE_ID_RECEIVED(0),
		CARD_RECEIVED(1),
		STILL_ALIVE(2),
		CRT_RPY(3);
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
	
	public enum BCTCode {
		BCT_DRAW_MSG(0),
		BCT_CLIENT_LST(1);
		/** The code. */
		private int code;

		
		private BCTCode(int c) {
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
		CON(0),
		ACK(1),
		BCT_RST(2),
		BCT_LST(3),
		BCT_RDY(4),
		BCT_CRD(5),
		BCT_CRT(6),
		CRD(7),
		DSC(8),
		BCT(9),
		LST(10),
		ELE(11),
		COD(12);

		
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
	public BodyMessage(int nodeID, MessageType messageType, Object message){
		this.nodeID = nodeID;
		this.messageType = messageType;
		this.message = message;
	}
	
	public BodyMessage(GamePlayerInfo gamePlayerInfo, MessageType messageType, Object message){
		this.mGamePlayerInfo = gamePlayerInfo;
		this.messageType = messageType;
		this.message = message;
	}
	
	/**
	 * Returns the client ID
	 */
	public int getNodeID(){ return nodeID; }
	
	/**
	 * Returns the message type
	 */
	public MessageType getMessageType(){ return messageType; }
	
	/**
	 * Returns the generic message
	 */
	public Object getMessage(){ return message; }
	
	
	/**
	 * Sets the message type
	 */
	public void setMessageType(MessageType messageType){
		this.messageType = messageType;
	}
	
	/**
	 * Sets the message to be sent
	 */
	public void setMessage(Object message){
		this.message = message;
	}
	
	public GamePlayerInfo getGamePlayerInfo() {
		return this.mGamePlayerInfo;
	}
	
	public void setGamePlayerInfo(GamePlayerInfo gamePlayerInfo){
		this.mGamePlayerInfo = gamePlayerInfo;
	}
}
