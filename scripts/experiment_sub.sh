#!/bin/bash
#sh experiment_sub.sh $remoteUserName $client $executionDir $serverMachines $serverPort $idStart $idEnd $clientRunTime &
#                            1           2         3                4            5          6       7      8
remoteUserName=$1
client=$2
executionDir=$3
serverMachines=$4
serverPort=$5
idStart=$6
idEnd=$7
clientRunTime=$7

scp  client.sh $remoteUserName@$client:$executionDir
ssh $remoteUserName@$client "cd $executionDir; bash client.sh $serverMachines $serverPort $idStart $idEnd $clientRunTime"