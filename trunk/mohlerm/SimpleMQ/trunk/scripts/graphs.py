import os
import re
import sys
import numpy
import statistics
import datetime

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
ans_snd_time = []
ans_snd_index = []
ans_snd_response = []
ans_rcv_time = []
ans_rcv_index = []
ans_rcv_response = []
req_snd_time = []
req_snd_index = []
req_rcv_time = []
req_rcv_index = []
for line in inputfile:
    found = False
    # TODO exclude the acks for 0 messages (where insert did not return a valid message id)
    # If message is a send answer
    m = re.search(r"(\d*-\d*-\d*\s\d*:\d*:\d*,\d*)\s*\S*\s*\S*\s*ANS\|SendMessageToAll\|(\d*)\|\d*\|\d*\|\S*\|\|[-+]?([0-9]*\.[0-9]+|[0-9]+)",line)
    if m is not None:
        ans_snd_time.append(m.group(1))
        ans_snd_index.append(m.group(2))
        ans_snd_response.append(float(m.group(3)))
        found = True
    # if message is a peek/pop answer
    m = re.search(r"(\d*-\d*-\d*\s\d*:\d*:\d*,\d*)\s*\S*\s*\S*\s*ANS\|(Pop|Peek)Queue\|(\d*)\|\d*\|\d*\|(small|medium|large)Message\|\|[-+]?([0-9]*\.[0-9]+|[0-9]+)",line)
    if m is not None:
        ans_rcv_time.append(m.group(1))
        # match group 2 is peek/pop which isn't used currently
        ans_rcv_index.append(m.group(3))
        ans_rcv_response.append(float(m.group(5)))
        found = True
    # if message is a send request
    m = re.search(r"(\d*-\d*-\d*\s\d*:\d*:\d*,\d*)\s*\S*\s*\S*\s*REQ\|SendMessageToAll\|(\d*)\|\d*\|\d*\|\d*\|(small|medium|large)Message",line)
    if m is not None:
        req_snd_time.append(m.group(1))
        req_snd_index.append(m.group(2))
        found = True
    m = re.search(r"(\d*-\d*-\d*\s\d*:\d*:\d*,\d*)\s*\S*\s*\S*\s*REQ\|(Peek|Pop)Queue\|(\d*)\|\d*\|\d*\|\d*\|null",line)
    if m is not None:
        req_rcv_time.append(m.group(1))
        req_rcv_index.append(m.group(3))
        found = True
   # if found == False:
       # print(line)

#
print("Mean acks:              " + str(statistics.mean(ans_snd_response)) + "+-" + str(2*statistics.stdev(ans_snd_response)))
print("Mean msg:               " + str(statistics.mean(ans_rcv_response)) + "+-" +str(2*statistics.stdev(ans_rcv_response)))

snd_miliseconds = []
rcv_miliseconds = []
for s in ans_snd_time:
    snd_miliseconds.append(datetime.datetime.strptime(s,"%Y-%m-%d %H:%M:%S,%f"))
for s in ans_rcv_time:
    rcv_miliseconds.append(datetime.datetime.strptime(s,"%Y-%m-%d %H:%M:%S,%f"))

startDate = datetime.datetime.strptime(req_snd_time[0],"%Y-%m-%d %H:%M:%S,%f")
endDate = datetime.datetime.strptime(ans_snd_time[len(ans_snd_time)-1],"%Y-%m-%d %H:%M:%S,%f")
print("First request:          " + str(startDate))
print("Last answer:            " + str(endDate))
total_seconds = (endDate-startDate).total_seconds()
print("Difference:             " + str(total_seconds))

print("Number of REQ sends:    " + str(len(req_snd_time)))
print("Number of REQ receives: " + str(len(req_rcv_time)))
print("Number of ANS sends:    " + str(len(ans_snd_time)))
print("Number of ANS receives: " + str(len(ans_rcv_time)))

print("Throughput:             " + str( (len(ans_snd_time)+len(ans_rcv_time))/total_seconds) + " messages/s")


# plot it *yay*
#t = arange(0.0, 2.0, 0.01)
#s = sin(2*pi*t)
#plt.plot_date(x=datestamp, y=value, fmt='-', linewidth=2)
plt.plot(snd_miliseconds, ans_snd_response, 'r.', label='SEND (message send)')
plt.plot(rcv_miliseconds, ans_rcv_response, 'b.', label="RECEIVE (message peak/pop)")
plt.xlabel('request number')
plt.ylabel('response time in ms')
plt.title('Response time')
plt.legend(loc='upper right')
plt.grid(True)
plt.savefig("test.png")
plt.show()