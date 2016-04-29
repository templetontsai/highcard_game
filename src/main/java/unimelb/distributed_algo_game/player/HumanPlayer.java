/*
 * 
 */
package unimelb.distributed_algo_game.player;

import unimelb.distributed_algo_game.network.GameClient;
import unimelb.distributed_algo_game.network.GameServer;
import unimelb.distributed_algo_game.pokers.Card;
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
		super(name, id, GameState.NONE, new PlayerScore(id));
		gameClient = GameClient.getInstance();
		gameServer = GameServer.getInstance();

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Runnable#run()
	 */
	public void run() {

		if (this.isDealer()) {

			gameServer.setPlayer(this);
			gameServerThread = new Thread(gameServer);
			gameServer.connect();
			gameServerThread.start();
			
			this.setGameState(GameState.PLAY);
			while(this.getGameState() == GameState.PLAY) {
				gameServer.broadcastToClients("hi");
				Card card = this.getCard(1);
				gameServer.sendCard(card, 1);
				
			}

		} else {

			gameClient.setPlayer(this);
			gameClientThread = new Thread(gameClient);
			gameClient.connect();
			gameClientThread.start();
			
			this.setGameState(GameState.PLAY);
			while(this.getGameState() == GameState.PLAY) {
				Object obj = gameClient.receiveMessage();
				if(obj != null) {
					((Card)obj).showCard();
					gameClient.disconnect();
					this.setGameState(GameState.LEAVE);
				}
			}
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
