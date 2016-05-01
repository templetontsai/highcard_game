/**
This class first make server socket from the passed nodeID and then listen for connection
from other nodes and for itself.
**/
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

public class SocketRequestReception extends Thread
{
	int nodeID, numberOfNodes;
	FileReaderWriter frw;
	SocketRequestReception(int nodeID, int numberOfNodes, FileReaderWriter frw)
	{
		super();
		start();
		this.nodeID = nodeID;
		this.numberOfNodes = numberOfNodes;
		this.frw = frw;
	}
	public void run()
	{
		try
		{
			// Starting server on specified thread node at supplied port number
			// upto this point frw has already made a map out of config file
			int port = Integer.parseInt(frw.map.get(Integer.toString(nodeID)).get(1));
			ServerSocket server = new ServerSocket(port);
			System.out.println("Server for this nodeID "+nodeID+" is listening on "+port);
			
			// i=0 is representing the first connection
			int i = 0;
			while (numberOfNodes>1)
			{
				//waiting for reuest from other node its a blocking operation
				Socket socket = server.accept();
				System.out.println("Currently this nodeId socket "+nodeID+" accepts "+i + " "+ socket);
				System.out.println("******************************");
				
				PlayerNode.nodeIDSocketMap.put(Integer.toString(i),socket);
				PlayerNode.socketReadersMap.put(socket,new BufferedReader(new InputStreamReader(socket.getInputStream())));
				PlayerNode.socketWritersMap.put(socket,new PrintWriter(socket.getOutputStream()));
				
	            // for ordering to store in array
	            i++;
	            
	            // no of nodes left from which connection yet to come.
	            numberOfNodes--;
			}
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}