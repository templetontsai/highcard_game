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

	//Initialize the variables to be used in the game
	private ArrayList<Card> cards = new ArrayList<Card>(52);
	private Card selectedCard = null;
	protected PlayerState playerState = null;
	protected PlayerScore playerScore = null;
	private String name = null;
	private int id = -1;

	/**
	 * Method to initialize the player name, id and state
	 */
	public Player(String name, int id, PlayerState playerState, PlayerScore playerScore) {
		this.name = name;
		this.id = id;
		this.playerScore = playerScore;
	}

	/**
	 * Returns the player name
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * Returns the player ID
	 */
	public int getID() {
		return this.id;
	}
	
	/**
	 * Returns the card user selected from the deck
	 */
	public Card getSelectedCard(){
		return selectedCard;
	}
	
	/**
	 * This method returns a player score object
	 */
	public PlayerScore getPlayerScore(){
		return playerScore;
	}
	
	/**
	 * This method updates the player score
	 */
	public void updateScore(){
		playerScore.updateScore();
	}

	/**
	 * Returns the cards randomly selected by the player
	 */
	public void setCards(ArrayList<Card> cards) {
		if (cards != null)
			this.cards = cards;
		else
			throw new NullPointerException();
	}
	
	/**
	 * Sets the card the user selected from the deck
	 */
	public void selectFromDeck(Card card){
		selectedCard = card;
	}
	
	/**
	 * Prints out the card selected by the user
	 */
	public void showCard(int option){
		System.out.println("You selected "+selectedCard.getCardRank() + "," + selectedCard.getPattern());
	}

	/**
	 * Prints out the cards randomly selected for the user
	 */
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
