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

	/** The none. */
	NONE(0),
	/** The play. */
	PLAY(1),
	/** The dealer. */
	DEALER(2),
	/** The getcard. */
	GETCARD(3),
	/** The leave. */
	LEAVE(4);

	/** The status code. */
	private int statusCode;

	/**
	 * Constructor for the game state.
	 *
	 * @param c
	 *            the c
	 */
	private GameState(int c) {
		statusCode = c;
	}

	/**
	 * Returns the status code.
	 *
	 * @return the code
	 */
	public int getCode() {
		return statusCode;
	}

}
