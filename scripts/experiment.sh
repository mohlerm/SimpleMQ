#!/bin/bash
# usage: sh experiment.sh --serverMachine=dryad07.ethz.ch --clientMachine=dryad08.ethz.ch --noOfClients=10 --remoteUserName=mohlerm --experimentId=0 --clientRunTime=100
# db:    /mnt/local/mohlerm/postgres/bin/postgres -D /mnt/local/mohlerm/postgres/db/ -p 51230 -i -k /mnt/local/mohlerm/
# ./createdb -h 127.0.0.1 -p 51230 -U testdb testdb
# ./createuser -h 127.0.0.1 -p 51230 --interactive
#
# Sample automation script that
#
# 1. Checks if passwordless login to server and client is working
# 2. Copies jar file to server and client machines
# 3. Runs server and waits for it to start listening to connections
# 4. Starts clients on client machines
# 5. Waits for clients to finish
# 6. Sends shut down signal to server and waits for it to shut down
# 7. Copies log files from client machine
# 8. Deletes log files from client and server machines
# 9. Processes log files
# 10. Plots the result with gnuplot
#

###############################
#
# Read command line arguments
#
###############################

function usage() {
	local programName=$1
	echo "Usage: $programName --dbMachine=<address> --serverMachine=<address> --clientMachine=<address> --noOfClients=<int> --remoteUserName=<username> --experimentId=<id> --clientRunTime=<seconds>"
	exit -1
}

dbMachine=""
serverMachine=""
clientMachine=""
noOfClients=""
remoteUserName=""
experimentId=""

serviceport=51234
clientRunTime=5
executionDir="/mnt/local/mohlerm"
serverStartMessage="Using server id: 0"

# Extract command line arguments
TEMP=`getopt -o b: --long dbMachine:,serverMachine:,clientMachine:,noOfClients:,remoteUserName:,experimentId:,clientRunTime: \
     -n 'example.bash' -- "$@"`

if [ $? != 0 ] ; then echo "Terminating..." >&2 ; exit 1 ; fi

# Note the quotes around `$TEMP': they are essential!
eval set -- "$TEMP"

while true ; do
        case "$1" in
        		--dbMachine) dbMachine="$2" ; shift 2 ;;
                --serverMachine) serverMachine="$2" ; shift 2 ;;
                --clientMachine) clientMachine="$2" ; shift 2 ;;
                --noOfClients) noOfClients="$2" ; shift 2 ;;
                --remoteUserName) remoteUserName="$2" ; shift 2 ;;
                --experimentId) experimentId="$2" ; shift 2 ;;
                --clientRunTime) clientRunTime="$2" ; shift 2 ;;
                --) shift ; break ;;
                *) echo "Internal error!" ; exit 1 ;;
        esac
done

# Check for correctness of the commandline arguments
if [[ $dbMachine == "" || $serverMachine == "" || $clientMachine == "" || $noOfClients == "" || $remoteUserName == "" || $experimentId == "" ]]
then
	usage $1
fi

#####################################
#
# Build server and clients locally
#
#####################################

echo -ne " Build using ant"
cd ..
ant >> ant.log
cd scripts

#####################################
#
# Copy server and clients to machines
#
#####################################

echo -ne "  Testing passwordless connection to the server machine and client machine... "
# Check if command can be run on server and client
success=$( ssh -o BatchMode=yes  $remoteUserName@$serverMachine echo ok 2>&1 )
if [ $success != "ok" ]
then
	echo "Passwordless login not successful for $remoteUserName on $serverMachine. Exiting..."
	exit -1
fi

success=$( ssh -o BatchMode=yes  $remoteUserName@$clientMachine echo ok 2>&1 )
if [ $success != "ok" ]
then
	echo "Passwordless login not successful for $remoteUserName on $clientMachine. Exiting..."
	exit -1
fi
echo "OK"

echo "  Create directories on all machines"
ssh $remoteUserName@$dbMachine "mkdir -p $executionDir"
ssh $remoteUserName@$serverMachine "mkdir -p $executionDir"
ssh $remoteUserName@$clientMachine "mkdir -p $executionDir"

echo "  Copying server.jar to server machine: $serverMachine"
# Copy jar to server machine
scp ../jar/SimpleMQ_server.jar $remoteUserName@$serverMachine:$executionDir
echo "  Copying client.jar to client machine: $clientMachine"
# Copy jar to client machine
scp ../jar/SimpleMQ_client.jar $remoteUserName@$clientMachine:$executionDir

######################################
#
# Move, unzip, configure and start SQL
#
######################################
#echo "  Setup PostgreSQL"
#ssh $remoteUserName@$dbMachine "cp /home/$remoteUserName/postgres_init.tar.gz $executionDir"
#ssh $remoteUserName@$dbMachine "tar -zxf $executionDir/postgres_init.tar.gz -C $executionDir"
#ssh $remoteUserName$dbMachine  "screen -dmS postgres $executionDir/postgres/bin/postgres -D $executionDir/postgres/db/ -p 51230 -i -k $executionDir/


######################################
#
# Run server and clients
#
######################################

# Run server
echo "  Starting the server"
ssh $remoteUserName@$serverMachine "java -jar $executionDir/SimpleMQ_server.jar 0 $serviceport $dbMachine 2>&1 > $executionDir/server.out " &

# Wait for the server to start up
echo -ne "  Waiting for the server to start up..."
sleep 1
while [ `ssh $remoteUserName@$serverMachine "cat $executionDir/server.out | grep '$serverStartMessage' | wc -l"` != 1 ]
do
	sleep 1
done
echo "OK"

echo "  Start the clients on the client machine: $clientMachine"
# Run the clients
clientIds=`seq $noOfClients`
pids=""
for clientId in $clientIds
do
	echo "    Start client: $clientId"
	ssh $remoteUserName@$clientMachine "cd $executionDir; java -jar $executionDir/SimpleMQ_client.jar $clientId $serverMachine $serviceport $clientRunTime > $executionDir/out.client${clientId}" &
	pids="$pids $!"
done

# Wait for the clients to finish
echo -ne "  Waiting for the clients to finish ... "
for f in $pids
do
	wait $f
done
echo "OK"

echo "  Sending shut down signal to server"
# Send a shut down signal to the server
# Note: server.jar catches SIGHUP signals and terminates gracefully
ssh $remoteUserName@$serverMachine "killall java"

#echo -ne "  Waiting for the server to shut down... "
# TODO
echo -ne "  Done"
# Wait for the server to gracefully shut down
while [ `ssh $remoteUserName@$serverMachine "cat $executionDir/server.out | grep 'Server shutting down' | wc -l"` != 1 ]
do
	sleep 1
done
echo "OK"

########################################
#
# Copy and process logs and plot graphs
#
########################################

# Copy log files from the clients
mkdir -p $experimentId
echo "  Copying log files from client machine... "
for clientId in $clientIds
do
	scp $remoteUserName@$clientMachine:$executionDir/client$clientId ./$experimentId/
done

# Cleanup
echo -ne "  Cleaning up files on client and server machines... "
ssh $remoteUserName@$clientMachine "rm $executionDir/client*; rm $executionDir/out.client*"
ssh $remoteUserName@$serverMachine "rm $executionDir/server.out"
echo "OK"

# Process the log files from the clients
echo "  Processing log files"
cat $experimentId/client* | sort -n > $experimentId/allclients

echo "  Generating trace.jpg with gnuplot"
gnuplot << EOF
set terminal jpeg
set output '$experimentId/trace.jpg'
set xlabel 'Time (s)'
set ylabel 'Response Time (ms)'
set title 'Trace log'
set xrange [0:]
set yrange [0:]
plot '$experimentId/allclients' using (\$1/1000):2 with lp title "$experimentId"
EOF
