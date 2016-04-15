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
		
	    //Create card deck for the game and shuffle
		Deck deck = Deck.getInstance();
		deck.shuffle();
	
		//Initialize players
		Player p1 = new HumanPlayer("Templeton", 1);
		Player p2 = new AIPlayer("AI 1", 2);
		Player p3 = new AIPlayer("AI 2", 3);

		//Math random number generator
		Random random = new Random();
		
		//Create arraylist of players and add all the players
		ArrayList<Player> players = new ArrayList<Player>();
		players.add(p1);
		players.add(p2);
		players.add(p3);
		
		//Variables to track the game
		boolean isFinished = false;
		boolean roundDone = false;
		
		//Initialize scanner to read user input
		Scanner scanner = null;
		while(!isFinished){
			//Create random selection for the AI players
		    int choice = random.nextInt(deck.getDeck().size()-1) + 0;
		    p2.selectFromDeck(deck.getCardFromDeck(choice));
		    choice = random.nextInt(deck.getDeck().size()-1) + 0;
		    p3.selectFromDeck(deck.getCardFromDeck(choice));
		    //Iterates until player has selected a valid card from the deck
			while(!roundDone){
				System.out.println("Please select from the deck between 1-"+deck.getDeck().size());
				scanner = new Scanner(System.in);
				int option = scanner.nextInt()-1;
				//Validate that card is within the deck range
				if(option>=0 && option<deck.getDeck().size()){
					roundDone = true;
					p1.selectFromDeck(deck.getCardFromDeck(option));
				}else{
					System.out.println("Invalid selection.");
				}
			}
			//Compares your selection against the other players to determine the round winner
			System.out.println(Util.compareRank(players)+" \n"+Util.getLeaderBoard(players));
			//Displays leaderboard
			boolean newMatch = false;
			//Check if user wants to start another round
			while(!newMatch){
			   System.out.println("Do you want to continue? [Y/N]");
			   scanner = new Scanner(System.in);
			   String option = scanner.nextLine();
			   if(option.equalsIgnoreCase("y")){
				  roundDone = false;
			      newMatch = true;
			      //Reset the deck with fresh cards and shuffles them
			      deck.resetDeck();
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
