/*
 * 
 */
package unimelb.distributed_algo_game.pokers;

import java.io.Serializable;

import unimelb.distributed_algo_game.pokers.Card.CardRank;

// TODO: Auto-generated Javadoc
/**
 * The Class Card.
 *
 * @author Ting-Ying Tsai
 */

public abstract class Card implements Serializable {

	/** The card rank. */
	private CardRank cardRank = null;

	/** The card pattern. */
	private CardPattern cardPattern = null;

	/**
	 * Instantiates a new card.
	 *
	 * @param cardRank
	 *            the card rank
	 * @param cardPattern
	 *            the card pattern
	 */
	public Card(CardRank cardRank, CardPattern cardPattern) {
		this.cardRank = cardRank;
		this.cardPattern = cardPattern;

	}

	/**
	 * The Enum CardPattern.
	 */
	public enum CardPattern {

		/** The Hearts. */
		Hearts(0),
		/** The Diamonds. */
		Diamonds(1),
		/** The Clubs. */
		Clubs(2),
		/** The Spades. */
		Spades(3);

		/** The code. */
		private int code;

		/**
		 * Instantiates a new card pattern.
		 *
		 * @param c
		 *            the c
		 */
		private CardPattern(int c) {
			this.code = c;
		}

		/**
		 * Gets the code.
		 *
		 * @return the code
		 */
		public int getCode() {
			return this.code;
		}

		/**
		 * Sets the code.
		 *
		 * @param code
		 *            the new code
		 */
		public void setCode(int code) {
			this.code = code;
		}
	}

	/**
	 * The Enum CardRank.
	 */
	public enum CardRank {

		/** The Two. */
		Two(2),
		/** The Three. */
		Three(3),
		/** The Four. */
		Four(4),
		/** The Five. */
		Five(5),
		/** The Six. */
		Six(6),
		/** The Seven. */
		Seven(7),
		/** The Eight. */
		Eight(8),
		/** The Nine. */
		Nine(9),
		/** The Ten. */
		Ten(10),
		/** The Jack. */
		Jack(11),
		/** The Queen. */
		Queen(12),
		/** The King. */
		King(13),
		/** The Ace. */
		Ace(1);

		/** The code. */
		private int code;

		/**
		 * Instantiates a new card rank.
		 *
		 * @param c
		 *            the c
		 */
		private CardRank(int c) {
			this.code = c;
		}

		/**
		 * Gets the code.
		 *
		 * @return the code
		 */
		public int getCode() {
			return this.code;
		}

		/**
		 * Sets the code.
		 *
		 * @param code
		 *            the new code
		 */
		public void setCode(int code) {
			this.code = code;
		}

	}

	/**
	 * Show card.
	 */
	public void showCard() {

		System.out.println(this.cardPattern + "," + this.cardRank);

	}

	/**
	 * Gets the pattern.
	 *
	 * @return the pattern
	 */
	public CardPattern getPattern() {
		return cardPattern;
	}

	/**
	 * Gets the card rank.
	 *
	 * @return the card rank
	 */
	public CardRank getCardRank() {

		return this.cardRank;
	}

	/**
	 * Gets the hand rank.
	 *
	 * @return the hand rank
	 */
	public int gethandRank() {
		return cardRank.getCode() + cardPattern.getCode();
	}

}
