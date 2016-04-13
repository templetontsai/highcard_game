package unimelb.distributed_algo_game.pokers;

import java.util.ArrayList;

import player.Player;

/**
 * @author Ting-Ying Tsai
 *
 */
public class Util {

	public static String highCard(ArrayList<Player> players) {
		    String matchDetails = players.get(0).getName()+" drew "+players.get(0).getSelectedCard().getCardRank()+" "+players.get(0).getSelectedCard().getPattern();
			int winningScore = 0;
			int playerID = 0;
			boolean matchDraw = false;
			StringBuilder sb = new StringBuilder(matchDetails);
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
			if(!matchDraw)
			   winner = players.get(playerID).getName()+" has won the match. "+sb.toString();
			else
			   winner = "The match was declared a draw. "+sb.toString();
			return winner;
	}
}
