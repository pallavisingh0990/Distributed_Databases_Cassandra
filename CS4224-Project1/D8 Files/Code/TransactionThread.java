import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.datastax.driver.core.Session;
import com.datastax.driver.core.exceptions.InvalidQueryException;

public class TransactionThread extends Thread {
        private Session session;
        private String keyspace;
		private Thread t;
        private String threadName;
        private int threadNum;
        private double startTime,endTime;
       
        TransactionThread(String name, int num, Session session, String keyspace) {
                threadName = name;
                threadNum = num;
                this.session = session;
                this.keyspace = keyspace;
                this.startTime = System.currentTimeMillis();
        }

     //read the file and based on first parameter (i.e. N,P,D,O,S,I) send the file to transaction proccessing
        private int readTransactionFile() {
                int count = 0;
                BufferedReader br = null;

                try {
                        String filename = String.valueOf(threadNum) + ".txt";
                        wholeSaleDataTransactions transaction = new wholeSaleDataTransactions(session,keyspace);
                        br = new BufferedReader(new FileReader(filename));

                        String strCurrentLine;
                        List<String> fileList = new ArrayList<String>();

                      
                        while ((strCurrentLine = br.readLine()) != null) {
                                fileList.add(strCurrentLine);
                        }
                               for (int i = 0; i < fileList.size(); i++) {
                                String[] words = (fileList.get(i)).split(",");
                                
								//Transaction 2.1 - New Order Transaction  
                                if ((words[0]).equals("N"))
									{
                                       
                                        int c_id = Integer.parseInt(words[1]);
                                        int w_id = Integer.parseInt(words[2]);
                                        int d_id = Integer.parseInt(words[3]);
                                        int m = Integer.parseInt(words[4]);
                                       
                                        int[] item_num = new int[m];
                                        int[] supplier_warehouse = new int[m];
                                        long[] quantity = new long[m];
                                        for (int j = 0; j < (m); j++) {
                                                String[] vals = (fileList.get(j+i+1)).split(",");
                                                item_num[j] = Integer.parseInt(vals[0]);
                                                supplier_warehouse[j] = Integer.parseInt(vals[1]);
                                                quantity[j] = (long)(Integer.parseInt(vals[2]));
                                        }
                                        i = i + m;
                                transaction.newOrderTransactions(w_id, d_id, c_id, m, item_num, supplier_warehouse, quantity);
                                count++;
                                }
								//Transaction 2.2 - Payment Transaction  
								else if ((words[0]).equals("P"))
									{
                                      
                                        int w_id = Integer.parseInt(words[1]);
                                        int d_id = Integer.parseInt(words[2]);
                                        int c_id = Integer.parseInt(words[3]);
                                        double payment = Double.parseDouble(words[4]);
                                transaction.PaymentTransactions(w_id, d_id, c_id, payment);
                                count++;
                                }
								//Transaction 2.3 - Delivery Transaction  
								else if ((words[0]).equals("D"))
									{
                                        
                               transaction.deliveryTransaction(Integer.parseInt(words[1]),Integer.parseInt(words[2]));
                               count++;
                                }
								//Transaction 2.4 - Order Status Transaction  
								else if ((words[0]).equals("O")) 
								{
                                        
                                transaction.orderStatusTransaction(Integer.parseInt(words[1]),Integer.parseInt(words[2]),Integer.parseInt(words[3]));
                                count++;
                                }
								//Transaction 2.5 - Stock Level Transaction  
								else if ((words[0]).equals("S")) 
								{
                                      
                                transaction.stockLevelTransaction(Integer.parseInt(words[1]),Integer.parseInt(words[2]),Integer.parseInt(words[3]),Integer.parseInt(words[4]));
                                count++;
                                }
								//Transaction 2.6 - Popular Item Transaction  
								else if ((words[0]).equals("I")) 
								{
                                     
                                transaction.popularItemTransaction(Integer.parseInt(words[1]),Integer.parseInt(words[2]),Integer.parseInt(words[3]));
                                count++;
                                }
								else
								{
                                 System.out.println("It is Invalid Transaction");
                                }
                        }

                } catch (IOException e)
				{
					    System.out.println("Exception Raised in IO");
                        e.printStackTrace();
                }
				catch (InvalidQueryException e)
				{
                        System.out.println("");
                        e.printStackTrace();
                } catch (Exception e) {
                        System.out.println("");
                        e.printStackTrace();
                } finally {
                        try 
						{
                          br.close();
                        }
						catch (IOException e)
						{
						 e.printStackTrace();
                        }
						catch (Exception e) 
						{
                        System.out.println("");
                         e.printStackTrace();
                        }
                }
                return count;
        }

		//read the file and calculate the time for the processesing. Perform BenchMark Testing
        public void run() {
                int count = 0;
                try
				{
                        count = this.readTransactionFile();

                } catch (Exception e)
				{
                        System.out.println("Thread Id: T" + threadNum + " has been Interrupted.");
                }
				finally 
				{
                        this.endTime = System.currentTimeMillis();
                        double totalTransTime = (endTime - startTime)/1000;
                        double throughputValue = count / totalTransTime;
                        double avgTransTime = totalTransTime / count;
                        System.err.println("\n\nThread Id: T" + threadNum + " : is handling Transactions:" + count + ", and the Total Time is : " + totalTransTime + "sec, Average Time per Transactions  is : "+throughputValue + ", Average Time is : " + avgTransTime);


                        TransactionClient.totalTransactions += count;
                        TransactionClient.sumTime += totalTransTime;

                        TransactionClient.Threadcount++;

                        if (TransactionClient.Threadcount == TransactionClient.clientCount) {
                      
                            TransactionClient.sumTime = (TransactionClient.sumTime)/1000;
              
                            double total = ((System.currentTimeMillis()) - TransactionClient.startTime)/1000;

                            double totalthroughputValue = TransactionClient.totalTransactions / total;
                            double totalAvgTime = TransactionClient.sumTime / TransactionClient.totalTransactions;
                            double avgTimeClientTook = TransactionClient.sumTime / TransactionClient.clientCount;
                            double clientResponseTime = total / TransactionClient.clientCount;
                            System.out.println("____________________________________________________________________________\n");

                            System.out.println("\nDisplay BenchMark Report for Group8 Cluster : \n");
                            System.out.println("____________________________________________________________________________\n");
                            System.out.println("Total Number of Transactions Processed : "+ TransactionClient.totalTransactions);
                            System.out.println("Total Time required for Processing the Transactions : " + total +" seconds" );
                            System.out.println("Total Number of Transactions processed/second: "+ totalthroughputValue);
                            System.out.println("Average Time/Transaction : "+ totalAvgTime +" seconds");
                            System.out.println("Average Time/Client      : "+ avgTimeClientTook +" seconds" );
                            System.out.println("Response Time/Client     : "+ clientResponseTime +" seconds");
                            System.out.println("\n____________________________________________________________________________\n");

                            TransactionClient.close();
                            System.exit(0);
    
	        }
        }
	}

        public void start()
		{
                if (t == null)
					{
                        t = new Thread(this,threadName);
                        t.start();
                }
        }
}

