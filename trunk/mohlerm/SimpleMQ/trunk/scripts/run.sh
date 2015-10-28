dbMachine=52.29.31.58
#serverMachines=52.29.30.57
clientMachines=52.29.33.110,52.29.79.231
#experimentConstant=maxClientPerInstance1srv
#workLoads=('staticsmall' 'staticlarge')
#for workload in "${workLoads[@]}" # different workloads
#do
#  for clients in {40..55..5} # different clients
#  do
#    for rep in {1..3..1} # repetitions for statistical reasons
#    do
#      bash experiment.sh --dbMachine=$dbMachine --dbPersistent=false --serverMachines=$serverMachines --serverWorkerTotal=64 --clientMachines=$clientMachines --clientTotal=$clients --clientWorkload=$workload --clientRunTime=300 --clientRunCount=-1 --remoteUserName=ubuntu --experimentId=$experimentConstant-$workload-$clients-$rep
#    done
#    ~/tg/bin/telegram-cli -k ~/tg/tg-server.pub -W -e "msg Marcel_Mohler Experiments $experimentConstant-$workload-$clients 3 repetitions done"
#  done
#done
serverMachines=52.29.30.57
experimentConstant=maxWorkerPerInstance
workLoads=('staticsmall' 'staticlarge')
for workload in "${workLoads[@]}" # different workloads
do
  for workers in {10..100..10} # different workers
  do
    for rep in {1..3..1} # repetitions for statistical reasons
    do
      bash experiment.sh --dbMachine=$dbMachine --dbPersistent=false --serverMachines=$serverMachines --serverWorkerTotal=$workers --clientMachines=$clientMachines --clientTotal=60 --clientWorkload=$workload --clientRunTime=300 --clientRunCount=-1 --remoteUserName=ubuntu --experimentId=$experimentConstant-$workload-$workers-$rep
    done
    ~/tg/bin/telegram-cli -k ~/tg/tg-server.pub -W -e "msg Marcel_Mohler Experiments $experimentConstant-$workload-$workers 3 repetitions done"
  done
done
#experimentConstant=longRun30Min
#workLoads=('staticsmall' 'staticlarge')
#for workload in "${workLoads[@]}" # different workloads
#do
#  for clients in {30..30..5} # different clients
#  do
#    for rep in {1..3..1} # repetitions for statistical reasons
#    do
#      bash experiment.sh --dbMachine=$dbMachine --dbPersistent=false --serverMachines=$serverMachines --serverWorkerTotal=64 --clientMachines=$clientMachines --clientTotal=$clients --clientWorkload=$workload --clientRunTime=1980 --clientRunCount=-1 --remoteUserName=ubuntu --experimentId=$experimentConstant-$workload-$clients-$rep
#    done
#    ~/tg/bin/telegram-cli -k ~/tg/tg-server.pub -W -e "msg Marcel_Mohler Experiments $experimentConstant-$workload-$clients 3 repetitions done"
#  done
#done  
