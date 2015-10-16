#!/bin/bash
###############################
#
# Read command line arguments
#
###############################

#sh client.sh $serverMachines $serverPort $idStart $idEnd $clientRunTime"
serverMachines=$1
serverPort=$2
idStart=$3
idEnd=$4
clientRunTime=$5
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
	screen -dmSl client$clientId java -jar SimpleMQ_client.jar $clientId ${servers[$serverId]} $serverPort staticsmall $clientRunTime
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