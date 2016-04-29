package unimelb.distributed_algo_game.token;

/**
 * 
 * @author Lupiya
 *This class is responsible for granting access to the critical section for mutual exclusion
 */
public class Token {

	boolean releasedState;
	int playerID;
	
	/**
	 * Token is initialized as released and held by the server
	 */
	public Token(){
		releasedState = true;
		playerID = 0;
	}
	
	/**
	 * This returns the release state of the token
	 */
	public boolean isReleased(){
		return releasedState;
	}
	
	/**
	 * This returns the ID of the player currently holding the token
	 */
	public int getCurrentHolder(){
		return playerID;
	}
	
	/**
	 * This grants the token to a player and makes the token held
	 */
	public void holdToken(int playerID){ 
		this.releasedState = false;
		this.playerID = playerID;
	}
	
	/**
	 * This releases a token from the current player holding it
	 */
	public void releaseToken(){
		this.releasedState = true;
		this.playerID = 0;
	}
}
