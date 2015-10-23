#!/bin/bash
#sh experiment_sub.sh $remoteUserName $client $executionDir $serverMachines $serverPort $idStart $idEnd $clientWorkload $clientRunTime &
#                            1           2         3                4            5          6       7      8                9
remoteUserName=$1
client=$2
executionDir=$3
serverMachines=$4
serverPort=$5
idStart=$6
idEnd=$7
clientWorkload=$8
clientRunTime=$9
echo "starting sub script $client"
scp -i ~/.ssh/id_aws client.sh $remoteUserName@$client:$executionDir
ssh -i ~/.ssh/id_aws $remoteUserName@$client "cd $executionDir; bash client.sh $serverMachines $serverPort $idStart $idEnd $clientWorkload $clientRunTime"
echo "done with sub script $client"
