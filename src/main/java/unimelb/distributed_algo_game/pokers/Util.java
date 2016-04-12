package unimelb.distributed_algo_game.pokers;

import java.util.ArrayList;

import player.Player;

/**
 * @author Ting-Ying Tsai
 *
 */
public class Util {

	public static String highCard(ArrayList<Player> players) {
		String winner = "";
		String matchDetails = players.get(0).getSelectedCard().getCardRank()+" "+players.get(0).getSelectedCard().getPattern()+
				" and the other player drew "+players.get(1).getSelectedCard().getCardRank()+" "+players.get(1).getSelectedCard().getPattern();
		if(!players.isEmpty())
			if(players.get(0).getSelectedCard().getCardRank().getCode()>players.get(1).getSelectedCard().getCardRank().getCode())
				winner = players.get(0).getName()+" has won the match. You drew "+matchDetails;
			else if(players.get(0).getSelectedCard().getCardRank().getCode()<players.get(1).getSelectedCard().getCardRank().getCode())
				winner = players.get(1).getName()+" has won the match. You drew "+matchDetails;
			else
				winner = "The match was a draw. You drew "+matchDetails;
			return winner;
	}
}
