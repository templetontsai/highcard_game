package player;

import java.util.Scanner;



/**
 * @author Ting-Ying Tsai
 *
 */
public class HumanPlayer extends Player {

	private boolean gameIsOver = false;
	public HumanPlayer(String name, int id) {
		super(name, id);
	}

	public void run() {
		Scanner scanner = null;
		while(!gameIsOver) {
			
			System.out.println("Enter your option: 1.Show your hand");
			scanner = new Scanner(System.in);
			int option = scanner.nextInt();
			
			switch (option) {
				case 1:
					showHand();
					break;
				case 2:
					
					break;
				default:
					System.out.println("Wrong option");
			}
		}
		
		scanner.close();

	}
	
	public void terminateGame(boolean gameIsOver) {
		this.gameIsOver = gameIsOver;
	}
	
}
