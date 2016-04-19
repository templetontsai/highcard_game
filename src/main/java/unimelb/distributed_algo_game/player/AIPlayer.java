/*
 * 
 */
package unimelb.distributed_algo_game.player;

import unimelb.distributed_algo_game.state.PlayerState;
import unimelb.distributed_algo_game.state.PlayerState.GameState;

// TODO: Auto-generated Javadoc
/**
 * The Class AIPlayer.
 *
 * @author Ting-Ying Tsai
 */
public class AIPlayer extends Player {

	/** The player state. */
	private static PlayerState playerState = null;

	/**
	 * Public constructor that initializes a player object using name, id, game
	 * state and score.
	 *
	 * @param name
	 *            the name
	 * @param id
	 *            the id
	 */
	public AIPlayer(String name, int id) {
		super(name, id, playerState, new PlayerScore(id));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Runnable#run()
	 */
	public void run() {
		this.setPlayStatus(GameState.Play);
		while (playerState.play()) {
			showHand();
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see unimelb.distributed_algo_game.player.Player#update()
	 */
	@Override
	public void update() {
		// TODO Auto-generated method stub

	}

}
