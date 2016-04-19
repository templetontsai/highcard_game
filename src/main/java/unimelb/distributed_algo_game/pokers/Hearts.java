/*
 * 
 */
package unimelb.distributed_algo_game.pokers;

// TODO: Auto-generated Javadoc
/**
 * The Class Hearts.
 *
 * @author Ting-Ying Tsai
 */
@SuppressWarnings("serial")
public final class Hearts extends Card {

	/** The card pattern. */
	private CardPattern cardPattern = null;

	/** The card rank. */
	private CardRank cardRank = null;

	/**
	 * Instantiates a new hearts.
	 *
	 * @param cardRank
	 *            the card rank
	 * @param cardPattern
	 *            the card pattern
	 */
	public Hearts(CardRank cardRank, CardPattern cardPattern) {
		super(cardRank, cardPattern);
	}

}
