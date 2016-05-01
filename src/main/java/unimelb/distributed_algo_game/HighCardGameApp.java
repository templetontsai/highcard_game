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
	    	runGame(new Integer(args[0]).intValue());
		}

	}
	
	/**
	 * This runs the game server of the game
	 */
	public static void runGame(int id){
		
		if(id == 0) {
			System.out.println("Dealer/Node0 Starts the game");		
			// Initialize players
			Player p = new HumanPlayer("Dealer", id);
			p.setDealer(true);
			Thread t = new Thread(p);
			t.start();
			//Initialize game token for mutex
			Token gameToken = new Token();
			//Initialize queue for mutex control
			ArrayList<Player> tokenRequests = new ArrayList<Player>();
		} else {
			Player p = new AIPlayer("AI 1", id);
			Thread t = new Thread(p);
			t.start();
		}
		
		
		
	}
	
	

}
