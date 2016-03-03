Instruction to Run the program for D8/D40.
---------------------------------------------------
1. Configure the nodes using config files - cassandra.yaml.
2. Create Table structure present in Table.txt data
3. Load the appropriate data from location http://www.comp.nus.edu.sg/~cs4224/D8.zip
4. Download apache-cassandra-2.2.2 file in /temp folder
5. Copy the java files : wholeSaleDataTransactions.java, TransactionThread, and TransactionClient.java in /temp location
6. Copy the .sh file : runBatch.sh in /temp location
7. Compile the java files-
   javac -classpath cassandra-java-driver-2.0.2/cassandra-driver-core-2.0.2.jar:. wholeSaleDataTransactions.java
   javac -classpath cassandra-java-driver-2.0.2/cassandra-driver-core-2.0.2.jar:. TransactionThread.java
8. Place the corresponding D8 aand D40 xact-spec-files files in /temp folder before the runBatch.sh file
9. Run the .sh file using below command. Please note that this batch file will compile and run the TransactionClient.java file
   sh runBatch.sh 
10. The output with Benchmark Report will be generated in TransactionOutput.txt and the Thread Processing results in ThreadResult.txt.
   Please note the errors while execution will be generated in ThreadResult.txt file.


---------------------------------------------------
