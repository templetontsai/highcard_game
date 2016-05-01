import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.TimerTask;

public class PlayerNode 
{
	public static ServerSocket server;
	
	// pair of id,socket | socket,bufferreader | socket,Printwriter are stored in Hashmaps.
    public static HashMap<String,Socket> nodeIDSocketMap = new HashMap<String,Socket>();
    public static HashMap<Socket,BufferedReader> socketReadersMap = new HashMap<Socket,BufferedReader>();
    public static HashMap<Socket,PrintWriter> socketWritersMap = new HashMap<Socket,PrintWriter>();

	// total players in program
	public static int numOfNodes = 0;
	// id if current player node
	public static int nodeID = 0;
	
	public static void main(String[] args)
	{		
		
		// player will have to pass its alloted id
		if (args.length > 0)
		{
			try
			{
		    	nodeID = Integer.parseInt(args[0]);
		    }
			catch (NumberFormatException e)
			{
				System.err.println("nodeId supplied must be an integer");
				System.exit(1);
		    }
		}
		try 
		{
			FileReaderWriter frw = new FileReaderWriter();
			OptimizedRicartAgrawalaAlgo RA = new OptimizedRicartAgrawalaAlgo(frw);
			
			numOfNodes = frw.readConfig();
			
			System.out.println("Total Players:"+numOfNodes);
			
			//avoiding thread blocking by running as a new thread
			SocketRequestReception RCT = new SocketRequestReception(nodeID,numOfNodes,frw);
			System.out.println("Start Listening.....");
			
			// waiting for other server and listener pair to get started 
			System.out.println("waiting for other nodes to join in");
			Thread.sleep(15000);
			
			SocketToServerConnection SCT = new SocketToServerConnection(nodeID,numOfNodes,frw);
			
			// waiting for socket connection
			Thread.sleep(5000);
			
			// Starting threads for always read listeners
			for (int i=0;i<numOfNodes;i++)
			{
				if (i!=nodeID)
				{
					MessageFormatDecryption mfd = new MessageFormatDecryption(nodeIDSocketMap.get(Integer.toString(i)),frw, RA);
					System.out.println("SocketID"+mfd);
					System.out.println("Started thread at "+nodeID+" for listening "+i);
				}
			}
			
			// starting the process with a start message
			if (nodeID == 0)
			{
				new Thread()
				{
					public void run()
					{
						broadcast("START");
					}
				 }.start();
				 RA.requestCriticalSection();
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
    
    /**
	* Broadcasts a message to all writers in the outputStreams arraylist.
	*/
	public static void broadcast(String message)
	{
		for(int i=0; i<numOfNodes; i++)
		{
			if (i!=nodeID)
			{
				try
				{
					System.out.println("Sending "+message+" to "+i);
					Socket bs = nodeIDSocketMap.get(Integer.toString(i));
					PrintWriter writer = socketWritersMap.get(bs);
		            writer.println(message);
	                writer.flush();
				}
				catch(Exception ex)
				{
					ex.printStackTrace();
				}
			}
		}
	}
	
	public static void closeSockets()
	{
		System.out.println("Halting this node");
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		for(int i=0; i<numOfNodes; i++)
		{
			//if (i!=nodeID)
			{
				try
				{	
					System.out.println("Closing socket with "+Integer.toString(i));
					Socket socket = nodeIDSocketMap.get(Integer.toString(i));
					if (socket != null)
					{
						PrintWriter writer = socketWritersMap.get(socket);
						BufferedReader BR = socketReadersMap.get(socket);
						writer.close();
						BR.close();
						socket.close();
						System.out.println("Closed socket with "+Integer.toString(i));
					}
				}
				catch(Exception ex)
				{
					ex.printStackTrace();
				}
			}
		}
		
		// Threads still not terminating
		// TODO: Figure out alternative
		System.exit(0);
	}
}
