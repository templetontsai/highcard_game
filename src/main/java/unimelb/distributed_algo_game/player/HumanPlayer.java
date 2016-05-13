/*
 * 
 */
package unimelb.distributed_algo_game.player;

import javax.swing.JPanel;

import unimelb.distributed_algo_game.network.GameClient;
import unimelb.distributed_algo_game.network.GameServer;
import unimelb.distributed_algo_game.network.gui.MainGameLoginClientPanel;
import unimelb.distributed_algo_game.network.gui.MainGameLoginDealerPanel;
import unimelb.distributed_algo_game.state.GameState;


// TODO: Auto-generated Javadoc
/**
 * The Class HumanPlayer.
 *
 * @author Ting-Ying Tsai
 */

public class HumanPlayer extends Player {

	/** The game is over. */
	private boolean gameIsOver = false;


	/** The game client. */
	private GameClient gameClient = null;

	/** The game server. */
	private GameServer gameServer = null;

	/** The game client thread. */
	private Thread gameClientThread = null;

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
	public HumanPlayer(String name, GamePlayerInfo gamePlayerInfo, JPanel panel) {
		super(name, gamePlayerInfo, GameState.NONE, new PlayerScore());
		gameClient = GameClient.getInstance();
		gameServer = GameServer.getInstance();
		this.mPanel = panel;

	}

	/**
	 * Runs the main thread of the human player
	 */
	public void run() {
		
		gameServer.setPlayer(this);
		
		gameServer.setPanel((MainGameLoginDealerPanel)mPanel);
		
		
		gameServerThread = new Thread(gameServer);
		gameServer.connect();
		gameServerThread.start();
		/*
		gameClient.setPlayer(this);
		gameClientThread = new Thread(gameClient);
		gameClient.connect();
		gameClientThread.start();
		*/
		this.setGameState(GameState.PLAY);
		while(this.getGameState() == GameState.PLAY) {
			if (this.isDealer()) {
				//TODO do dealer stuff here, checking connection, updating stuff
				//System.out.println("dealer/node0 is playing game");
				//Card card = this.getCard(1);
				//gameServer.sendCard(card, 1);
			} else {
				//TODO do client stuff here, checking connection, updating stuff
				//System.out.println("client is playing game");
			
				
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

}
