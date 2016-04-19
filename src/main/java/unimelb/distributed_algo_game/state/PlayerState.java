/*
 * 
 */
package unimelb.distributed_algo_game.state;

// TODO: Auto-generated Javadoc
/**
 * The Interface PlayerState.
 *
 * @author Tin-Ying Tsai
 */
public interface PlayerState {

	/**
	 * The Enum GameState.
	 */
	// TODO put more state if needed
	enum GameState {

		/** The Play. */
		Play,
		/** The Leave. */
		Leave,
		/** The Participant. */
		Participant,
		/** The Non participant. */
		NonParticipant,
		/** The Coordinator. */
		Coordinator
	};

	/**
	 * Play.
	 *
	 * @return true, if successful
	 */
	public boolean play();

}
