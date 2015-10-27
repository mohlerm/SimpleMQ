dbMachine=52.28.191.89
serverMachines=52.29.55.139
clientMachines=52.29.53.244
experimentConstant=maxclientperinstance1srv_lrg
for i in {10..30..5} #{5..20..5}
  do
    bash experiment.sh --dbMachine=$dbMachine --dbPersistent=false --serverMachines=$serverMachines --serverWorkerTotal=64 --clientMachines=$clientMachines --clientTotal=$i --clientWorkload=staticlarge --clientRunTime=300 --clientRunCount=-1 --remoteUserName=ubuntu --experimentId=$experimentConstant$i
    ~/tg/bin/telegram-cli -k ~/tg/tg-server.pub -W -e "msg Marcel_Mohler Experiments $experimentConstant$i large done"
  done
serverMachines=52.29.55.139,52.29.53.74
experimentConstant=maxclientperinstance2srv_lrg
for i in {10..30..5} #{5..20..5}
  do
    bash experiment.sh --dbMachine=$dbMachine --dbPersistent=false --serverMachines=$serverMachines --serverWorkerTotal=64 --clientMachines=$clientMachines --clientTotal=$i --clientWorkload=staticlarge --clientRunTime=300 --clientRunCount=-1 --remoteUserName=ubuntu --experimentId=$experimentConstant$i
    ~/tg/bin/telegram-cli -k ~/tg/tg-server.pub -W -e "msg Marcel_Mohler Experiments $experimentConstant$i large done"
  done 
