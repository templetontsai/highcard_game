package player;

import unimelb.distributed_algo_game_network.GameClient;
import unimelb.distributed_algo_game_network.GameServer;



/**
 * @author Ting-Ying Tsai
 *
 */
public class HumanPlayer extends Player {

	private boolean gameIsOver = false;
	private GameClient gameClient = null;
	private GameServer gameServer = null;
	private Thread gameClientThread = null;
	private Thread gameServerThread = null;
	
	public HumanPlayer(String name, int id) {
		super(name, id);
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
		while(!gameIsOver) {
		
			
			System.out.println("Please select from the deck between 1-52");
			scanner = new Scanner(System.in);
			int option = scanner.nextInt();
			client.setPlayer(this);
			client.startClient();

			if(option>0 && option<53){
				showCard(option);
				terminateGame(true);
			}else{
				System.out.println("Wrong option");
			}
		}
		
		scanner.close();
		*/

	}
	
	public void terminateGame(boolean gameIsOver) {
		this.gameIsOver = gameIsOver;
	}
	
}
