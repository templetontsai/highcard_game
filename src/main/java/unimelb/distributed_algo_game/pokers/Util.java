package unimelb.distributed_algo_game.pokers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import player.Player;
import player.PlayerScore;

/**
 * @author Ting-Ying Tsai
 * This class is responsible for comparing the cards selected by the users and determines the winner
 */
public class Util {

	/**
	 * This method returns the winner of the match and updates the winner's score
	 */
	public static String compareRank(ArrayList<Player> players) {
		    //Initializes the variables
		    String matchDetails = players.get(0).getName()+" drew "+players.get(0).getSelectedCard().getCardRank()+" "+players.get(0).getSelectedCard().getPattern();
			int winningScore = 0;
			int playerID = 0;
			boolean matchDraw = false;
			StringBuilder sb = new StringBuilder(matchDetails);
			//Iterates through the players and picks the highest score to determine the winner
			for(int i=0; i <players.size(); i++){
				if(players.get(i).getSelectedCard().getCardRank().getCode()>winningScore){
					winningScore = players.get(i).getSelectedCard().getCardRank().getCode();
					playerID = i;
				}else if(players.get(i).getSelectedCard().getCardRank().getCode()==winningScore){
					matchDraw = true;
				}
				if(i>0)
					sb.append(", "+players.get(i).getName()+" drew "+players.get(i).getSelectedCard().getCardRank()+" "+players.get(i).getSelectedCard().getPattern());
			}
			String winner = "";
			//Ensures to check if there is a draw and awards no point in that case
			if(!matchDraw){
			   winner = players.get(playerID).getName()+" has won the match. "+sb.toString();
			   players.get(playerID).updateScore();
	        }else{
			   winner = "The match was declared a draw. "+sb.toString();	   
	        }
			return winner;
	}
	
	/**
	 * This method returns the leaderboard of the current players
	 */
	public static String getLeaderBoard(ArrayList<Player> players){
		String leaderboard = "";
		ArrayList<PlayerScore> scoreBoard = new ArrayList<PlayerScore>();
		for(int i = 0; i < players.size(); i++){
			scoreBoard.add(players.get(i).getPlayerScore());
		}
		Collections.sort(scoreBoard, new Comparator<PlayerScore>() {
	        public int compare(PlayerScore p1, PlayerScore p2) {
	        	return Integer.compare(p2.getScore(), p1.getScore());
	        }
	    } );
		
		StringBuilder sb = new StringBuilder(leaderboard);
		for(int i = 0; i <scoreBoard.size(); i++){
		    int j = i +1;
			sb.append(j+". Player "+scoreBoard.get(i).getPlayerID()+" - "+scoreBoard.get(i).getScore()+" \n");	
		}
		return sb.toString();
	}
}
