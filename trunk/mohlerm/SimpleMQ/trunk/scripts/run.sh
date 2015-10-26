dbMachine=52.29.13.86
serverMachines=52.29.4.92 #,52.29.76.96
clientMachines=52.29.43.245 #,52.29.51.163
experimentConstant=5
for i in {5..20..5}
  do
    bash experiment.sh --dbMachine=$dbMachine --dbPersistent=false --serverMachines=$serverMachines --serverWorkerTotal=32 --clientMachines=$clientMachines --clientTotal=$i --clientWorkload=staticsmall --clientRunTime=300 --clientRunCount=-1 --remoteUserName=ubuntu --experimentId=$experimentConstant$i
    ~/tg/bin/telegram-cli -k ~/tg/tg-server.pub -W -e "msg Marcel_Mohler Experiments $experimentConstant$i small done"
b 
  done
