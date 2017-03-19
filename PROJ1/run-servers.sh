#!/bin/bash

BACKUP_PATH="$HOME/workspace/sdis-proj1/bin"

for i in `seq 1 5`
do
java -cp $BACKUP_PATH backup.Peer 1.0 $i 1 224.0.0.1 2000 224.0.0.2 2002 224.0.0.3 2003 server &
done

echo 'Backup service initiated'
