/*
 * 
 */
package unimelb.distributed_algo_game.player;

import javax.swing.JPanel;

import unimelb.distributed_algo_game.network.GameClient;
import unimelb.distributed_algo_game.network.GameServer;

import unimelb.distributed_algo_game.network.gui.MainGamePanel;
import unimelb.distributed_algo_game.state.GameState;

// TODO: Auto-generated Javadoc
/**
 * The Class HumanPlayer.
 *
 * @author Ting-Ying Tsai
 */

public class DealerPlayer extends Player {



	/** The game client. */
	private GameClient gameClient = null;

	/** The game server. */
	private GameServer gameServer = null;

	/** The game client thread. */
	private Thread gameClientThread = null;

	/** The game server thread. */
	private Thread gameServerThread = null;

	private JPanel mMainGameLoginDealerPanel = null;

	/**
	 * Public constructor that initializes a player object using name, id, game
	 * state and score.
	 *
	 * @param name
	 *            the name
	 * @param id
	 *            the id
	 */
	public DealerPlayer(String name, GamePlayerInfo gamePlayerInfo, JPanel panel) {
		super(name, gamePlayerInfo, GameState.NONE, new PlayerScore());
		gameClient = GameClient.getInstance();
		gameServer = GameServer.getInstance();
		this.mMainGameLoginDealerPanel = panel;

	}


	public void play() {

		gameServer.setPlayer(this);

		gameServer.setPanel((MainGamePanel) mMainGameLoginDealerPanel);

		gameServerThread = new Thread(gameServer);
		gameServer.connect();
		gameServerThread.start();

	}

	/**
	 * Runs an update
	 */
	@Override
	public void update() {
		// TODO Auto-generated method stub

	}

	public void dealerDrawnCard() {
		gameServer.dealerDrawnCard();
	}
	
	public void restartServer(){
		gameServer = null;
		gameServer = GameServer.getInstance();
		gameServer.setPlayer(this);
		gameServer.setPanel((MainGamePanel) mMainGameLoginDealerPanel);
		gameServerThread = new Thread(gameServer);
		gameServer.connect();
		gameServerThread.start();
	}

}
