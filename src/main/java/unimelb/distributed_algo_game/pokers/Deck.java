package unimelb.distributed_algo_game.pokers;

import java.util.ArrayList;

import unimelb.distributed_algo_game.pokers.Card.CardPattern;
import unimelb.distributed_algo_game.pokers.Card.CardRank;

/**
 * @author Ting-Ying Tsai
 *
 */
public final class Deck {

	private static Deck instance = null;
	private static final int DECK_SIZE = 52;
	private ArrayList<Card> deck = new ArrayList<Card>(DECK_SIZE);
	

	protected Deck() {
		init();
	}

	public static Deck getInstance() {
		if (instance == null) {
			instance = new Deck();
		}
		return instance;
	}

	private void init() {
		for (CardRank cardRank : CardRank.values()) {
			deck.add(new Clubs(cardRank, CardPattern.Clubs));
			deck.add(new Hearts(cardRank,CardPattern.Hearts));
			deck.add(new Diamonds(cardRank, CardPattern.Diamonds));
			deck.add(new Spades(cardRank, CardPattern.Spades));
		}

	}

	public void shuffle() {
		ArrayList<Card> temp = new ArrayList<Card>();
		while (!deck.isEmpty()) {
			int index = (int) (Math.random() * deck.size());
			temp.add(deck.get(index));
			deck.remove(index);
		}
		deck = temp;
	}
	
	public ArrayList<Card> getCards(int numCards) {
		ArrayList<Card> cards = new ArrayList<Card>(numCards);
		while(numCards != 0) {
			cards.add(deck.get(numCards));
			deck.remove(numCards);
			numCards--;
		}
		return cards;
	}
	
	public void showCards() {
		
		for(Card c: deck) {
			System.out.println(c.getPattern() + "," + c.getCardRank());
		}
	}
	
	
}
