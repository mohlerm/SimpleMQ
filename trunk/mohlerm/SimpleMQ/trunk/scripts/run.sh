dbMachine=52.29.101.82
serverMachines=52.29.70.237 #
clientMachines=52.29.100.71
experimentConstant=maxclientperinstance1srv
for i in {15..30..5} #{5..20..5}
  do
    bash experiment.sh --dbMachine=$dbMachine --dbPersistent=false --serverMachines=$serverMachines --serverWorkerTotal=64 --clientMachines=$clientMachines --clientTotal=$i --clientWorkload=staticsmall --clientRunTime=300 --clientRunCount=-1 --remoteUserName=ubuntu --experimentId=$experimentConstant$i
    ~/tg/bin/telegram-cli -k ~/tg/tg-server.pub -W -e "msg Marcel_Mohler Experiments $experimentConstant$i small done"
  done
serverMachines=52.29.70.237,52.28.229.8
experimentConstant=maxclientperinstance2srv
for i in {15..30..5} #{5..20..5}
  do
    bash experiment.sh --dbMachine=$dbMachine --dbPersistent=false --serverMachines=$serverMachines --serverWorkerTotal=64 --clientMachines=$clientMachines --clientTotal=$i --clientWorkload=staticsmall --clientRunTime=300 --clientRunCount=-1 --remoteUserName=ubuntu --experimentId=$experimentConstant$i
    ~/tg/bin/telegram-cli -k ~/tg/tg-server.pub -W -e "msg Marcel_Mohler Experiments $experimentConstant$i small done"
  done 
