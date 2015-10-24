#!/bin/bash
#bash experiment_sub.sh $remoteUserName $client $executionDir $serverMachines $serverPort $clientTotal $idStart $idEnd $clientWorkload $clientRunTime $clientRunCount
#                            1           2         3                4            5          6           7      8                9       10                11
remoteUserName=$1
client=$2
executionDir=$3
serverMachines=$4
serverPort=$5
clientTotal=$6
idStart=$7
idEnd=$8
clientWorkload=$9
clientRunTime=$10
clientRunCount=$11
#echo "starting sub script $client"
scp -i ~/.ssh/id_aws client.sh $remoteUserName@$client:$executionDir
ssh -i ~/.ssh/id_aws $remoteUserName@$client "cd $executionDir; bash client.sh $serverMachines $serverPort $clientTotal $idStart $idEnd $clientWorkload $clientRunTime $clientRunCount"
#echo "done with sub script $client"
