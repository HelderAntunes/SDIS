Project 1 -- Distributed Backup Service

The explanation of protocol enhancements is found in the /doc folder.

Instructions for compiling and running:

To compile (scripts/compile.sh):
mkdir -p bin
javac -d bin src/backup/*.java src/backup/listeners/*.java src/backup/responseHandlers/*.java src/backup/initiators/*.java

Init rmi registry (scripts/init_rmi.sh):
cd ./bin/
rmiregistry 1051 &

To run a peer (scripts/server.sh, two arguments - id and rmi object name):
java -cp <bin_folder> backup.Peer <version> <id> <rmi_ap> <MC_addr> <MC_port> <MDB_addr> <MDB_port> <MDR_addr> <MDR_port>

To run the testing application:
java -cp <bin_folder> backup.TestApp <peer_ap> <sub_protocol> <opnd_1> <opnd_2>

In the /files folder are some sample files to test the application.

NOTE: All commands and scripts assume that the current directory is the project folder.


Authors: 
Hélder Antunes, 201406163
Pedro Pacheco, 201406316
 
t2-g05, SDIS 2016-2017
