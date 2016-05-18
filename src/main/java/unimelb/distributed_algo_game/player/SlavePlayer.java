/*
 * 
 */
package unimelb.distributed_algo_game.player;

import javax.swing.JPanel;

import unimelb.distributed_algo_game.network.GameClient;
import unimelb.distributed_algo_game.network.GameClientSocketManager;
import unimelb.distributed_algo_game.network.GameServer;
import unimelb.distributed_algo_game.network.gui.MainGamePanel;
import unimelb.distributed_algo_game.network.utils.Utils;
import unimelb.distributed_algo_game.state.GameState;

// TODO: Auto-generated Javadoc
/**
 * The Class AIPlayer.
 *
 * @author Ting-Ying Tsai
 */
public class SlavePlayer extends Player {

	/** The game client. */
	private GameClient gameClient = null;
	/** The game client thread. */
	private Thread gameClientThread = null;

	/** The game server. */
	private GameServer gameServer = null;

	/** The game server thread. */
	private Thread gameServerThread = null;

	/** The m panel. */
	private MainGamePanel mPanel = null;

	/** The m game client socket manager. */
	private GameClientSocketManager mGameClientSocketManager = null;

	/**
	 * Public constructor that initializes a player object using name, id, game
	 * state and score.
	 *
	 * @param name
	 *            the name
	 * @param gamePlayerInfo
	 *            the game player info
	 * @param gameServerInfo
	 *            the game server info
	 * @param panel
	 *            the panel
	 */
	public SlavePlayer(String name, GamePlayerInfo gamePlayerInfo, GamePlayerInfo gameServerInfo, MainGamePanel panel) {
		super(name, gamePlayerInfo, GameState.NONE, gameServerInfo);
		gameClient = new GameClient(this, gameServerInfo.getIPAddress(), gameServerInfo.getPort(), true);
		gameServer = GameServer.getInstance();
		this.mPanel = panel;
		mGameClientSocketManager = new GameClientSocketManager(this);
		mGameClientSocketManager.setPanel(panel);

	}

	/**
	 * Instantiates a new slave player.
	 *
	 * @param gamePlayerInfo
	 *            the game player info
	 */
	public SlavePlayer(GamePlayerInfo gamePlayerInfo) {
		super("Slave", gamePlayerInfo, GameState.NONE);
		gameServer = GameServer.getInstance();
	}

	/**
	 * Runs the main thread of the AI player.
	 */
	public void play() {

		gameServer.setPlayer(this);
		// Set panel and this panel ref and will only be used when get elected
		// as new leader
		gameServer.setPanel((MainGamePanel) mPanel);

		mGameClientSocketManager.setGameServer(gameServer);

		gameServer.setGameClientSocketManager(mGameClientSocketManager);
		gameServerThread = new Thread(gameServer);
		gameServer.connect();
		gameServerThread.start();

		gameClient.setPanel(mPanel);
		gameClient.setClientSocketManager(mGameClientSocketManager);
		gameClientThread = new Thread(gameClient);
		gameClient.connect();

		gameClientThread.setName("Slave Player Socket Thread0");
		gameClientThread.start();
		gameServer.setGameClient(gameClient);
		mGameClientSocketManager.setSocketClientToDealer(gameClient);
		gameClient.play();

	}

	/**
	 * Runs an update.
	 */
	@Override
	public void update() {
		// TODO Auto-generated method stub

	}

	/**
	 * Request card from dealer.
	 */
	public void requestCardFromDealer() {
		// gameClient.requestCard();

		if (gameServer.getNumofNodes() >= 1) {

			mGameClientSocketManager.sendRequestServerTime();
			System.out.println("1requestCardFromDealer");

			while (!mGameClientSocketManager.getReply())
				;
			System.out.println("2requestCardFromDealer");
			gameClient.requestCard();
			gameServer.setIsCRTRequested(false, Utils.getProcessTimestamp());
		} else {
			gameClient.requestCard();
		}

	}

	/**
	 * Disconnect client.
	 */
	public void disconnectClient() {
		gameClient.disconnect();
	}

	/**
	 * Gets the panel.
	 *
	 * @return the panel
	 */
	public JPanel getPanel() {
		return mPanel;
	}

}
