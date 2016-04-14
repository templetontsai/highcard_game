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
		// TODO Auto-generated method stub
		
	
		Deck deck = Deck.getInstance();
		deck.shuffle();
	
		
		Player p1 = new HumanPlayer("Templeton", 1);
		Player p2 = new AIPlayer("AI 1", 2);
		Player p3 = new AIPlayer("AI 2", 3);

		Random random = new Random();
		
		ArrayList<Player> players = new ArrayList<Player>();
		players.add(p1);
		players.add(p2);
		players.add(p3);
		
		boolean isFinished = false;
		boolean roundDone = false;
		
		Scanner scanner = null;
		while(!isFinished){
		    int choice = random.nextInt(51) + 0;
		    p2.selectFromDeck(deck.getCardFromDeck(choice));
		    choice = random.nextInt(51) + 0;
		    p3.selectFromDeck(deck.getCardFromDeck(choice));
			while(!roundDone){
				System.out.println("Please select from the deck between 1-52");
				scanner = new Scanner(System.in);
				int option = scanner.nextInt()-1;
				if(option==choice){
					System.out.println("Other player already selected that card.");
				}else if(option>=0 && option<52){
					roundDone = true;
					p1.selectFromDeck(deck.getCardFromDeck(option));
				}else{
					System.out.println("Invalid selection.");
				}
			}
			System.out.println(Util.compareRank(players));
			boolean newMatch = false;
			while(!newMatch){
			   System.out.println("Do you want to continue? [Y/N]");
			   scanner = new Scanner(System.in);
			   String option = scanner.nextLine();
			   if(option.equalsIgnoreCase("y")){
				  roundDone = false;
			      newMatch = true;
				  deck.shuffle();
			   }else if(option.equalsIgnoreCase("n")){
				  isFinished = true;
			      newMatch = true;
			   }else
				  System.out.println("Invalid selection.");
			}
		}
	
	}

}
