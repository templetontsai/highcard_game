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
public class SlavePlayer extends Player {

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
	public SlavePlayer(String name, GamePlayerInfo gamePlayerInfo, GamePlayerInfo gameServerInfo, JPanel panel) {
		super(name, gamePlayerInfo, GameState.NONE, gameServerInfo);
		gameClient = GameClient.getInstance();
		gameServer = GameServer.getInstance();
		this.mPanel = panel;
	}

	public SlavePlayer(GamePlayerInfo gamePlayerInfo) {
		super("Slave", gamePlayerInfo, GameState.NONE);
		gameClient = GameClient.getInstance();
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

		gameClient.setPlayer(this);
		gameClient.setPanel((MainGameLoginClientPanel) mPanel);
		gameClient.setServerDetails();
		gameClientThread = new Thread(gameClient);
		gameClient.connect();

		gameClientThread.setName("Slave Player Socket Thread");
		gameClientThread.start();

		gameServer.setGameClient(gameClient);
		gameClient.play();

	}
	
	public void rePlay(){
		gameClient.setPlayer(this);
		gameClient.setPanel((MainGameLoginClientPanel) mPanel);
		gameClient.setServerDetails();
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
		//TODO implement recart, do a blocking call 
		gameServer.broadcastCRT();
		System.out.println("requestCardFromDealer");
		while(!gameServer.getReply())
			;
		gameClient.requestCard();
	}

	public void disconnectClient() {
		gameClient.disconnect();
	}

}
