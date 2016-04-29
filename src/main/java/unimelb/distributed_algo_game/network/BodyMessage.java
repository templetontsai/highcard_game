package unimelb.distributed_algo_game.network;

import java.io.Serializable;

/**
 * 
 * @author Lupiya
 *This class is generic type to carry JSON data in messages
 */
public class BodyMessage implements Serializable{
	private int clientID;
	private MessageType messageType;
	private Object message;
	
	public enum MessageType {
		ACK(0),
		BCT(1),
		CRD(2),
		DSC(3);
		
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
	public BodyMessage(int clientID, MessageType messageType, Object message){
		this.clientID = clientID;
		this.messageType = messageType;
		this.message = message;
	}
	
	/**
	 * Returns the client ID
	 */
	public int getClientID(){ return clientID; }
	
	/**
	 * Returns the message type
	 */
	public MessageType getMessageType(){ return messageType; }
	
	/**
	 * Returns the generic message
	 */
	public Object getMessage(){ return message; }
	
	/**
	 * Sets the client ID
	 */
	public void setClientID(int clientID){
		this.clientID = clientID;
	}
	
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
}
