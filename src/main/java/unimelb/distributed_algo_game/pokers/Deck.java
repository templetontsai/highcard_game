/*
 * 
 */
package unimelb.distributed_algo_game.pokers;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;

import unimelb.distributed_algo_game.pokers.Card.CardPattern;
import unimelb.distributed_algo_game.pokers.Card.CardRank;

// TODO: Auto-generated Javadoc
/**
 * The Class Deck.
 *
 * @author Ting-Ying Tsai
 */
public final class Deck implements Serializable {

	/** The instance. */
	// Initialize the variables for the deck class
	private static Deck instance = null;

	/** The Constant DECK_SIZE. */
	private static final int DECK_SIZE = 5;// For testing changing it to 13
											// from 52

	/** The deck. */
	private ArrayList<Card> deck = new ArrayList<Card>(DECK_SIZE);

	/**
	 * Initialize the card deck.
	 */
	protected Deck() {
		init();
	}

	/**
	 * Returns the existing of the card deck if it exists else it initializes a
	 * new deck.
	 *
	 * @return single instance of Deck
	 */
	public static Deck getInstance() {
		if (instance == null) {
			instance = new Deck();
		}
		return instance;
	}

	/**
	 * Creates a new deck and adds all the cards for each rank and
	 * pattern/suite.
	 */
	private void init() {
		/*
		 * for (CardRank cardRank : CardRank.values()) { //TODO comment other
		 * pattern out for the simplicity of the game to demo //deck.add(new
		 * Clubs(cardRank)); //deck.add(new Diamonds(cardRank)); //deck.add(new
		 * Hearts(cardRank)); //deck.add(new Spades(cardRank)); }
		 */
		deck.add(new Clubs(CardRank.Ace));
		deck.add(new Clubs(CardRank.Two));
		deck.add(new Clubs(CardRank.Three));
		deck.add(new Clubs(CardRank.Four));
		deck.add(new Clubs(CardRank.Five));

	}

	/**
	 * This method reorders the cards in the deck in a random order.
	 */
	public void shuffle() {
		Collections.shuffle(deck);
	}

	/**
	 * Returns a card from the deck at the given index.
	 *
	 * @param cardIndex
	 *            the card index
	 * @return the card
	 */
	public Card getCard(int cardIndex) {
		Card card = null;

		if (!deck.isEmpty()) {
			card = deck.get(cardIndex - 1);
			deck.remove(cardIndex - 1);

		} else {
			resetDeck();
			shuffle();
			card = deck.get(cardIndex - 1);
			deck.remove(cardIndex - 1);
		}

		return card;
	}

	/**
	 * Prints out the current cards in the deck.
	 */
	public void showCards() {

		for (Card c : deck) {
			System.out.println(c.getPattern() + "," + c.getCardRank());
		}
	}

	/**
	 * Returns the current deck of cards.
	 *
	 * @return the deck
	 */
	public ArrayList<Card> getDeck() {
		return deck;
	}

	/**
	 * This method clears the deck and re-initializes the deck with new cards.
	 */
	private void resetDeck() {
		deck.clear();
		/*
		 * for (CardRank cardRank : CardRank.values()) { deck.add(new
		 * Clubs(cardRank)); //deck.add(new Hearts(cardRank)); //deck.add(new
		 * Diamonds(cardRank)); //deck.add(new Spades(cardRank)); }
		 */
		deck.add(new Clubs(CardRank.Ace));
		deck.add(new Clubs(CardRank.Two));
		deck.add(new Clubs(CardRank.Three));
		deck.add(new Clubs(CardRank.Four));
		deck.add(new Clubs(CardRank.Five));

	}

}
