/*
 * 
 */
package unimelb.distributed_algo_game;

import java.util.ArrayList;

import unimelb.distributed_algo_game.network.gui.MainGameFrameGUI;
import unimelb.distributed_algo_game.player.AIPlayer;
import unimelb.distributed_algo_game.player.HumanPlayer;
import unimelb.distributed_algo_game.player.Player;
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
		
		MainGameFrameGUI mainGui = new MainGameFrameGUI("High Card Game");
		
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
			t.setName("Human Player");
			//Initialize game token for mutex
			Token gameToken = new Token();
			//Initialize queue for mutex control
			ArrayList<Player> tokenRequests = new ArrayList<Player>();
		} else {
			Player p = new AIPlayer("AI 1", id);
			Thread t = new Thread(p);
			t.setName("AI Player Thread");
			t.start();
		}
		
		
		
	}
	
	

}
