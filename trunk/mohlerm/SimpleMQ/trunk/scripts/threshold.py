import os
import re
import glob
import sys
import statistics
import matplotlib
matplotlib.use('Agg')
import matplotlib.pyplot as plt
import numpy as np
import matplotlib.mlab as mlab
from matplotlib.ticker import FuncFormatter


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

currFile = []
xValues = []
yThroughput = []

yResponseSnd = []
yResponseSndErr = []
yResponseRec = []
yResponseRecErr = []
result = []
for i in range(start,stop,step):
    xValues.append(i)
    currFile = glob.glob(r''+experimentId+str(i) + '/experiment_'+experimentId+str(i)+'.log')
    linestring = str(i)+";"
    f = open(currFile[0])
    file = f.read()
    f.close()
    m = re.search(r"INFO:root:Throughput:\s*[-+]?([0-9]*\.[0-9]+|[0-9]+)\s*messages\/s",file)
    if(m is not None):
        linestring += (m.group(1))
        yThroughput.append(float(m.group(1)))
    m = re.search(r"INFO:root:Mean SEND acks:\s*[-+]?([0-9]*\.[0-9]+|[0-9]+)\s*\+\-[-+]?([0-9]*\.[0-9]+|[0-9]+)",file)
    linestring = linestring +(";")
    if(m is not None):
        linestring += ((m.group(1)) + (";") + (m.group(2)))
        yResponseSnd.append(float(m.group(1)))
        yResponseSndErr.append(float(m.group(2)))
    m = re.search(r"INFO:root:Mean RECEIVE acks:\s*[-+]?([0-9]*\.[0-9]+|[0-9]+)\s*\+\-[-+]?([0-9]*\.[0-9]+|[0-9]+)",file)
    linestring = linestring +(";")
    if(m is not None):
        linestring += ((m.group(1)) + (";") + (m.group(2)))
        yResponseRec.append(float(m.group(1)))
        yResponseRecErr.append(float(m.group(2)))
    if(linestring is not ""):
        result.append(linestring)
result.sort()
for b in result:
    print(b);

plt.figure()
plt.errorbar(xValues, yThroughput, fmt='b-', label="Throughput")
plt.xlabel('Number of whatever')
plt.ylabel('Average throughput [in messages/second]')
plt.savefig(experimentId+"_througput_per_parameter.pdf")
plt.clf()

plt.figure()
plt.errorbar(xValues, yResponseSnd, fmt='b-', yerr=yResponseSndErr)
plt.errorbar(xValues, yResponseRec, fmt='r-', yerr=yResponseRecErr)
plt.xlabel('Number of whatever')
plt.ylabel('Average response time [in milliseconds]')
plt.savefig(experimentId+"_responsetime_per_parameter.pdf")
plt.clf()
