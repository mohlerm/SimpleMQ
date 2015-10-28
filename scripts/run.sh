dbMachine=52.29.31.58
serverMachines=52.29.30.57
clientMachines=52.29.33.110
experimentConstant=maxThroughput
workLoads=('staticsmall') # 'staticlarge')
for workload in "${workLoads[@]}" # different workloads
do
  for clients in {60..180..20} # different clients
  do
    for workers in {60..180..20}
    do
      for rep in {1..3..1} # repetitions for statistical reasons
      do
        bash experiment.sh --dbMachine=$dbMachine --dbPersistent=false --serverMachines=$serverMachines --serverWorkerTotal=$workers --clientMachines=$clientMachines --clientTotal=$clients --clientWorkload=$workload --clientRunTime=300 --clientRunCount=-1 --remoteUserName=ubuntu --experimentId=$experimentConstant-$workload-$workers-$clients-$rep
      done
    done
   # ~/tg/bin/telegram-cli -k ~/tg/tg-server.pub -W -e "msg Marcel_Mohler Experiments $experimentConstant-$workload-$clients 3 repetitions done"
  done
done
