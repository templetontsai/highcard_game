package player;

import player.PlayerState.GameState;

/**
 * @author Ting-Ying Tsai
 *
 */
public class AIPlayer extends Player{
	private static PlayerState playerState = null;
	/**
	 *Public constructor that initializes a player object using name, id, game state and score
	 */
	public AIPlayer(String name, int id) {
		super(name, id, playerState, new PlayerScore(id));
	}

	public void run() {
		this.setPlayStatus(GameState.Play);
		while (playerState.play()) {
			showHand();
		}

	}

	
}
