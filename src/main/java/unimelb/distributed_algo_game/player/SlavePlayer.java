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

	private MainGamePanel mPanel = null;

	private GameClientSocketManager mGameClientSocketManager = null;

	/**
	 * Public constructor that initializes a player object using name, id, game
	 * state and score.
	 *
	 * @param name
	 *            the name
	 * @param id
	 *            the id
	 */
	public SlavePlayer(String name, GamePlayerInfo gamePlayerInfo, GamePlayerInfo gameServerInfo, MainGamePanel panel) {
		super(name, gamePlayerInfo, GameState.NONE, gameServerInfo);
		gameClient = new GameClient(this, gameServerInfo.getIPAddress(), gameServerInfo.getPort());
		gameServer = GameServer.getInstance();
		this.mPanel = panel;
		mGameClientSocketManager = new GameClientSocketManager(this);
		
	}

	public SlavePlayer(GamePlayerInfo gamePlayerInfo) {
		super("Slave", gamePlayerInfo, GameState.NONE);
		gameServer = GameServer.getInstance();
	}

	/**
	 * Runs the main thread of the AI player
	 */
	public void play() {

		gameServer.setPlayer(this);
		gameServerThread = new Thread(gameServer);
		gameServer.connect();
		gameServerThread.start();

		gameClient.setPanel(mPanel);
		gameClientThread = new Thread(gameClient);
		gameClient.connect();

		gameClientThread.setName("Slave Player Socket Thread0");
		gameClientThread.start();
		// Adding to the first gameclient and pass ref for manager later run
		// this manager thread once it gets the list of nodes in the network
		//mGameClientSocketManager.addSocketClient(gameClient);
		gameClient.setClientSocketManager(mGameClientSocketManager);
		// TODO check the need for this after refactoring
		//gameServer.setGameClient(gameClient);

		gameClient.play();

	}

	public void rePlay() {
		gameClient = null;
		gameClient = new GameClient(this);
		gameClient.setPanel(mPanel);
		gameClientThread = new Thread(gameClient);
		gameClient.connect();

		gameClientThread.setName("Slave Player Socket Thread");
		gameClientThread.start();

		gameServer.setGameClient(gameClient);
		gameClient.play();
	}

	/**
	 * Runs an update
	 */
	@Override
	public void update() {
		// TODO Auto-generated method stub

	}

	public void requestCardFromDealer() {
		// gameClient.requestCard();

		if (gameServer.getNumofNodes() >= 1) {
			long timestamp = Utils.getProcessTimestamp();
			gameServer.setIsRequested(true, timestamp);
			
			mGameClientSocketManager.broadcastCRT(timestamp);
			System.out.println("requestCardFromDealer");
			while (!mGameClientSocketManager.getReply())
				;
			gameClient.requestCard();
			gameServer.setIsRequested(false, Utils.getProcessTimestamp());
		} else {
			gameClient.requestCard();
		}

	}

	public void disconnectClient() {
		gameClient.disconnect();
	}

	public JPanel getPanel() {
		return mPanel;
	}

}
