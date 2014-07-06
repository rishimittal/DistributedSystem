#!/bin/bash

javac Programs/*.java
cd Programs
java XmlServer $1 &
java ByteServer $1 &
java XmlClient
java ByteClient
