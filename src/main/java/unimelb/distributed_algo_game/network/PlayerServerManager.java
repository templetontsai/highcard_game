package unimelb.distributed_algo_game.network;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.json.simple.JSONObject;

import unimelb.distributed_algo_game.network.BodyMessage.MessageType;
import unimelb.distributed_algo_game.network.NetworkInterface.ClientConnectionState;
import unimelb.distributed_algo_game.network.utils.Utils;
import unimelb.distributed_algo_game.player.GamePlayerInfo;
import unimelb.distributed_algo_game.player.Player;
import unimelb.distributed_algo_game.player.SlavePlayer;
import unimelb.distributed_algo_game.pokers.Card;
/**
 * This class is responsible for managing the threads connected to the other servers
 * @author Lupiya
 *
 */
public class PlayerServerManager {

	/** The player server client list. */
	private Map<Integer, PlayerServerThread> playerClientServerList = null;
	/** The m player. */
	private Player mPlayer = null;

	private boolean isPlay = false;

	private boolean isLockRound = false;

	private Map<Integer, Player> playerList = null;
	
	private Map<Integer, Player> nodeList = null;
	
	private Map<Integer, String[]> serverList = null;
	
	private GamePlayerInfo myNeighbor = null;
	
	private boolean isReplied = false;
	
	

	/**
	 * Instantiates a new player client manager.
	 *
	 * @param playerClientNum
	 *            the player client num
	 */
	public PlayerServerManager(int playerClientNum) {
		playerClientServerList = new HashMap<Integer, PlayerServerThread>(playerClientNum);
		playerList = new HashMap<Integer, Player>();
		serverList = new HashMap<Integer, String[]>();
	}

	/**
	 * This sets the player acting as the server of this client server manager
	 */
	public void setPlayer(Player mPlayer) {
		this.mPlayer = mPlayer;
	}

	/**
	 * Adds the client.
	 * 
	 * @param clientThread
	 *            the client thread
	 */
	public synchronized void addClient(int clientID, PlayerServerThread clientThread) {
		playerClientServerList.put(clientID, clientThread);
	}

	public synchronized void addPlayer(GamePlayerInfo gamePlayerInfo) {
		playerList.put(gamePlayerInfo.getNodeID(), new SlavePlayer(gamePlayerInfo));
	}

	/**
	 * Removes the client.
	 *
	 * @param clientThread
	 *            the client thread
	 */
	public synchronized void removeClient(int clientThread) {
		playerClientServerList.remove(clientThread);
		System.out.println( playerClientServerList.size() + "sss");
	}

	public synchronized void removePlayer(int nodeID) {
		playerList.remove(nodeID);
	}


	/**
	 * Notify all clients.
	 */
	public synchronized void notifyAllClients(Object object, ClientConnectionState mConnectionState, MessageType messageType) {
		for (Map.Entry<Integer, PlayerServerThread> t : playerClientServerList.entrySet()) {
			JSONObject mMessage = new JSONObject();
			BodyMessage bodyMessage = new BodyMessage(mPlayer.getGamePlayerInfo(), messageType, object);
			mMessage.put("header", mConnectionState);
			mMessage.put("body", bodyMessage);
			t.getValue().sendMessage(mMessage);
		}
	}

	/**
	 * This method sends a message to a client in the thread pool
	 */
	public synchronized void sendMessageToClient(Object message, int clientID, ClientConnectionState mConnectionState,
			MessageType messageType) {

		if (playerClientServerList.size() > 0) {
			JSONObject mMessage = new JSONObject();
			BodyMessage bodyMessage = new BodyMessage(mPlayer.getGamePlayerInfo().getNodeID(), messageType, message);
			mMessage.put("header", mConnectionState);
			mMessage.put("body", bodyMessage);
			playerClientServerList.get(clientID).sendMessage(mMessage);
		}
	}
	
	/**
	 * Checks the status of all the players
	 */
	public synchronized void checkPlayerStatus() {
		//Trigger the play panel here and to have the fixed size of the player
		if (isLockRound() && playerList.size() >= 2) {
			// Dealer draw a card
			updatePlayerCard(mPlayer.getGamePlayerInfo().getNodeID(), mPlayer.getCard(1));
			notifyAllClients(Utils.compareRank(playerList), ClientConnectionState.CONNECTED, MessageType.BCT);
			for (Map.Entry<Integer, PlayerServerThread> entry : playerClientServerList.entrySet()) {
				entry.getValue().setClientStatus(false);
			}
		}
	}
	
	/**
	 * Sends a card update to the player
	 * @param nodeID
	 * @param c
	 */
	public synchronized void updatePlayerCard(int nodeID, Card c) {
		Player p = playerList.get(nodeID);
		p.selectFromDeck(c);
		System.out.println("node: " + nodeID + ", " + c.getPattern() + ", " + c.getCardRank());
	
	}
	
	/**
	 * Returns the lock round status of all the players
	 * @return
	 */
	public synchronized boolean isLockRound() {
		if(playerClientServerList.size() >= 1) {
			for (Map.Entry<Integer, PlayerServerThread> entry : playerClientServerList.entrySet()) {
				this.isLockRound = entry.getValue().getClientStatus();
			}

		} else {
			
			this.isLockRound =  false;
		}
		
		return this.isLockRound;
		
	}
	
	/**
	 * Sends the current client list to the client's server port
	 * @param mConnectionState
	 * @param messageType
	 */
	public synchronized void sendClientList(ClientConnectionState mConnectionState, MessageType messageType){
		for (Map.Entry<Integer, PlayerServerThread> t : playerClientServerList.entrySet()) {
			JSONObject mMessage = new JSONObject();
			
			BodyMessage bodyMessage = new BodyMessage(mPlayer.getGamePlayerInfo(), messageType, getPlayersSockets());
			mMessage.put("header", mConnectionState);
			mMessage.put("body", bodyMessage);
			t.getValue().sendMessage(mMessage);
		}
	}
	
	/**
	 * Generates list of current clients and their socket details
	 * @return
	 */
	public String getPlayersSockets(){
		StringBuilder sb = new StringBuilder();
		for (Map.Entry<Integer, PlayerServerThread> t : playerClientServerList.entrySet()) {
			GamePlayerInfo playerInfo = t.getValue().getClientGamePlayerInfo();
			sb.append(playerInfo.getNodeID()+":"+playerInfo.getIPAddress()+":"+playerInfo.getPort()+"\n");
		}
		return sb.toString();
	}
	
	/**
	 * Updates the list of current player servers in the game
	 * @param serverArrayList
	 */
	public void updateServerList(ArrayList<String> serverArrayList){
		
		this.serverList.clear();
		
		for(int i = 0; i < serverArrayList.size(); i++){
			String[] details = serverArrayList.get(i).split(":");
			String[] serverInfo = {details[0], details[1], details[2]};
			serverList.put(new Integer(details[0]), serverInfo);
		}
		//System.out.println("New server list size is "+serverList.size());
	}
	
	/**
	 * Returns the next logical member in the ring
	 * @return
	 */
	public GamePlayerInfo getNextNeighbor(){
		
		int clientID = mPlayer.getGamePlayerInfo().getNodeID();
		boolean nextFound = false;
		int i = 0;
		for (Map.Entry<Integer, String[]> t : serverList.entrySet()) {
			//System.out.println(t.getValue()+"-"+t.getKey()+"-"+clientID);
			//TODO fix this
			if(i==0)
				myNeighbor = new GamePlayerInfo(t.getValue(), false);
			
			if(!nextFound && clientID < t.getKey()){
				myNeighbor = new GamePlayerInfo(t.getValue(), false);
				nextFound = true;
			}
			i++;
		}
		return myNeighbor;
	}
	
	/**
	 * Sends a message to the neighbor client server
	 * @param mMessage
	 */
	public void sendMessageToNeighbor(JSONObject mMessage){
		for (Map.Entry<Integer, PlayerServerThread> t : playerClientServerList.entrySet()) {
			if(t.getKey()==myNeighbor.getNodeID()){
			    t.getValue().sendMessage(mMessage);
			}
		}
	}
	

	
	public void startElection(){
		for (Map.Entry<Integer, PlayerServerThread> t : playerClientServerList.entrySet()) {
			if(t.getKey()==myNeighbor.getNodeID()){
			    t.getValue().startElection();
			}
		}
	}
	
	public synchronized boolean isAllCRTReplied() {
		if(playerClientServerList.size() >= 1) {
			for (Map.Entry<Integer, PlayerServerThread> entry : playerClientServerList.entrySet()) {
				this.isReplied = entry.getValue().getReply();
			}
			

		} else {
			
			this.isReplied =  true;
		}
		
		return this.isReplied;
		
	}
	

	


}
