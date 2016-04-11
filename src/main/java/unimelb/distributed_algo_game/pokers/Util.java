package unimelb.distributed_algo_game.pokers;

import java.util.ArrayList;

import player.Player;

/**
 * @author Ting-Ying Tsai
 *
 */
public class Util {

	public static Player highCard(ArrayList<Player> players) {
		Player winner;
		if(!players.isEmpty())
			if(players.get(0).getSelectedCard().getCardRank().compareTo(players.get(1).getSelectedCard().getCardRank())>1)
				winner = players.get(0);
			else if(players.get(0).getSelectedCard().getCardRank().compareTo(players.get(1).getSelectedCard().getCardRank())<1)
				winner = players.get(1);
		else
			System.out.println("No player");
			return null;
	}
}
