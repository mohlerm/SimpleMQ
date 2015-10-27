import os
import re
import glob
import sys

if(len(sys.argv) != 3):
    print("Please supply experimentId, from:to:steps")
    sys.exit(1)
experimentId = str(sys.argv[1])
variable = str(sys.argv[2])
split = variable.split(":")
if(len(split) != 3):
    print("Please supply correct 2nd parameter: from:to:steps")
    sys.exit(1)
start = int(split[0])
stop = int(split[1])
step = int(split[2])

files = []
for i in range(start,stop,step):
    files += glob.glob(experimentId+str(i) + 'experiment_'+experimentId+str(i)+'.log')
files.sort()
print(files)
result = []
for infile in files:
    linestring = ""
    f = open(infile)
    file = f.read()
    f.close()
    m = re.search(r"INFO:root:Throughput:\s*[-+]?([0-9]*\.[0-9]+|[0-9]+)\s*messages\/s",file)
    linestring = linestring +(";")
    if(m is not None):
        linestring = linestring + (m.group(1))
    m = re.search(r"INFO:root:Mean SEND acks:\s*[-+]?([0-9]*\.[0-9]+|[0-9]+)\s*\+\-[-+]?([0-9]*\.[0-9]+|[0-9]+)",file)
    linestring = linestring +(";")
    if(m is not None):
        linestring = linestring + (m.group(1))
    m = re.search(r"INFO:root:Mean SEND acks:\s*[-+]?([0-9]*\.[0-9]+|[0-9]+)\s*\+\-[-+]?([0-9]*\.[0-9]+|[0-9]+)",file)
    linestring = linestring +(";")
    if(m is not None):
        linestring = linestring + (m.group(1))
    if(linestring is not ""):
        result.append(linestring)
result.sort()
for b in result:
    print(b);

