#!/bin/bash
fileStartCount=1
fileEndCount=3
echo `rm -rf *.class; javac -classpath cassandra-java-driver-2.0.2/cassandra-driver-core-2.0.2.jar:. TransactionClient.java`
while (( fileStartCount!=fileEndCount ))
do
        echo "File"$fileStartCount" Processing"
        echo `java -classpath cassandra-java-driver-2.0.2/*:cassandra-java-driver-2.0.2/lib/*:. TransactionClient $fileStartCount > TransactionOutput.txt 2>ThreadResult.txt`
        fileStartCount=$((fileStartCount+1))
done

