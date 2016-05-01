import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.Date;
import java.util.Random;


public class OptimizedRicartAgrawalaAlgo {
	
	FileReaderWriter frw;
	OptimizedRicartAgrawalaAlgo(FileReaderWriter frw)
	{
		this.frw = frw;
	}
	
	public long requestTimeStamp;
	public int noOfCS = 0;
	public boolean requestCriticalSection = false;
	public boolean criticalSection = false;
	public int completetionMessageCount = 0;
	public boolean nodeZeroCompletetionMessage = false;
	public int noOfReply = 0;
	public long noOfSentRequests= 0;
	public long timeTaken = 0;
	public long messagesExchangeUpperBound = 0;
	public long messagesExchangeLowerBound = 0;
	
	public ArrayList<String> deferredNodesList = new ArrayList<String>();
	public ArrayList<String> mainMemberList = new ArrayList<String>();
	public ArrayList<String> mainMemberListCopy = new ArrayList<String>();
	public int noOfMembers = 0;
	
	// TODO Algorithm Class
	public void requestCriticalSection()
	{
		
		if (noOfCS < 3)
		{
			// delay before entering critical section
			Random rn = new Random();
			int time = 10 + rn.nextInt(90);
			try {
				Thread.sleep(time);
				System.out.println("delay of "+time);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			System.out.println(PlayerNode.nodeID+" ready to enter CS");
			requestCriticalSection = true;
			
            java.util.Date date= new java.util.Date();
            requestTimeStamp = ProcessTimeStamp.getProcessTimestamp();
            
			// send request to all the nodes based on Roucairol-Carvalho optimization
            if (noOfCS == 0)
            {
            	messagesExchangeUpperBound = 2*(PlayerNode.numOfNodes-1);
            	messagesExchangeLowerBound = 2*(PlayerNode.numOfNodes-1);
            	
    			for(int i=0; i<PlayerNode.numOfNodes; i++)
    			{
    				if (i!=PlayerNode.nodeID)
    				{
    					try
    					{
    						Socket bs = PlayerNode.nodeIDSocketMap.get(Integer.toString(i));
    						PrintWriter writer = PlayerNode.socketWritersMap.get(bs);
    			            writer.println("REQUEST,"+requestTimeStamp+","+PlayerNode.nodeID);
    			            writer.flush();
    			            System.out.println("Sending request to others at:"+requestTimeStamp);
    					}
    					catch(Exception ex)
    					{
    						ex.printStackTrace();
    					}
    				}
    			}
            }
            else
            {
            	
            	mainMemberListCopy.clear();
            	mainMemberListCopy.addAll(mainMemberList);
            	noOfMembers = mainMemberListCopy.size();
            	
            	// No members in list so continue with CS
            	if (mainMemberListCopy.isEmpty())
            	{
            		messagesExchangeLowerBound = 0;
            		
            		new Thread()
    				{
            		public void run(){
            		criticalSection = true;
            		long currentTS1 = ProcessTimeStamp.getProcessTimestamp();
		            java.util.Date date1= new java.util.Date();
		            long currentTS = ProcessTimeStamp.getProcessTimestamp();
					System.out.println("CRITICAL SECTION:"+ noOfCS +":"
		            +currentTS);
					
					timeTaken = currentTS - requestTimeStamp;
					
					// amount of time spent in CS =20sec
					try {
						Thread.sleep(20);
					} catch (InterruptedException e1) {
						e1.printStackTrace();
					}
					
					
					// reset node values
					criticalSection = false;
					requestCriticalSection = false;
					noOfReply = 0;
					noOfCS++;
					long currentTS2 = ProcessTimeStamp.getProcessTimestamp();
					sendDeferredReplies();
					
					
					requestCriticalSection();
    				}
    				}.start();
					
				}
            	else
            	{
            		
            		for(String item: mainMemberListCopy)
                	{
            			String Null = null;
        				if (PlayerNode.nodeID != Integer.parseInt(item))
        				{
        					try
        					{
        						Socket bs = PlayerNode.nodeIDSocketMap.get(item);
        						PrintWriter writer = PlayerNode.socketWritersMap.get(bs);
        			            writer.println("REQUEST,"+requestTimeStamp+","+PlayerNode.nodeID);
        			            writer.flush();
        			            System.out.println("Sending request to"+item+":"+requestTimeStamp);
        					}
        					catch(Exception ex)
        					{
        						ex.printStackTrace();
        					}
        				}
                	}
            	}
            }
		}
		else
		{
			System.out.println("Work Done. Send Completion Meassage");
			

			if (PlayerNode.nodeID !=0)
			{
				Socket bs = PlayerNode.nodeIDSocketMap.get("0");
				PrintWriter writer = PlayerNode.socketWritersMap.get(bs);
				writer.println("COMPLETE"+","+noOfSentRequests);
	            writer.flush();
			}
			else
			{
				nodeZeroCompletetionMessage = true;
				if (completetionMessageCount == PlayerNode.numOfNodes-1)
				{
					System.out.println("Final Completion Message recieved. Game Over:"+noOfSentRequests);
					PlayerNode.broadcast("HALT");
					PlayerNode.closeSockets();
				}
			}
		}
	}
	

	public void sendDeferredReplies()
	{
		System.out.println("Started sending deferred reply messages");
		// TODO
		for(int i=0; i<deferredNodesList.size(); i++)
		{
			String deferredNode = deferredNodesList.get(i);
			System.out.println("sending deferred reply messages to player node:"+deferredNode);
			try
			{
				Socket bs = PlayerNode.nodeIDSocketMap.get(deferredNode);
				PrintWriter writer = PlayerNode.socketWritersMap.get(bs);
				writer.println("REPLY"+","+PlayerNode.nodeID);
	            writer.flush();
			}
			catch(Exception ex)
			{
				ex.printStackTrace();
			}
			mainMemberList.add(deferredNode);
		}
		deferredNodesList.clear();
	}
	
	public synchronized void checkCS()
	{
			if ((noOfCS == 0 && 
					noOfReply == PlayerNode.numOfNodes-1) || 
					noOfReply == noOfMembers)
			{
				criticalSection = true;
				noOfSentRequests += (2*noOfReply);
				if (2*noOfReply < messagesExchangeLowerBound)
				{
					messagesExchangeLowerBound = 2*noOfReply;
				}
				
				long currentTS1 = ProcessTimeStamp.getProcessTimestamp();

				System.out.println("We are in "+ noOfCS +" th critical section :"
	            +currentTS1);
				
				
				// Delay of 20 milliseconds
				try {
					Thread.sleep(20);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				
				// reset node parameters
				criticalSection = false;
				requestCriticalSection = false;
				noOfReply = 0;
				noOfCS++;
				
				long currentTS2 = ProcessTimeStamp.getProcessTimestamp();
				sendDeferredReplies();
				
				
				requestCriticalSection();
			}
	}
	
	public synchronized void incrementCount() {
		noOfReply++;
    }
}
