#! /bin/sh
java -Xmx512M -cp .:lib/ECLA.jar:lib/DTNConsoleConnection.jar:lib/sqlite-jdbc-3.7.2.jar core.DTNSim $*
