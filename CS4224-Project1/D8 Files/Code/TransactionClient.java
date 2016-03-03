import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Metadata;
import com.datastax.driver.core.Session;


public class TransactionClient 
{
private static Cluster cluster;
private static Session session;
private static String keyspace;
		
public static int totalTransactions=0,Threadcount=0;
public static double sumTime=0.0,startTime = 0.0;
 
//clientCount will handle the value of Z i.e. Client
static int clientCount=0;
    
	//Connect to the Cluster
    public void connect(String node)
	{
        cluster = Cluster.builder().addContactPoint(node).build();
        Metadata metadata = cluster.getMetadata();
        System.out.printf("Connected to Cluster Name :  %s\n",metadata.getClusterName());
        session = cluster.connect();
        System.out.println("Connected Session :  "+session);
        System.out.println("Connected Cluster :  "+cluster);              
	    System.out.println("************************************************************\n\n");

    }

    //close the cluster	
    public static void close()
	{
        cluster.close();
    }

    public static void main(String[] args) throws Exception 
	{
     //the first parameter is the value of Z i.e clientCount 
    clientCount = Integer.parseInt(args[0]);  
    
	if (clientCount < 1 || clientCount > 100)
		{
            System.err.println("Incorrect value of Clients. Please choose a value between 1 to 100.");
            System.exit(-1);
        }

        TransactionClient client = new TransactionClient();
        client.connect("compg42");
        
        //Declaring the keyspace		
        keyspace = "TransTeam8";            
 
        startTime = System.currentTimeMillis();
		for (int k = 0; k<(clientCount); k++)
                {
				String name = "Thread-"+k;
                TransactionThread Transth = new TransactionThread(name, k, session, keyspace);
                        Transth.start();
                }

        }
}

