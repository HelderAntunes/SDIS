#!/bin/bash

BIN_FOLDER="/usr/users2/mieic2014/up201406163/workspace/sdis-proj/bin"
echo $BIN_FOLDER

echo 'Welcome to backup service!'
echo 'Backup server configuration'

read -p "Enter id of server: " id
id=${id:-Richard}

read -p "Enter name of the remote object(RMI) providing the testing service: " rmi
rmi=${rmi:-rmi_obj}

# args = [version, id, rmi_id_server, [MC], [MDB], [MDR]] 
java -cp $BACKUP_PATH backup.Peer 1.0 $id $rmi 224.0.0.1 2000 224.0.0.2 2002 224.0.0.3 2003

echo 'Backup server initiated.'
