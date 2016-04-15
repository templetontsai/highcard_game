package player;

import java.io.Serializable;
import java.util.ArrayList;

import player.PlayerState.GameState;
import unimelb.distributed_algo_game.pokers.Card;

/**
 * @author Ting-Ying Tsai
 *
 */
public abstract class Player implements Serializable, Runnable {

	private ArrayList<Card> cards = new ArrayList<Card>(52);
	private Card selectedCard = null;
	protected PlayerState playerState = null;
	private String name = null;
	private int id = -1;

	public Player(String name, int id, PlayerState playerState) {
		this.name = name;
		this.id = id;
	}

	public String getName() {
		return this.name;
	}

	public int getID() {
		return this.id;
	}
	
	public Card getSelectedCard(){
		return selectedCard;
	}

	public void setCards(ArrayList<Card> cards) {
		if (cards != null)
			this.cards = cards;
		else
			throw new NullPointerException();
	}
	
	public void selectFromDeck(Card card){
		selectedCard = card;
	}
	
	public void showCard(int option){
		selectedCard = cards.get(option);
		System.out.println("You selected "+cards.get(option).getCardRank() + "," + cards.get(option).getPattern());
	}

	public ArrayList<Card> showHand() {
		if (this.cards != null) {
			for (Card card : cards) {
				System.out.println(card.getCardRank().getCode() + "," + card.getPattern());
			}
			return cards;
		} else
			throw new NullPointerException();
	}
	
	/**
	 * This is the method to change the state for a player. 
	 * This might be refactor out later to actual player class.
	 * @param gameState: State.Play, State.Leave
	 * 
	 */
	public void setPlayStatus(GameState gameState) {
		switch (gameState) {
		case Play:
			playerState = new PlayState();
			break;
		case Leave:
			playerState = new StopState();
			break;

		}
	}
	
}
