package unimelb.distributed_algo_game;



import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;

import player.AIPlayer;
import player.HumanPlayer;
import player.Player;
import unimelb.distributed_algo_game.pokers.Card;
import unimelb.distributed_algo_game.pokers.Deck;
import unimelb.distributed_algo_game.pokers.Util;

public class HighCardGameApp {

	public static void main(String[] args) {
		
		
	
		Deck deck = Deck.getInstance();
		deck.shuffle();
	
		
		Player p1 = new HumanPlayer("Templeton", 1);
		
		Thread t = new Thread(p1);
		t.start();
		

		
	
	
	}

}
