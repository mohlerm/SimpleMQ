dbMachine=52.29.115.64
serverMachines=52.29.115.65,52.29.115.60
clientMachines=52.29.26.154,52.29.5.195
experimentConstant=maxThroughput2s2c
workLoads=('staticsmall') # 'staticlarge')
for workload in "${workLoads[@]}" # different workloads
do
  for clients in {60..200..20} # different clients
  do
    for workers in {40..100..20}
    do
      for rep in {1..3..1} # repetitions for statistical reasons
      do
        bash experiment.sh --dbMachine=$dbMachine --dbPersistent=false --serverMachines=$serverMachines --serverWorkerTotal=$workers --clientMachines=$clientMachines --clientTotal=$clients --clientWorkload=$workload --clientRunTime=300 --clientRunCount=-1 --clientThinkTime=0 --remoteUserName=ubuntu --experimentId=$experimentConstant-$workload-$workers-$clients-$rep
      done
      ~/tg/bin/telegram-cli -k ~/tg/tg-server.pub -W -e "msg Marcel_Mohler Experiments $experimentConstant-$workload-$workers-$clients 3 repetitions done"
    done
  done
done
