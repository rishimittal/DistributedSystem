#!/bin/bash

javac *.java
java RMIClient $1 $2 $3
