import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Random;
import java.util.logging.Logger;

public class MessageFormatDecryption extends Thread
{
	long messageTS, currentTS;
	Socket socket;
	BufferedReader BR;
	OptimizedRicartAgrawalaAlgo RA;
	FileReaderWriter frw;
	
	MessageFormatDecryption(Socket socket, FileReaderWriter frw, OptimizedRicartAgrawalaAlgo RA)
	{
		super();
		start();
		this.socket = socket;
		this.RA = RA;
		this.frw = frw;
		try {
			BR = PlayerNode.socketReadersMap.get(socket);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void run()
	{
		String message;
		try
		{
			while((message = BR.readLine() ) != null)
			{
				String tokens[] = message.split(",");
				String messageType = tokens[0];
				
				if(messageType.equals("START"))
				{
					RA.requestCriticalSection();
				}
				
				// Completion Message
				if(messageType.equals("COMPLETE"))
				{
					RA.nodeCompletetionCount++;
					RA.totalRequestsSent += Integer.parseInt(tokens[1]);
					if (RA.nodeCompletetionCount == PlayerNode.numOfNodes-1 && 
					RA.nodeZeroCompletetion == true)
					{
						System.out.println("Completion Message recieved. Total Request Sent are:"+RA.totalRequestsSent);
						PlayerNode.broadcast("HALT");
						PlayerNode.closeSockets();
					}
				}
				
				// Halt message recieved so start terminating 
				if(messageType.equals("HALT"))
				{
					PlayerNode.closeSockets();
				}
				
				if(messageType.equals("REPLY"))
				{
					
					RA.incrementCount();

					// This is our optimization step (Roucairol-Carvalho)
					RA.participants.remove(tokens[1]);
					
					System.out.println("No of REPLY MSG:"+RA.replyCount+":FROM"+tokens[1]);
					RA.checkCS();
				}
				
				if(messageType.equals("REQUEST"))
				{
						
					System.out.println("Request for SERVER Timestamp :"+RA.requestTS);
					messageTS = Long.parseLong(tokens[1]);
					
					System.out.println("Request for Message Timestamp:"+tokens[2]+":"+messageTS);
					System.out.println("******************************");
					
					// Comparisons are made based on paper.
					if(RA.criticalSection == false && 
							((RA.requestCS == false)
							|| (RA.requestCS == true && RA.requestTS > messageTS)
							|| (RA.requestCS == true && RA.requestTS == messageTS
							 && PlayerNode.nodeID > Integer.parseInt(tokens[2]))))
            		{
						if (RA.requestCS == true && RA.criticalSection == false 
			            		&& RA.criticalSectionCount != 0 && !(RA.copyOfParticipants.contains(tokens[2])))
			            {
			            	++RA.participantsCount;
			            	PrintWriter writer2 = PlayerNode.socketWritersMap.get(socket);
			            	long requestTS = ProcessTimeStamp.getProcessTimestamp();
    			            writer2.println("REQUEST,"+requestTS+","+PlayerNode.nodeID);
    			            writer2.flush();
    			            System.out.println("Sending delayed request to"+tokens[2]+":"+requestTS);
			            }
						
						System.out.println("REPLY SENT TO:"+tokens[2]);
						// Reply
						PrintWriter writer = PlayerNode.socketWritersMap.get(socket);
						writer.println("REPLY"+","+PlayerNode.nodeID);
			            writer.flush();
			            
			            // Roucairol-Carvalho optimization
			            RA.participants.add(tokens[2]);
            		}
					else
					{
						// defer REPLY
						System.out.println("Deferred reply to "+tokens[2]);
						RA.deferred.add(tokens[2]);
					}
				}
			}
			
		}
		catch (Exception e)
		{
			// TODO Auto-generated catch block
			// e.printStackTrace();
		}
	}
}