package unimelb.distributed_algo_game_network;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;

import player.HumanPlayer;
import player.Player;

/**
 * @author Ting-Ying Tsai
 *
 */
public final class GameServer implements Runnable{

	private static GameServer instance = null;
	private int id = -1;
	private boolean isRunning = false;

	protected GameServer() {

	}

	public static GameServer getInstance() {
		if (instance == null) {
			instance = new GameServer();
		}
		return instance;
	}

	private void startServer() {

		ServerSocket serverSocket = null;
		Socket clientSocket = null;
		ObjectInputStream ois = null;
		
		try {
			serverSocket = new ServerSocket(NetworkInterface.PORT);
			System.out.println("Server Start, Waiting....");
			
			while(serverSocket.accept() != null) {
				
				clientSocket = serverSocket.accept();
				System.out.println("Client Connected");

				ois = new ObjectInputStream(clientSocket.getInputStream());
				Object revObj = ois.readObject();
				System.out.println(revObj);
				/*
				if (revObj != null && revObj instanceof Player) {
					System.out.println(((HumanPlayer)revObj).getName());
				}*/
			}
			
			
			ois.close();
			clientSocket.close();
			serverSocket.close();
			
		} catch (IOException ioe) {
			// TODO Adding error handling
			ioe.printStackTrace();
		} catch (ClassNotFoundException classNotFoundException) {
			// TODO Adding error handling
			classNotFoundException.printStackTrace();
			
		} finally {
			System.out.println("Connection Closed");
		}

	}
	
	public void setId(int id) {
		this.id = id;		
	}
	
	public void stopServer(boolean isRunning) {
		this.isRunning = isRunning;
	}

	public void run() {
		startServer();
	}

}
