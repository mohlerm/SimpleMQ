#!/bin/bash
# usage: bash experiment.sh --dbMachine=52.28.240.101 --serverMachines=52.29.50.196 --serverWorkerTotal=8 --clientMachines=52.29.49.29 --clientTotal=10 --clientWorkload=staticsmall --clientRunTime=100 --clientRunCount=100 --remoteUserName=ubuntu --experimentId=2
# db:    /mnt/local/mohlerm/postgres/bin/postgres -D /mnt/local/mohlerm/postgres/db/ -p 51230 -i -k /mnt/local/mohlerm/
# ./createdb -h 127.0.0.1 -p 51230 -U testdb testdb
# ./createuser -h 127.0.0.1 -p 51230 --interactive
#
# staticsmall | staticmedium | staticlarge
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
	echo "Usage: $programName --dbMachine=<address> --dbPersistent=<true/false> --serverMachines=<address> --serverWorkerTotal=<int> --clientMachines=<address> --clientTotal=<int> --clientWorkload=<string> --clientRunTime=<seconds> --clientRunCount=<int> --remoteUserName=<username> --experimentId=<id>"
	exit -1
}

dbMachine=""
dbPersistent=""
serverMachines=""
serverWorkerTotal=""
clientMachines=""
clientWorkload=""
clientTotal=""
clientRunTime=""
clientRunCount=""
remoteUserName=""
experimentId=""

serverPort=51234 # keep in mind first server uses +1, second uses +2 etc...
dbPort=51230
executionDir="/home/ubuntu/simplemq"
#executionDir="/mnt/local/mohlerm"
serverStartMessage="Using server id: "

# Extract command line arguments
TEMP=`getopt -o b: --long dbMachine:,dbPersistent:,serverMachines:,serverWorkerTotal:,clientMachines:,clientTotal:,clientWorkload:,clientRunTime:,clientRunCount:,remoteUserName:,experimentId: \
     -n 'experiment.sh' -- "$@"`

if [ $? != 0 ] ; then echo "Terminating..." >&2 ; exit 1 ; fi

# Note the quotes around `$TEMP': they are essential!
eval set -- "$TEMP"

while true ; do
        case "$1" in
        		--dbMachine) dbMachine="$2" ; shift 2 ;;
        		--dbPersistent) dbPersistent="$2" ; shift 2 ;;
                --serverMachines) serverMachines="$2" ; shift 2 ;;
                --serverWorkerTotal) serverWorkerTotal="$2" ; shift 2 ;;
                --clientMachines) clientMachines="$2" ; shift 2 ;;
                --clientTotal) clientTotal="$2" ; shift 2 ;;
                --clientWorkload) clientWorkload="$2" ; shift 2 ;;
                --clientRunTime) clientRunTime="$2" ; shift 2 ;;
                --clientRunCount) clientRunCount="$" ; shift 2 ;;
                --remoteUserName) remoteUserName="$2" ; shift 2 ;;
                --experimentId) experimentId="$2" ; shift 2 ;;
                --) shift ; break ;;
                *) echo "Internal error!" ; exit 1 ;;
        esac
done

# Check for correctness of the commandline arguments
if [[ $dbMachine == "" || dbPersistent == "" || $serverMachines == "" || $serverWorkerTotal == "" || $clientMachines == "" || $clientTotal == "" || $clientWorkload == "" || $clientRunTime == "" || $clientRunCount == "" || $remoteUserName == "" || $experimentId == "" ]]
then
	usage $1
fi

#####################################
#
# Parse servers and client inputs
#
#####################################

declare -a servers
declare -a clients
serverCount=0
clientCount=0
IFS=',' read -ra ADDR <<< "$serverMachines"
for i in "${ADDR[@]}"; do
    servers[serverCount]=$i
    ((serverCount++))
done
IFS=',' read -ra ADDR <<< "$clientMachines"
for i in "${ADDR[@]}"; do
    clients[clientCount]=$i
    ((clientCount++))
done

echo -ne "servers: "$serverCount"\t"
echo ${servers[*]}
echo -ne "clients: "$clientCount"\t"
echo ${clients[*]}

#####################################
#
# Save configuration
#
#####################################
rm -R $experimentId
mkdir -p $experimentId
echo -e "#!/bin/bash\nbash experiment.sh --dbMachine=$dbMachine --dbPersistent=$dbPersistent --serverMachines=$serverMachines --serverWorkerTotal=$serverWorkerTotal --clientMachines=$clientMachines --clientTotal=$clientTotal --clientWorkload=$clientWorkload --clientRunTime=$clientRunTime --remoteUserName=$remoteUserName --experimentId=$experimentId" > $experimentId/experiment_$experimentId.sh

#####################################
#
# Build server and clients locally
#
#####################################

echo -ne " Build using ant..."
cd ..
ant >> ant.log
cd scripts
echo "OK"
#####################################
#
# Cleanup initially
#
#####################################
if [ "$dbPersistent" == "false" ]
then
    echo -ne "  Sending shut down signal to database..."
    ssh -i ~/.ssh/id_aws $remoteUserName@$dbMachine "screen -X -S postgres quit"
    ssh -i ~/.ssh/id_aws $remoteUserName@$dbMachine "killall -u $remoteUserName postgres"
    echo "OK"
else
    echo "  Do not shut down database"
fi
for client in "${clients[@]}"
do
    echo -ne "  Sending shut down signal to client $client..."
    ssh -i ~/.ssh/id_aws $remoteUserName@$client "killall -u $remoteUserName java"
    echo "OK"
done
for server in "${servers[@]}"
do
    echo -ne "  Sending shut down signal to server $server..."
    ssh -i ~/.ssh/id_aws $remoteUserName@$server "killall -u $remoteUserName java"
    echo "OK"
done
# Cleanup
echo -ne " Cleanup directories..."
for server in "${servers[@]}"
do
    ssh -i ~/.ssh/id_aws $remoteUserName@$server "rm -Rf $executionDir"
done
for client in "${clients[@]}"
do
    ssh -i ~/.ssh/id_aws $remoteUserName@$client "rm -Rf $executionDir"
done
if [ "$dbPersistent" == "false" ]
then
    ssh -i ~/.ssh/id_aws $remoteUserName@$dbMachine "rm -Rf $executionDir"
fi
echo "OK"

#####################################
#
# Copy server and clients to machines
#
#####################################

echo -ne "  Testing passwordless connection to the server and client machines... "
# Check if command can be run on server and client
for server in "${servers[@]}"
do
    success=$( ssh -i ~/.ssh/id_aws -o BatchMode=yes  $remoteUserName@$server echo ok 2>&1 )
    if [ $success != "ok" ]
    then
        echo "Passwordless login not successful for $remoteUserName on $server. Exiting..."
        exit -1
    fi
done

for client in "${clients[@]}"
do
    success=$( ssh -i ~/.ssh/id_aws -o BatchMode=yes  $remoteUserName@$client echo ok 2>&1 )
    if [ $success != "ok" ]
    then
	    echo "Passwordless login not successful for $remoteUserName on $client. Exiting..."
	    exit -1
    fi
done
echo "OK"

#echo -ne "  Copying server.jar to server machines: $serverMachine..."
# Copy jar to server machine
for server in "${servers[@]}"
do
    ssh -i ~/.ssh/id_aws $remoteUserName@$server "mkdir -p $executionDir"
    scp -i ~/.ssh/id_aws ../jar/SimpleMQ_server.jar $remoteUserName@$server:$executionDir
done
#echo "OK"
#echo -ne "  Copying client.jar to client machines: $clientMachine"
# Copy jar to client machine
for client in "${clients[@]}"
do
    ssh -i ~/.ssh/id_aws $remoteUserName@$client "mkdir -p $executionDir"
    scp -i ~/.ssh/id_aws ../jar/SimpleMQ_client.jar $remoteUserName@$client:$executionDir
done
#echo "OK"

######################################
#
# Move, unzip, configure and start SQL
#
######################################
if [ "$dbPersistent" == "false" ]
then
    echo -ne "  Setup PostgreSQL..."
    ssh -i ~/.ssh/id_aws $remoteUserName@$dbMachine "mkdir -p $executionDir"
    scp -i ~/.ssh/id_aws postgres_init.tar.gz $remoteUserName@$dbMachine:$executionDir
    #ssh -i ~/.ssh/id_aws $remoteUserName@$dbMachine "cd $executionDir; wget http://mohlerm.ch/simplemq/postgres_init.tar.gz"
    ssh -i ~/.ssh/id_aws $remoteUserName@$dbMachine "tar -zxf $executionDir/postgres_init.tar.gz -C $executionDir"
    sleep 1
    ssh -i ~/.ssh/id_aws $remoteUserName@$dbMachine  "screen -dmS postgres $executionDir/postgres/bin/postgres -D $executionDir/postgres/db/ -p $dbPort -i -k $executionDir/"
    sleep 3
    echo "OK"
else
    echo "  Reusing previous PostgreSQL instance"
fi

######################################
#
# Run server and clients remotely
#
######################################

# Run server
i=1
serverWorker=$(($serverWorkerTotal/$serverCount))
for server in "${servers[@]}"
do
    echo "  Starting the server $server"
    #port=$(($serverPort+$i))
    screen -dmS server$i ssh -i ~/.ssh/id_aws $remoteUserName@$server "cd $executionDir; java -jar SimpleMQ_server.jar $i $serverPort $serverWorker $dbMachine $dbPort" # 2>&1 > $executionDir/server_$i.log"

    # Wait for the server to start up
    echo -ne "  Waiting for the server to start up..."
    sleep 1
    while [ `ssh -i ~/.ssh/id_aws $remoteUserName@$server "cat $executionDir/logs/server_$i.log | grep '$serverStartMessage$i' | wc -l"` != 1 ]
    do
	    sleep 1
    done
    echo "OK"
    ((i++))
done

idStart=1
idStep=$(($clientTotal/$clientCount))
idEnd=$idStep
for client in "${clients[@]}"
do
    echo "  Start the clients id$idStart to id$idEnd on the client machine: $client"
    # we use all servermachines
    screen -dmS client_sub$idStart_$idEnd bash experiment_sub.sh $remoteUserName $client $executionDir $serverMachines $serverPort $clientTotal $idStart $idEnd $clientWorkload $clientRunTime $clientRunCount
    # Run the clients
    idStart=$(($idStart+$idStep))
    idEnd=$(($idEnd+$idStep))
done

echo -ne "  Waiting for the clients to finish ..."
sleep 1
while [ `ps aux | grep "bash experiment_sub.sh" | grep $(whoami) | wc -l` != 1 ]
do
	sleep 1
	echo -ne "..."
done
echo "OK"

if [ "$dbPersistent" == "false" ]
then
    echo -ne "  Sending shut down signal to database..."
    ssh -i ~/.ssh/id_aws $remoteUserName@$dbMachine "screen -X -S postgres quit"
    ssh -i ~/.ssh/id_aws $remoteUserName@$dbMachine "killall -u $remoteUserName postgres"
    echo "OK"
else
    echo "  Do not shut down database"
fi


# Send a shut down signal to the server
# Note: server.jar catches SIGHUP signals and terminates gracefully
for server in "${servers[@]}"
do
    echo -ne "  Sending shut down signal to server $server..."
    ssh -i ~/.ssh/id_aws $remoteUserName@$server "killall -u $remoteUserName java"
    echo "OK"
done


#echo -ne "  Waiting for the server to shut down... "
# TODO
#echo -ne "  Done"
# Wait for the server to gracefully shut down
#while [ `ssh $remoteUserName@$serverMachine "cat $executionDir/server.out | grep 'Server shutting down' | wc -l"` != 1 ]
#do
#	sleep 1
#done
#echo "OK"

########################################
#
# Copy and process logs and plot graphs
#
########################################

# Copy log files from the clients
#clientIds=`seq $clientTotal`

echo "  Copying tar'd log files from client machine untar and delete tar"

for client in "${clients[@]}"
do
    scp -i ~/.ssh/id_aws $remoteUserName@$client:$executionDir/client_logs.tar.gz ./$experimentId/
    cd $experimentId
    tar xzfm client_logs.tar.gz
    rm client_logs.tar.gz
    cd ..
done
i=1
for server in "${servers[@]}"
do
    echo "  Taring log file from server $server"
    ssh -i ~/.ssh/id_aws $remoteUserName@$server "cd $executionDir/logs; tar czf server_$i.tar.gz server_$i.log"
    echo "  Copying log files from server $server"
    scp -i ~/.ssh/id_aws $remoteUserName@$server:$executionDir/logs/server_$i.tar.gz ./$experimentId/
    ((i++))
done

# Cleanup
#echo -ne "  Cleaning up files on client and server machines... "
#ssh $remoteUserName@$clientMachine "rm $executionDir/logs/.client*"
#ssh $remoteUserName@$serverMachine "rm $executionDir/server.out"
#echo "OK"
echo -ne " Cleanup directories..."
for server in "${servers[@]}"
do
    ssh -i ~/.ssh/id_aws $remoteUserName@$server "rm -Rf $executionDir"
done
for client in "${clients[@]}"
do
    ssh -i ~/.ssh/id_aws $remoteUserName@$client "rm -Rf $executionDir"
done
if [ "$dbPersistent" == "false" ]
then
    ssh -i ~/.ssh/id_aws $remoteUserName@$dbMachine "rm -Rf $executionDir"
fi
echo "OK"



# Process the log files from the clients
echo "  Processing log files"
cat $experimentId/client* | sort -n > $experimentId/allclients.log

#echo "  Generating trace.jpg with gnuplot"
#gnuplot << EOF
#set terminal jpeg
#set output '$experimentId/trace.jpg'
#set xlabel 'Time (s)'
#set ylabel 'Response Time (ms)'
#set title 'Trace log'
#set xrange [0:]
#set yrange [0:]
#plot '$experimentId/allclients.log' using (\$1/1000):2 with lp title "$experimentId"
#EOF

echo "  Parse with python and generate graphs using matplotlib"
python3 graphs.py $clientCount $experimentId allclients.log | tee "$experimentId/experiment_$experimentId.txt"
