/**
Send connection to different nodeID's server if nodeId is snaller then the nodeId whom u sending request.
**/
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

public class SocketToServerConnection extends Thread
{
	int nodeID, numberOfNodes;
	FileReaderWriter frw;
	SocketToServerConnection(int nodeID, int numberOfNodes, FileReaderWriter frw)
	{
		super();
		start();
		this.nodeID = nodeID;
		this.numberOfNodes = numberOfNodes;
		this.frw = frw;
	}
	public void run()
	{
		Socket socket;
		for(int i=0;i<numberOfNodes;i++)
		{
			// Send connection to all nodes with nodeID > current node
			if (nodeID < i)
			{
				String hostName = frw.map.get(Integer.toString(i)).get(0);
				int port = Integer.parseInt(frw.map.get(Integer.toString(i)).get(1));
				try
				{
					System.out.println("We are connecting "+hostName+" with given server with port number:"+port);
					socket = new Socket(hostName,port);
					System.out.println("Wow I am happy connection is established");
					
					System.out.println("Currently this nodeId socket "+nodeID+" is sending to "+i + " "+ socket);
					System.out.println("******************************");
					
					PlayerNode.nodeIDSocketMap.put(Integer.toString(i),socket);
					PlayerNode.socketReadersMap.put(socket,new BufferedReader(new InputStreamReader(socket.getInputStream())));
					PlayerNode.socketWritersMap.put(socket,new PrintWriter(socket.getOutputStream()));
				}
				catch (IOException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
		}
	}
}