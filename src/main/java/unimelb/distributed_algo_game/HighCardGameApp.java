/*
 * 
 */
package unimelb.distributed_algo_game;

import unimelb.distributed_algo_game.network.gui.MainGameFrameGUI;

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
	 * This runs the game server of the game.
	 *
	 * @param id
	 *            the id
	 */
	public static void runGame(int id) {
		MainGameFrameGUI mainGui = new MainGameFrameGUI("High Card Game", id);

	}

}
