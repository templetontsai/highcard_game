/*
 * 
 */
package unimelb.distributed_algo_game.player;

// TODO: Auto-generated Javadoc
/**
 * The Class PlayerScore.
 *
 * @author David This class is responsible for maintaining a player's score
 */
public class PlayerScore {

	/** The player id. */
	// Initialize the class variables
	private int playerID = 0;

	/** The score. */
	private int score = 0;

	/**
	 * Constructor for the class that initializes the object.
	 *
	 * @param playerID
	 *            the player id
	 */
	public PlayerScore(int playerID) {
		this.playerID = playerID;
	}

	/**
	 * Returns the id of the player.
	 *
	 * @return the player id
	 */
	public int getPlayerID() {
		return playerID;
	}

	/**
	 * Returns the current score.
	 *
	 * @return the score
	 */
	public int getScore() {
		return score;
	}

	/**
	 * Updates the player score.
	 */
	public void updateScore() {
		score++;
	}

}
