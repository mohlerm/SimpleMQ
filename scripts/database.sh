function usage() {
	local programName=$1
	echo "Usage: $programName --dbMachine=<address> --remoteUserName=<username> --command=<start/stop/clean"
	exit -1
}

dbMachine=""
remoteUserName=""

dbPort=51230
executionDir="/home/ubuntu/simplemq"
#executionDir="/mnt/local/mohlerm"

# Extract command line arguments
TEMP=`getopt -o b: --long dbMachine:,remoteUserName:,command: \
     -n 'database.sh' -- "$@"`

if [ $? != 0 ] ; then echo "Terminating..." >&2 ; exit 1 ; fi

# Note the quotes around `$TEMP': they are essential!
eval set -- "$TEMP"

while true ; do
        case "$1" in
        		--dbMachine) dbMachine="$2" ; shift 2 ;;
                --remoteUserName) remoteUserName="$2" ; shift 2 ;;
                --command) command="$2" ; shift 2 ;;
                --) shift ; break ;;
                *) echo "Internal error!" ; exit 1 ;;
        esac
done

# Check for correctness of the commandline arguments
if [[ $dbMachine == "" || $remoteUserName == "" || $command == "" ]]
then
	usage $1
fi
if [ "$command" == "start" ]
then
    echo "  Setup PostgreSQL..."
    ssh -i ~/.ssh/id_aws $remoteUserName@$dbMachine "mkdir -p $executionDir"
    scp -i ~/.ssh/id_aws postgres_init.tar.gz $remoteUserName@$dbMachine:$executionDir
    #ssh -i ~/.ssh/id_aws $remoteUserName@$dbMachine "cd $executionDir; wget http://mohlerm.ch/simplemq/postgres_init.tar.gz"
    ssh -i ~/.ssh/id_aws $remoteUserName@$dbMachine "tar -zxf $executionDir/postgres_init.tar.gz -C $executionDir"
    sleep 1
    ssh -i ~/.ssh/id_aws $remoteUserName@$dbMachine  "screen -dmS postgres $executionDir/postgres/bin/postgres -D $executionDir/postgres/db/ -p $dbPort -i -k $executionDir/"
    sleep 3
    echo "OK"
elif [ "$command" == "stop" ]
then
    echo -ne "  Sending shut down signal to database..."
    ssh -i ~/.ssh/id_aws $remoteUserName@$dbMachine "screen -X -S postgres quit"
    ssh -i ~/.ssh/id_aws $remoteUserName@$dbMachine "killall postgres"
    echo "OK"
elif [ "$command" == "clean" ]
then
    echo -ne "  Clean directory"
    ssh -i ~/.ssh/id_aws $remoteUserName@$dbMachine "rm -Rf $executionDir"
    echo "OK"
fi

