dbMachine=dryad07.ethz.ch
clientMachines=dryad08.ethz.ch
experimentConstant=dbOnly
workLoads=('directsmall') # 'staticlarge')
for workload in "${workLoads[@]}" # different workloads
do
  for clients in {60..200..20} # different clients
  do
    for rep in {1..3..1} # repetitions for statistical reasons
    do
      bash dbonly_experiment.sh --dbMachine=$dbMachine --dbPersistent=false --clientMachines=$clientMachines --clientTotal=$clients --clientWorkload=$workload --clientRunTime=300 --clientRunCount=-1 --clientThinkTime=0 --remoteUserName=ubuntu --experimentId=$experimentConstant-$workload-$workers-$clients-$rep
    done
    ~/tg/bin/telegram-cli -k ~/tg/tg-server.pub -W -e "msg Marcel_Mohler Experiments $experimentConstant-$workload-$clients 3 repetitions done"
  done
done
