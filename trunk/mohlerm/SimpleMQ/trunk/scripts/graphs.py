import os
import re
import sys
import numpy
import statistics

import matplotlib
#matplotlib.use('Agg')
import matplotlib.pyplot as plt

if(len(sys.argv) != 3):
    print("Please supply an experiment id and log file")
    sys.exit(1)

experimentId = sys.argv[1]
print("Using experimentID="+experimentId)
# import log file
inputfile = open(experimentId+'/'+sys.argv[2])
# parse log file into datastructure
result_acks = []
result_messages = []
requests_acks = []
requests_messages = []
for line in inputfile:
    m = re.search(r"ANS\|Ack\|(\d*)\|\d*\|\d*\|\S*\|\|[-+]?([0-9]*\.[0-9]+|[0-9]+)",line)
    if(m is not None):
        requests_acks.append(m.group(1))
        result_acks.append(float(m.group(2)))
    m = re.search(r"ANS\|Message\|(\d*)\|\d*\|\d*\|\S*\|\|[-+]?([0-9]*\.[0-9]+|[0-9]+)",line)
    if(m is not None):
        requests_messages.append(m.group(1))
        result_messages.append(float(m.group(2)))
#
print("Mean acks: " + str(statistics.mean(result_acks)) + "+-" + str(statistics.stdev(result_acks)))
print("Mean msg:  " + str(statistics.mean(result_messages)) + "+-" +str(statistics.stdev(result_messages)))



# plot it *yay*
#t = arange(0.0, 2.0, 0.01)
#s = sin(2*pi*t)
#plt.plot_date(x=datestamp, y=value, fmt='-', linewidth=2)
plt.plot(requests_acks, result_acks, 'r.', label='ACKS')
plt.plot(requests_messages, result_messages, 'b.', label="MSG")
plt.xlabel('request number')
plt.ylabel('response time in ms')
plt.title('Response time')
plt.legend(loc='upper right')
plt.grid(True)
plt.savefig("test.png")
plt.show()