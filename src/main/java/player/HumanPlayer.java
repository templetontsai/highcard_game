package player;

import java.util.Scanner;

import player.PlayerState.GameState;

/**
 * @author Ting-Ying Tsai
 *
 */
/**
 * @author templeton
 *
 */
public class HumanPlayer extends Player {

	
	private static PlayerState playerState = null;

	/**
	 *Public constructor that initializes a player object using name, id, game state and score
	 */
	public HumanPlayer(String name, int id) {
		super(name, id, playerState, new PlayerScore(id));
	}

	public void run() {
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

	}
	


}
