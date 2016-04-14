package unimelb.distributed_algo_game_network;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;

import player.HumanPlayer;
import player.Player;

/**
 * @author Ting-Ying Tsai
 *
 */
public final class GameClient implements Runnable {
	private static GameClient instance = null;
	private Player mPlayer = null;
	// TODO Game State Client
	private boolean isPlaying = false;

	protected GameClient() {

	}

	public static GameClient getInstance() {
		if (instance == null) {
			instance = new GameClient();
		}
		return instance;
	}

	public void setPlayer(Player mPlayer) {
		if (mPlayer != null) {
			this.mPlayer = mPlayer;
		} else {
			System.out.println("Player can't be null");
			throw new NullPointerException();
		}

	}

	private Socket startClient() {
		Socket clientSocket = null;

		
		try {
			
			clientSocket = new Socket("localhost", NetworkInterface.PORT);

		} catch (IOException ioe) {
			// TODO Adding error handling
			ioe.printStackTrace();
			return null;
		}

		return clientSocket;

	}

	public void run() {

		ObjectOutputStream objectOutputStream = null;
		Socket clientSocket = startClient();
		
		if (clientSocket != null) {
			isPlaying = true;

			try {
				objectOutputStream = new ObjectOutputStream(clientSocket.getOutputStream());
				while (isPlaying) {
					//System.out.println("Client connecting...");

					
					// Object sendObj = mPlayer;
					// System.out.println(((HumanPlayer)sendObj).getName());
					objectOutputStream.writeObject("ss");

				}
				System.out.println("Client connecting...");
				objectOutputStream.close();
				clientSocket.close();
			} catch (IOException ioe) {
				// TODO Adding error handling
				ioe.printStackTrace();
			}

		}
	}
}
