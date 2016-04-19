/*
 * 
 */
package unimelb.distributed_algo_game.player;

import unimelb.distributed_algo_game.network.GameClient;
import unimelb.distributed_algo_game.network.GameServer;
import unimelb.distributed_algo_game.state.PlayerState;

// TODO: Auto-generated Javadoc
/**
 * The Class HumanPlayer.
 *
 * @author Ting-Ying Tsai
 */

public class HumanPlayer extends Player {

	/** The game is over. */
	private boolean gameIsOver = false;

	/** The player state. */
	private static PlayerState playerState = null;

	/** The game client. */
	private GameClient gameClient = null;

	/** The game server. */
	private GameServer gameServer = null;

	/** The game client thread. */
	private Thread gameClientThread = null;

	/** The game server thread. */
	private Thread gameServerThread = null;

	/**
	 * Public constructor that initializes a player object using name, id, game
	 * state and score.
	 *
	 * @param name
	 *            the name
	 * @param id
	 *            the id
	 */
	public HumanPlayer(String name, int id) {
		super(name, id, playerState, new PlayerScore(id));
		gameClient = GameClient.getInstance();
		gameServer = GameServer.getInstance();

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Runnable#run()
	 */
	public void run() {
		gameServerThread = new Thread(gameServer);
		gameServer.connect();
		gameServerThread.start();
		gameClient.setPlayer(this);
		gameClient.connect();
		gameClientThread = new Thread(gameClient);
		gameClientThread.start();
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		/*
		 * Scanner scanner = null;
		 * 
		 * this.setPlayStatus(GameState.Play);
		 * 
		 * while (playerState.play()) {
		 * 
		 * System.out.println("Please select from the deck between 1-52");
		 * scanner = new Scanner(System.in); int option = scanner.nextInt();
		 * 
		 * if (option > 0 && option < 53) {
		 * 
		 * showCard(option); this.setPlayStatus(GameState.Leave); } else {
		 * System.out.println("Wrong option"); } }
		 * 
		 * scanner.close();
		 */

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
