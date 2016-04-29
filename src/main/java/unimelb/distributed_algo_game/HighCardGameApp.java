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
	    if (args.length > 0) {
			if (Integer.parseInt(args[0]) == 0) {
				runGameServer();
			} else {
				runGameClient();
			}
		}

	}
	
	/**
	 * This runs the game server of the game
	 */
	public static void runGameServer(){
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
		
		runGameClient();
	}
	
	/**
	 * This runs the game client of the game
	 */
	public static void runGameClient(){
		System.out.println("Client Joins the game");
		Player p = new AIPlayer("AI 1", 2);
		Thread t = new Thread(p);
		t.start();
		/**Player p2 = new AIPlayer("AI 2", 3);
		Thread t2 = new Thread(p2);
		t2.start();**/
	}

}
