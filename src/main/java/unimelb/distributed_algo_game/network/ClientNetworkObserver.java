/*
 * 
 */
package unimelb.distributed_algo_game.network;

// TODO: Auto-generated Javadoc
/**
 * An asynchronous update interface for receiving notifications about
 * ClientNetwork information as the ClientNetwork is constructed.
 */
public interface ClientNetworkObserver {

	/**
	 * This method is called when information about an ClientNetwork which was
	 * previously requested using an asynchronous interface becomes available.
	 */
	public void update();
}
