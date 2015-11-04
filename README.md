# SimpleMQ
A rather simple message passing system written in Java.
The system uses a PostgreSQL database as a storage backend and an arbitrary amount of middleware servers.
Clients connect to a middleware server to be able to send and receive messages to and from queues.

## Compile
This project uses ant. Simply check out the repository and run 
```
ant build
```
## Usage
the `/scripts` folder contains a script called `experiment.sh` that allows you to simply deploy the system to a set of servers.
```
bash experiment.sh --dbMachine=dryad01.ethz.ch --dbPersistent=false 
--serverMachines=dryad02.ethz.ch,dryad03.ethz.ch --serverWorkerTotal=60 
--clientMachines=dryad04.ethz.ch,dryad05.ethz.ch --clientTotal=100 
--clientWorkload=staticsmall --clientRunTime=300 --clientRunCount=-1 
--clientThinkTime=0 --remoteUserName=ubuntu --experimentId=6
```
The parameter `dbMachine` specifies the IP of the system where the database should be deployed. 
`dbPersistent` is a flag that can be used to make the database state persistent between multiple benchmark runs.
`serverMachines` takes an arbitrary amount of IPs separated by commas where the middlewares will be deployed on. \texttt{serverWorkerTotal} specifies the total amount of workers over all middlewares. \\
`clientMachines` is similar to `serverMachines` but used for the client instances. `clientTotal` defines the total amount of clients. When using n `clientMachines` and m `clientTotal` each instance will run m/n (rounded down) clients. `clientWorkload` defines the workload of the experiment. `clientRunTime` is used to limit the execution time of each client in seconds. Instead of seconds one can also limit the client run time by number of request by using `clientRunCount`. -1 defines no limit. If one wants to add a think time between each client request, `clientThinkTime` can be modified.
`remoteUserName` specify the name of the user for the remote systems.
`experimentId` allows to give this experiment a name for better categorization.

The script will then execute the following steps automatically:
1. parse command line parameter 
2. build server and client from source into a .jar using ant
3. check if passwordless login is enabled 
4. cleanup servers in case some old experiments are still running 
5. copy compiled .jar's to client and server instances 
6. setup postgres 
7. run database, server and clients remotely 
8. wait for the clients to finish 
9. shutdown database and server 
10. copy log files from remote locations 
11. invoke further processing of log files

