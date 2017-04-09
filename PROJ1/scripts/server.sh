#!/bin/bash

# argument 1: id of peer
# argument 2: access point of peer (rmi object name)

java -cp ./bin backup.Peer 1.0 $1 $2 224.0.0.1 2000 224.0.0.2 2002 224.0.0.3 2003

