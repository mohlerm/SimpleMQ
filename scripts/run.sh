dbMachine=52.28.191.89
serverMachines=52.29.55.139,52.29.53.74
clientMachines=52.29.53.244
experimentConstant=maxClientPerInstance
declare -a workLoads={'staticsmall','staticlarge'}
for workload in "${workLoads[@]}" # different workloads
do
  for clients in {10..30..5} # different clients
  do
    for rep in {1..5..1} # repetitions for statistical reasons
    do
      bash experiment.sh --dbMachine=$dbMachine --dbPersistent=false --serverMachines=$serverMachines --serverWorkerTotal=64 --clientMachines=$clientMachines --clientTotal=$clients --clientWorkload=$workload --clientRunTime=300 --clientRunCount=-1 --remoteUserName=ubuntu --experimentId=$experimentConstant-$workload-$clients-$rep
    done
    ~/tg/bin/telegram-cli -k ~/tg/tg-server.pub -W -e "msg Marcel_Mohler Experiments $experimentConstant-$workload-$clients 5 repetitions done"
  done
done
   
