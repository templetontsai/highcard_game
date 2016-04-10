package unimelb.distributed_algo_game.pokers;

import java.io.Serializable;

import unimelb.distributed_algo_game.pokers.Card.CardRank;

/**
 * @author Ting-Ying Tsai
 *
 */

public abstract class Card implements Serializable {

	private CardRank cardRank = null;
	private CardPattern cardPattern = null;
	
	public Card(CardRank cardRank, CardPattern cardPattern) {
		this.cardRank = cardRank;
		this.cardPattern = cardPattern;
	}

	public enum CardPattern {
		Hearts(0), Diamonds(1), Clubs(2), Spades(3);
		
		private int code;

		private CardPattern(int c) {
			code = c;
		}

		public int getCode() {
			return code;
		}
	}

	public enum CardRank {
		Two(2), Three(3), Four(4), Five(5), Six(6), Seven(7), Eight(8), Night(9), Ten(10), Jack(11), Queen(12), King(
				13), Ace(1);
		private int code;

		private CardRank(int c) {
			code = c;
		}

		public int getCode() {
			return code;
		}

	}
	
	
	public void showCard() {
		System.out.println(this.cardPattern + "," + this.cardRank);
	}

	public CardPattern getPattern() {
		return cardPattern;
	}

	public CardRank getCardRank() {
		return cardRank;
	}


}
