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

	/** The m main game login dealer panel. */
	private JPanel mMainGameLoginDealerPanel = null;

	/** The game size. */
	private int gameSize = -1;

	/**
	 * Public constructor that initializes a player object using name, id, game
	 * state and score.
	 *
	 * @param name
	 *            the name
	 * @param gamePlayerInfo
	 *            the game player info
	 * @param panel
	 *            the panel
	 */
	public DealerPlayer(String name, GamePlayerInfo gamePlayerInfo, JPanel panel) {
		super(name, gamePlayerInfo, GameState.NONE, new PlayerScore());
		gameClient = new GameClient(this);
		gameServer = GameServer.getInstance();
		this.mMainGameLoginDealerPanel = panel;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see unimelb.distributed_algo_game.player.Player#play()
	 */
	public void play() {

		gameServer.setPlayer(this);
		if (gameSize != -1)
			gameServer.setGameSize(gameSize);

		gameServer.setPanel((MainGamePanel) mMainGameLoginDealerPanel);

		gameServerThread = new Thread(gameServer);
		gameServer.connect();
		gameServerThread.start();

	}

	/**
	 * Runs an update.
	 */
	@Override
	public void update() {
		// TODO Auto-generated method stub

	}

	/**
	 * Dealer drawn card.
	 */
	public void dealerDrawnCard() {
		gameServer.dealerDrawnCard();
	}

	/**
	 * Sets the game size.
	 *
	 * @param gameSize
	 *            the new game size
	 */
	public void setGameSize(int gameSize) {
		this.gameSize = gameSize;
	}

}
