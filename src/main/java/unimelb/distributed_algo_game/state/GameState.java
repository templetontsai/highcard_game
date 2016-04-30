/*
 * 
 */
package unimelb.distributed_algo_game.state;


// TODO: Auto-generated Javadoc
/**
 * The Class PlayState.
 *
 * @author Ting_ying Tsai
 */
public enum GameState {
	NONE(0),
	PLAY(1),
	DEALER(2),
	GETCARD(3),
	LEAVE(4);
	
	private int statusCode;
	
	/**
	 * Constructor for the game state
	 * @param c
	 */
	private GameState(int c) {
		statusCode = c;
	}
	
	/**
	 * Returns the status code
	 * @return
	 */
	public int getCode() {
		return statusCode;
	}

}
