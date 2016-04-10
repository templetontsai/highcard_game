package player;



/**
 * @author Ting-Ying Tsai
 *
 */
public class AIPlayer extends Player{
	private boolean gameIsOver = false;

	public AIPlayer(String name, int id) {
		super(name, id);
	}

	public void run() {
		
		while (!gameIsOver) {
			showHand();
		}

	}

	public void terminateGame(boolean gameIsOver) {
		this.gameIsOver = gameIsOver;
	}
	
}
