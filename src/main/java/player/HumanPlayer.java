package player;

import unimelb.distributed_algo_game_network.GameClient;
import unimelb.distributed_algo_game_network.GameServer;

import player.PlayerState.GameState;

/**
 * @author Ting-Ying Tsai
 *
 */

public class HumanPlayer extends Player {


	private boolean gameIsOver = false;
	private static PlayerState playerState = null;
	private GameClient gameClient = null;
	private GameServer gameServer = null;
	private Thread gameClientThread = null;
	private Thread gameServerThread = null;
	
	/**
	 *Public constructor that initializes a player object using name, id, game state and score
	 */
	public HumanPlayer(String name, int id) {
		super(name, id, playerState, new PlayerScore(id));
		gameClient = GameClient.getInstance();
		gameServer = GameServer.getInstance();

	}
	




	public void run() {
		gameServerThread = new Thread(gameServer);
		gameServerThread.start();
		gameClient.setPlayer(this);
		gameClientThread = new Thread(gameClient);
		gameClientThread.start();
		//gameServer.stopServer(true);
		
		/*
		Scanner scanner = null;

		this.setPlayStatus(GameState.Play);
		
		while (playerState.play()) {

			System.out.println("Please select from the deck between 1-52");
			scanner = new Scanner(System.in);
			int option = scanner.nextInt();

			if (option > 0 && option < 53) {

				showCard(option);
				this.setPlayStatus(GameState.Leave);
			} else {
				System.out.println("Wrong option");
			}
		}

		scanner.close();
		*/

	}
	


}
