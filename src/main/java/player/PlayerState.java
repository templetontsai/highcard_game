package player;

/**
 * @author Tin-Ying Tsai
 *
 */
public interface PlayerState {
	// TODO put more state if needed
	enum GameState {
		Play, Leave, Participant, NonParticipant, Coordinator 
	};
	
	public boolean play();

}
