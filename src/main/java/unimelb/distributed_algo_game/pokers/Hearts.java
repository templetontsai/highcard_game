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
	 */
	public Hearts(CardRank cardRank) {
		super(cardRank, CardPattern.Hearts);
	}

}
