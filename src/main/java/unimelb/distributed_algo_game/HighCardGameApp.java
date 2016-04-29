/*
 * 
 */
package unimelb.distributed_algo_game;

import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;

import unimelb.distributed_algo_game.network.GameClient;
import unimelb.distributed_algo_game.network.GameServer;
import unimelb.distributed_algo_game.player.AIPlayer;
import unimelb.distributed_algo_game.player.HumanPlayer;
import unimelb.distributed_algo_game.player.Player;
import unimelb.distributed_algo_game.pokers.Deck;
import unimelb.distributed_algo_game.pokers.Util;
import unimelb.distributed_algo_game.token.Token;

// TODO: Auto-generated Javadoc
/**
 * The Class HighCardGameApp.
 */
public class HighCardGameApp {

	/**
	 * The main method.
	 *
	 * @param args
	 *            the arguments
	 */
	public static void main(String[] args) {
		boolean gameRunning = true;
		Scanner input = new Scanner(System.in);
		while(gameRunning){
		  System.out.println("Select one of the options: ");
		  System.out.println("1. Create Game Lobby");
		  System.out.println("2. Join Game Lobby");
		  int choice = input.nextInt();
		  /**Check if there is already a server first on the predefined port before 
		   * allowing this player to be the server
		   */
		  if(choice==1){
			  //Check for existing server before making current player the server
			  GameServer pokerDealer = GameServer.getInstance();
		  //Let player find a server to join an existing lobby	  
		  }else if(choice==2){
		      GameClient pokerPlayer = GameClient.getInstance();
		  //Reject any other input from the player
		  }else{
			  System.out.println("Invalid choice. Please select again.");
		  }
		}
		  if (args.length > 0) {
			if (Integer.parseInt(args[0]) == 0) {
				System.out.println("Dealer Starts the game");
				
				// Initialize players
				Player p = new HumanPlayer("Dealer", 1);
				p.setDealer(true);
				Thread t = new Thread(p);
				t.start();
				//Initialize game token for mutex
				Token gameToken = new Token();
				//Initialize queue for mutex control
				ArrayList<Player> tokenRequests = new ArrayList<Player>();
				
			} else {
				System.out.println("Client Joins the game");
				Player p = new AIPlayer("AI 1", 2);
				Thread t = new Thread(p);
				t.start();
				Player p2 = new AIPlayer("AI 2", 3);
				Thread t2 = new Thread(p2);
				t2.start();
			}
		}

	}

}
