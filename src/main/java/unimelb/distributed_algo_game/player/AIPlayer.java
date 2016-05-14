/*
 * 
 */
package unimelb.distributed_algo_game.player;

import javax.swing.JPanel;

import unimelb.distributed_algo_game.network.GameClient;
import unimelb.distributed_algo_game.network.GameServer;
import unimelb.distributed_algo_game.network.gui.MainGameLoginClientPanel;
import unimelb.distributed_algo_game.state.GameState;

// TODO: Auto-generated Javadoc
/**
 * The Class AIPlayer.
 *
 * @author Ting-Ying Tsai
 */
public class AIPlayer extends Player {

	/** The game client. */
	private GameClient gameClient = null;
	/** The game client thread. */
	private Thread gameClientThread = null;

	/** The game server. */
	private GameServer gameServer = null;

	/** The game server thread. */
	private Thread gameServerThread = null;

	private JPanel mPanel = null;

	/**
	 * Public constructor that initializes a player object using name, id, game
	 * state and score.
	 *
	 * @param name
	 *            the name
	 * @param id
	 *            the id
	 */
	public AIPlayer(String name, GamePlayerInfo gamePlayerInfo, GamePlayerInfo gameServerInfo, JPanel panel) {
		super(name, gamePlayerInfo, GameState.NONE, gameServerInfo);
		gameClient = GameClient.getInstance();
		gameServer = GameServer.getInstance();
		this.mPanel = panel;
	}

	public AIPlayer(GamePlayerInfo gamePlayerInfo) {
		super("AI", gamePlayerInfo, GameState.NONE);
		gameClient = GameClient.getInstance();
		gameServer = GameServer.getInstance();
	}

	/**
	 * Runs the main thread of the AI player
	 */
	public void run() {

		gameServer.setPlayer(this);
		gameServerThread = new Thread(gameServer);
		gameServer.connect();
		gameServerThread.start();

		gameClient.setPlayer(this);
		gameClient.setPanel((MainGameLoginClientPanel) mPanel);
		gameClient.setServerDetails();
		gameClientThread = new Thread(gameClient);
		gameClient.connect();
		gameClientThread.setName("AI Player Socket Thread");
		gameClientThread.start();

		gameServer.setGameClient(gameClient);

		this.setGameState(GameState.PLAY);
		while (this.getGameState() == GameState.PLAY) {
			if (this.isDealer()) {
				// TODO do dealer stuff here, checking connection, updating
				// stuff
				// System.out.println("dealer/node0 is playing game");
				// Card card = this.getCard(1);
				// gameServer.sendCard(card, 1);
			} else {
				// TODO do client stuff here, checking connection, updating
				// stuff
				// System.out.println("client is playing game");
				gameClient.play();
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}

		}

	}

	/**
	 * Runs an update
	 */
	@Override
	public void update() {
		// TODO Auto-generated method stub

	}

	public void requestCardFromDealer() {
		gameClient.requestCard();
	}

	public void disconnectClient() {
		gameClient.disconnect();
	}

}
