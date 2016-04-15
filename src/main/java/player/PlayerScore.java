package player;
/**
 * 
 * @author David
 *This class is responsible for maintaining a player's score
 */
public class PlayerScore {
	//Initialize the class variables
	private int playerID = 0;
	private int score = 0;
	
	/**
	 * Constructor for the class that initializes the object
	 */
	public PlayerScore(int playerID){
		this.playerID = playerID;
	}
	
	/**
	 * Returns the id of the player
	 */
	public int getPlayerID(){
		return playerID;
	}
	
	/**
	 * Returns the current score
	 */
	public int getScore(){
		return score;
	}
	
	/**
	 * Updates the player score
	 */
	public void updateScore(){
		score++;
	}
	

}
