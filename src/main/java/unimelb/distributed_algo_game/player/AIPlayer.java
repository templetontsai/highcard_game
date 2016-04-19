package unimelb.distributed_algo_game.player;

import java.util.Observable;

import unimelb.distributed_algo_game.state.PlayerState;
import unimelb.distributed_algo_game.state.PlayerState.GameState;

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

	@Override
	public void update() {
		// TODO Auto-generated method stub
		
	}

	
}
