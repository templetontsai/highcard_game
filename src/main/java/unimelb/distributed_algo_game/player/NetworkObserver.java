/*
 * 
 */
package unimelb.distributed_algo_game.player;

// TODO: Auto-generated Javadoc
/**
 * An asynchronous update interface for receiving notifications about Network
 * information as the Network is constructed.
 *
 * @author Ting-Ying Tsai
 */
public interface NetworkObserver {

	/**
	 * This method is called when information about an Network which was
	 * previously requested using an asynchronous interface becomes available.
	 */
	public void update();
}
