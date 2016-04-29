package unimelb.distributed_algo_game.network;

import java.io.Serializable;

/**
 * 
 * @author Lupiya
 *This class is generic type to carry JSON data in messages
 */
public class BodyMessageJSON implements Serializable{
	int clientID;
	String messageType;
	Object message;
	
	/**
	 * Main constructor for this class
	 */
	public BodyMessageJSON(int clientID, String messageType, Object message){
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
	public String getMessageType(){ return messageType; }
	
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
	public void setMessageType(String messageType){
		this.messageType = messageType;
	}
	
	/**
	 * Sets the message to be sent
	 */
	public void setMessage(Object message){
		this.message = message;
	}
}
