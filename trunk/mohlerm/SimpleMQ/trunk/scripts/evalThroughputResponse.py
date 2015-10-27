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
import scipy as sp
import scipy.stats

def confidence(data, confidence=0.95):
    a = 1.0*np.array(data)
    n = len(a)
    m, se = np.mean(a), scipy.stats.sem(a)
    h = se * sp.stats.t._ppf((1+confidence)/2., n-1)
    return h

if(len(sys.argv) != 4):
    print("Please supply experimentId, from:to:steps repetitions")
    sys.exit(1)
experimentId = str(sys.argv[1])
variable = str(sys.argv[2])
split = variable.split(":")
repetitions = int(sys.argv[3])
if(len(split) != 3):
    print("Please supply correct 2nd parameter: from:to:steps")
    sys.exit(1)
start = int(split[0])
stop = int(split[1])
step = int(split[2])

currFile = []
xValues = []
yThroughput = []
yThroughputErr = []
yResponseSnd = []
yResponseSndErr = []
yResponseSndUpper = []
yResponseSndUpperErr = []
yResponseRec = []
yResponseRecErr = []
yResponseRecUpper = []
yResponseRecUpperErr = []
result = []
for i in range(start,stop,step):
    xValues.append(i)
    currThrp = []
    currSnd = []
    currSndUpper = []
    currRec = []
    currRecUpper = []
    for r in range(1,repetitions+1):
      currFile = glob.glob(r''+experimentId+'-'+str(i)+'-'+str(r) + '/experiment_'+experimentId+'-'+str(i)+'-'+str(r)+'.log')
      print(experimentId+str(i) + '/experiment_'+experimentId+'-'+str(i)+'-'+str(r)+'.log')
      f = open(currFile[0])
      file = f.read()
      f.close()
      m = re.search(r"INFO:root:Throughput:\s*[-+]?([0-9]*\.[0-9]+|[0-9]+)\s*messages\/s",file)
      if(m is not None):
          currThrp.append(float(m.group(1)))
      m = re.search(r"INFO:root:Mean SEND acks:\s*[-+]?([0-9]*\.[0-9]+|[0-9]+)\s*\+\-[-+]?([0-9]*\.[0-9]+|[0-9]+)",file)
      if(m is not None):
          currSnd.append(float(m.group(1)))
          currSndUpper.append(float(m.group(1)) + float(m.group(2)))
      m = re.search(r"INFO:root:Mean RECEIVE acks:\s*[-+]?([0-9]*\.[0-9]+|[0-9]+)\s*\+\-[-+]?([0-9]*\.[0-9]+|[0-9]+)",file)
      if(m is not None):
          currRec.append(float(m.group(1)))
          currRecUpper.append(float(m.group(1)) + float(m.group(2)))
    # append average and error 
    yThroughput.append(statistics.mean(currThrp))
    yThroughputErr.append(confidence(currThrp))
    yResponseSnd.append(statistics.mean(currSnd))
    yResponseSndErr.append(confidence(currSnd))
    yResponseSndUpper.append(statistics.mean(currSndUpper))
    yResponseSndUpperErr.append(confidence(currSndUpper))
    yResponseRec.append(statistics.mean(currRec))
    yResponseRecErr.append(confidence(currRec))
    yResponseRecUpper.append(statistics.mean(currRecUpper))
    yResponseRecUpperErr.append(confidence(currRecUpper))

print('parameter;throughput;+-;sndresponsetime;+-;sndreponsetimeupper;+-;recresponsetime;+-;responsetimeupper;+-')
for i in range(0,len(xValues)):
  print(str(xValues[i])+';'+str(yThroughput[i])+';'+str(yThroughputErr[i])+';'+str(yResponseSnd[i])+';'+str(yResponseSndErr[i])+';'+str(yResponseSndUpper[i])+';'+str(yResponseSndUpperErr[i])+';'+str(yResponseRec[i])+';'+str(yResponseRecErr[i])+';'+str(yResponseRecUpper[i])+';'+str(yResponseRecUpperErr[i]))


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
