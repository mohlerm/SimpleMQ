#!/usr/bin/env bash
###############################
#
# Read command line arguments
#
###############################

#client.sh servermachine serverport noofclients clientRunTime

serverMachine=$1
serverPort=$2
noOfClients=$3
clientRunTime=$4
username=$(whoami)


######################################
#
# Run clients
#
######################################


clientIds=`seq $noOfClients`
pids=""
for clientId in $clientIds
do
	echo "    Start client: $clientId"
	screen -dmS client$clientId java -jar SimpleMQ_client.jar $clientId $serverMachine $serverPort staticsmall $clientRunTime
done

echo -ne "  Waiting for the clients to finish ... "
sleep 1
while [ `ps aux | grep java | grep $username | wc -l` != 1 ]
do
	sleep 1
done
echo "OK"

#
# Wait for the clients to finish
#echo -ne "  Waiting for the clients to finish ... "
#for f in $pids
#do
#	wait $f
#done
#echo "DONE"