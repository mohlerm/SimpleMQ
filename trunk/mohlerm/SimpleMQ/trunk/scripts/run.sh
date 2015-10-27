dbMachine=52.28.84.191
serverMachines=52.28.43.155,52.28.181.169
clientMachines=52.29.45.70
experimentConstant=maxClientPerInstance
workLoads=('staticsmall' 'staticlarge')
for workload in "${workLoads[@]}" # different workloads
do
  for clients in {10..30..5} # different clients
  do
    for rep in {1..3..1} # repetitions for statistical reasons
    do
      bash experiment.sh --dbMachine=$dbMachine --dbPersistent=false --serverMachines=$serverMachines --serverWorkerTotal=64 --clientMachines=$clientMachines --clientTotal=$clients --clientWorkload=$workload --clientRunTime=300 --clientRunCount=-1 --remoteUserName=ubuntu --experimentId=$experimentConstant-$workload-$clients-$rep
    done
    ~/tg/bin/telegram-cli -k ~/tg/tg-server.pub -W -e "msg Marcel_Mohler Experiments $experimentConstant-$workload-$clients 3 repetitions done"
  done
done
   
