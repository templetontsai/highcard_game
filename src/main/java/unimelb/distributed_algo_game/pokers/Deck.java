/*
 * 
 */
package unimelb.distributed_algo_game.pokers;

import java.util.ArrayList;

import unimelb.distributed_algo_game.pokers.Card.CardPattern;
import unimelb.distributed_algo_game.pokers.Card.CardRank;

// TODO: Auto-generated Javadoc
/**
 * The Class Deck.
 *
 * @author Ting-Ying Tsai
 */
public final class Deck {

	/** The instance. */
	// Initialize the variables for the deck class
	private static Deck instance = null;

	/** The Constant DECK_SIZE. */
	private static final int DECK_SIZE = 13;//For testing changing it to 13 from 52

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
		for (CardRank cardRank : CardRank.values()) {
			deck.add(new Clubs(cardRank, CardPattern.Clubs));
			deck.add(new Hearts(cardRank, CardPattern.Hearts));
			deck.add(new Diamonds(cardRank, CardPattern.Diamonds));
			deck.add(new Spades(cardRank, CardPattern.Spades));
		}

	}

	/**
	 * This method reorders the cards in the deck in a random order.
	 */
	public void shuffle() {
		ArrayList<Card> temp = new ArrayList<Card>();
		while (!deck.isEmpty()) {
			int index = (int) (Math.random() * deck.size());
			temp.add(deck.get(index));
			deck.remove(index);
		}
		deck = temp;
	}


	public Card getCard(int cardIndex) {
		
			Card card = deck.get(cardIndex);
			deck.remove(cardIndex);
		
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
	public void resetDeck() {
		deck.clear();
		for (CardRank cardRank : CardRank.values()) {
			deck.add(new Clubs(cardRank, CardPattern.Clubs));
			deck.add(new Hearts(cardRank, CardPattern.Hearts));
			deck.add(new Diamonds(cardRank, CardPattern.Diamonds));
			deck.add(new Spades(cardRank, CardPattern.Spades));
		}
	}

}
