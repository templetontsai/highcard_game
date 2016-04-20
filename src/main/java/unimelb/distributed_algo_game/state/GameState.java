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
	
	private GameState(int c) {
		statusCode = c;
	}
	
	public int getCode() {
		return statusCode;
	}

}
