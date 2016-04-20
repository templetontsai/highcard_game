/*
 * 
 */
package unimelb.distributed_algo_game.player;

import java.io.Serializable;
import java.util.ArrayList;

import unimelb.distributed_algo_game.pokers.Card;
import unimelb.distributed_algo_game.pokers.Deck;
import unimelb.distributed_algo_game.state.GameState;


// TODO: Auto-generated Javadoc
/**
 * The Class Player.
 *
 * @author Ting-Ying Tsai
 */
/**
 * @author templeton
 *
 */
public abstract class Player implements Serializable, Runnable, NetworkObserver {

	/** The cards. */
	// Initialize the variables to be used in the game
	private ArrayList<Card> cards = new ArrayList<Card>(52);

	/** The selected card. */
	private Card selectedCard = null;


	/** The player score. */
	protected PlayerScore playerScore = null;

	/** The name. */
	private String name = null;

	/** The id. */
	private int id = -1;
	
	private boolean isDealer = false;
	
	private GameState gameState = null;
	
	private Deck mDeck = null;



	/**
	 * Method to initialize the player name, id and state.
	 *
	 * @param name
	 *            the name
	 * @param id
	 *            the id
	 * @param playerState
	 *            the player state
	 * @param playerScore
	 *            the player score
	 */
	public Player(String name, int id, GameState gameState, PlayerScore playerScore) {
		this.name = name;
		this.id = id;
		this.playerScore = playerScore;
		this.gameState = gameState;
	}

	/**
	 * Returns the player name.
	 *
	 * @return the name
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * Returns the player ID.
	 *
	 * @return the id
	 */
	public int getID() {
		return this.id;
	}

	/**
	 * Returns the card user selected from the deck.
	 *
	 * @return the selected card
	 */
	public Card getSelectedCard() {
		return selectedCard;
	}

	/**
	 * This method returns a player score object.
	 *
	 * @return the player score
	 */
	public PlayerScore getPlayerScore() {
		return playerScore;
	}

	/**
	 * This method updates the player score.
	 */
	public void updateScore() {
		playerScore.updateScore();
	}

	/**
	 * Returns the cards randomly selected by the player.
	 *
	 * @param cards
	 *            the new cards
	 */
	public void setCards(ArrayList<Card> cards) {
		if (cards != null)
			this.cards = cards;
		else
			throw new NullPointerException();
	}

	/**
	 * Sets the card the user selected from the deck.
	 *
	 * @param card
	 *            the card
	 */
	public void selectFromDeck(Card card) {
		selectedCard = card;
	}

	/**
	 * Prints out the card selected by the user.
	 *
	 * @param option
	 *            the option
	 */
	public void showCard(int option) {
		System.out.println("You selected " + selectedCard.getCardRank() + "," + selectedCard.getPattern());
	}

	/**
	 * Prints out the cards randomly selected for the user.
	 *
	 * @return the array list
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

	public void setDealer(boolean isDealer) {
		this.isDealer = isDealer;
		// Create card deck for the game and shuffle
		mDeck = Deck.getInstance();
		mDeck.shuffle();
	}
	
	public boolean isDealer() {
		return this.isDealer;
	}
	
	public Card getCard(int cardIndex) {
		Card card = null;
		if(this.isDealer)
			card = mDeck.getCard(cardIndex);
		else {
			System.out.println("Only dealer can be in charge of giving cards");
			throw new NullPointerException();
		}
		
		return card;
	}
	
	public GameState getGameState() {
		return gameState;
	}

	public void setGameState(GameState gameState) {
		this.gameState = gameState;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see unimelb.distributed_algo_game.player.NetworkObserver#update()
	 */
	public abstract void update();

}
