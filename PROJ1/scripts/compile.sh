#!/bin/bash

mkdir -p bin

javac -d bin src/backup/*.java src/backup/listeners/*.java src/backup/responseHandlers/*.java src/backup/initiators/*.java

