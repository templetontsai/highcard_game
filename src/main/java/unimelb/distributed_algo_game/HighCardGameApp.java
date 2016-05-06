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
		
		
		
	    if (args.length > 0) {
	    	runGame(new Integer(args[0]).intValue());
		}
	    

	}
	
	/**
	 * This runs the game server of the game
	 */
	public static void runGame(int id){
		
		if(id == 0) {
			MainGameFrameGUI mainGui = new MainGameFrameGUI("High Card Game",id);
			mainGui.setNodeID(id);

		} else {
			

			MainGameFrameGUI mainGui = new MainGameFrameGUI("High Card Game",id);
			mainGui.setClientNodeID(id);
			

		}
		
		
		
	}
	
	

}
