/*
 * 
 */
package unimelb.distributed_algo_game.network.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Map;

import unimelb.distributed_algo_game.player.Player;
import unimelb.distributed_algo_game.player.PlayerScore;

// TODO: Auto-generated Javadoc
/**
 * The Class Util.
 *
 * @author Ting-Ying Tsai This class is responsible for comparing the cards
 *         selected by the users and determines the winner
 */
public class Utils {

	/**
	 * This method returns the winner of the match and updates the winner's
	 * score.
	 *
	 * @param players
	 *            the players
	 * @return the string
	 */
	public static String compareRank(ArrayList<Player> players) {
		// Initializes the variables
		String matchDetails = "node" + players.get(0).getGamePlayerInfo().getNodeID() + " drew "
				+ players.get(0).getSelectedCard().getCardRank() + " " + players.get(0).getSelectedCard().getPattern();
		int winningScore = 0;
		int playerID = 0;
		boolean matchDraw = false;
		StringBuilder sb = new StringBuilder(matchDetails);
		// Iterates through the players and picks the highest score to determine
		// the winner
		for (int i = 0; i < players.size(); i++) {
			if (players.get(i).getSelectedCard().getCardRank().getCode() > winningScore) {
				winningScore = players.get(i).getSelectedCard().getCardRank().getCode();
				playerID = i;
			} else if (players.get(i).getSelectedCard().getCardRank().getCode() == winningScore) {
				matchDraw = true;
			}
			if (i > 0)
				sb.append(", node" + players.get(i).getGamePlayerInfo().getNodeID() + " drew "
						+ players.get(i).getSelectedCard().getCardRank() + " "
						+ players.get(i).getSelectedCard().getPattern());
		}
		String winner = "";
		// Ensures to check if there is a draw and awards no point in that case
		if (!matchDraw) {
			winner = players.get(playerID).getGamePlayerInfo().getNodeID() + " has won the match. " + sb.toString();
			players.get(playerID).updateScore();
		} else {
			winner = "The match was declared a draw. " + sb.toString();
		}
		return winner;
	}

	/**
	 * Compare rank.
	 *
	 * @param players
	 *            the players
	 * @return the int
	 */
	public static synchronized int compareRank(Map<Integer, Player> players) {

		// Initializes the variables
		/**
		 * String matchDetails = "node" +
		 * players.get(0).getGamePlayerInfo().getNodeID() + " drew " +
		 * players.get(0).getSelectedCard().getCardRank() + " " +
		 * players.get(0).getSelectedCard().getPattern();
		 **/
		int winningScore = 0;
		int playerID = 0;
		boolean matchDraw = false;
		StringBuilder sb = new StringBuilder("");
		int i = 0;
		// Iterates through the players and picks the highest score to determine
		// the winner
		for (Map.Entry<Integer, Player> p : players.entrySet()) {
			i = p.getValue().getGamePlayerInfo().getNodeID();

			if (p.getValue().getSelectedCard().getCardRank().getCode() > winningScore) {
				winningScore = p.getValue().getSelectedCard().getCardRank().getCode();
				playerID = i;
			} else if (p.getValue().getSelectedCard().getCardRank().getCode() == winningScore) {
				matchDraw = true;
			}
			if (i > 0)
				sb.append(", node" + players.get(i).getGamePlayerInfo().getNodeID() + " drew "
						+ p.getValue().getSelectedCard().getCardRank() + " "
						+ players.get(i).getSelectedCard().getPattern());
		}
		String winner = "";
		int winner_id = -1;
		// Ensures to check if there is a draw and awards no point in that case
		if (!matchDraw) {
			winner = players.get(playerID).getGamePlayerInfo().getNodeID() + " has won the match. " + sb.toString();
			winner_id = players.get(playerID).getGamePlayerInfo().getNodeID();
			players.get(playerID).updateScore();
		} else {
			winner = "The match was declared a draw. " + sb.toString();
		}
		return winner_id;
	}

	/**
	 * Gets the process timestamp.
	 *
	 * @return the process timestamp
	 */
	public synchronized static long getProcessTimestamp() {
		return new Date().getTime();
	}
	/**
	 * This method returns the leaderboard of the current players.
	 *
	 * @param players
	 *            the players
	 * @return the leader board
	 */
	/*
	 * public static String getLeaderBoard(ArrayList<Player> players) { String
	 * leaderboard = ""; ArrayList<PlayerScore> scoreBoard = new
	 * ArrayList<PlayerScore>(); for (int i = 0; i < players.size(); i++) {
	 * scoreBoard.add(players.get(i).getPlayerScore()); }
	 * Collections.sort(scoreBoard, new Comparator<PlayerScore>() { public int
	 * compare(PlayerScore p1, PlayerScore p2) { return
	 * Integer.compare(p2.getScore(), p1.getScore()); } });
	 * 
	 * StringBuilder sb = new StringBuilder(leaderboard); for (int i = 0; i <
	 * scoreBoard.size(); i++) { int j = i + 1; sb.append(j + ". Player " +
	 * scoreBoard.get(i).getPlayerID() + " - " + scoreBoard.get(i).getScore() +
	 * " \n"); } return sb.toString(); }
	 */
}
