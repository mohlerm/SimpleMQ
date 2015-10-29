#!/bin/bash
###############################
#
# Read command line arguments
#
###############################

#bash client.sh $serverMachines $serverPort $clientTotal $idStart $idEnd $clientWorkload $clientRunTime $clientRunCount $clientThinkTime"
#                    1               2           3          4       5           6              7             8               9
serverMachines=$1
serverPort=$2
clientTotal=$3
idStart=$4
idEnd=$5
clientWorkload=$6
clientRunTime=$7
clientRunCount=$8
clientThinkTime=$9
username=$(whoami)

######################################
#
# Run clients
#
######################################

serverCount=0
declare -a servers
IFS=',' read -ra ADDR <<< "$serverMachines"
for i in "${ADDR[@]}"; do
    servers[serverCount]=$i
    ((serverCount++))
done

clientIds=`seq $idStart $idEnd`
#pids=""
for clientId in $clientIds
do
	serverId=$((clientId%serverCount))
	echo "    Start client: $clientId on server: ${servers[$serverId]}"
	screen -dmS client$clientId java -jar SimpleMQ_client.jar $clientId ${servers[$serverId]} $serverPort $clientTotal $clientWorkload $clientRunTime $clientRunCount $clientThinkTime
done

#echo -ne "  Waiting for the clients to finish ... "
sleep 1
while [ `ps aux | grep java | grep $username | wc -l` != 1 ]
do
	sleep 1
done
#echo "OK"

echo -ne "  Packing logs ... "
cd logs
tar czf ../client_logs.tar.gz *.*
cd ..
