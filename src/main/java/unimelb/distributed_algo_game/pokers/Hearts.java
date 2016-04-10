package unimelb.distributed_algo_game.pokers;


/**
 * @author Ting-Ying Tsai
 *
 */
@SuppressWarnings("serial")
public final class Hearts extends Card{
	
	private CardPattern cardPattern = null;
	private CardRank cardRank = null;
	

	public Hearts(CardRank cardRank, CardPattern cardPattern) {
		super(cardRank, cardPattern);
	}
	


}
