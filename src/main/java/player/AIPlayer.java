package player;

import player.PlayerState.GameState;

/**
 * @author Ting-Ying Tsai
 *
 */
public class AIPlayer extends Player{
	private static PlayerState playerState = null;
	public AIPlayer(String name, int id) {
		super(name, id, playerState);
	}

	public void run() {
		this.setPlayStatus(GameState.Play);
		while (playerState.play()) {
			showHand();
		}

	}

	
}
