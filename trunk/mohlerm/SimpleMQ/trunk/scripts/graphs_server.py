import os
import re
import sys
import statistics
import datetime

import matplotlib
matplotlib.use('Agg')
import matplotlib.pyplot as plt
import numpy as np
import matplotlib.mlab as mlab
from matplotlib.ticker import FuncFormatter
########################################
#
# custom percentage for y label
#
########################################
def to_percent(y, position):
    # Ignore the passed in position. This has the effect of scaling the default
    # tick locations.
    s = str(int(100 * y))

    # The percent symbol needs escaping in latex
    if matplotlib.rcParams['text.usetex'] == True:
        return s + r'$\%$'
    else:
        return s + '%'

########################################
#
# parse inputs and setup variables
#
########################################
if len(sys.argv) != 4:
    print("Please supply clientAmount, experimentId")
    sys.exit(1)

clientAmount = int(sys.argv[1])
serverAmount = int(sys.argv[2])
experimentId = sys.argv[2]
print("Using experimentID="+experimentId)
# import log file
inputfile = open(experimentId+'/allservers.log')
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

currentClients = 0
########################################
#
# parse merged logfile
#
########################################
for line in inputfile:
    found = False
    # always look for deregistration messages
    m = re.search(r"(\d*-\d*-\d*\s\d*:\d*:\d*,\d*)\s*\S*\s*\S*\s*REQ\|DeleteClient\|\d*\|\d*\|\d*\|\d*\|Empty",line)
    if m is not None:
        break
        #currentClients = currentClients-1
    # as long as all clients have not registered look for registration messages and
    # don't include these measurements yet (since we're still in warmup)
    if currentClients < clientAmount:
        m = re.search(r"(\d*-\d*-\d*\s\d*:\d*:\d*,\d*)\s*\S*\s*\S*\s*ANS\|CreateClient\|\d*\|\d*\|\d*\|OK\|\|[-+]?([0-9]*\.[0-9]+|[0-9]+)",line)
        if m is not None:
            currentClients = currentClients+1
            #      print(m.group(2)+" registered")
    else:
        # TODO exclude the acks for 0 messages (where insert did not return a valid message id)
        # If message is a send answer
        m = re.search(r"(\d*-\d*-\d*\s\d*:\d*:\d*,\d*)\s*\S*\s*\S*\s*ANS\|SendMessageToReceiver\|(\d*)\|\d*\|\d*\|\S*\|\|[-+]?([0-9]*\.[0-9]+|[0-9]+)",line)
        if m is not None:
            ans_snd_time.append(m.group(1))
            ans_snd_index.append(m.group(2))
            ans_snd_response.append(float(m.group(3)))
            found = True
        # if message is a peek/pop answer
        if found == False:
            m = re.search(r"(\d*-\d*-\d*\s\d*:\d*:\d*,\d*)\s*\S*\s*\S*\s*ANS\|(Pop|Peek)Queue\|(\d*)\|\d*\|\d*\|(small|medium|large)Message\|\|[-+]?([0-9]*\.[0-9]+|[0-9]+)",line)
            if m is not None:
                ans_rcv_time.append(m.group(1))
                # match group 2 is peek/pop which isn't used currently
                ans_rcv_index.append(m.group(3))
                ans_rcv_response.append(float(m.group(5)))
                found = True
                # if message is a send request
        if found == False:
            m = re.search(r"(\d*-\d*-\d*\s\d*:\d*:\d*,\d*)\s*\S*\s*\S*\s*REQ\|SendMessageToReceiver\|(\d*)\|\d*\|\d*\|\d*\|(small|medium|large)Message",line)
            if m is not None:
                req_snd_time.append(m.group(1))
                req_snd_index.append(m.group(2))
                found = True
        if found == False:
            m = re.search(r"(\d*-\d*-\d*\s\d*:\d*:\d*,\d*)\s*\S*\s*\S*\s*REQ\|(Peek|Pop)Queue\|(\d*)\|\d*\|\d*\|\d*\|null",line)
            if m is not None:
                req_rcv_time.append(m.group(1))
                req_rcv_index.append(m.group(3))
                found = True
                # if found == False:
                #     print(line)

########################################
#
# calculate and print means etc.
#
########################################
mean_ans_snd_response = statistics.mean(ans_snd_response)
mean_ans_rcv_response = statistics.mean(ans_rcv_response)
stdev_ans_snd_response = statistics.stdev(ans_snd_response)
stdev_ans_rcv_response = statistics.stdev(ans_rcv_response)
median_ans_snd_response = statistics.median(ans_snd_response)
median_ans_rcv_response = statistics.median(ans_rcv_response)

print("Mean SEND acks:         " + str(mean_ans_snd_response) + "+-" + str(2*stdev_ans_snd_response))
print("Mean RECEIVE acks:      " + str(mean_ans_rcv_response) + "+-" +str(2*stdev_ans_rcv_response))
print("median SEND acks:       " + str(median_ans_snd_response))
print("median RECEIVE acks:    " + str(median_ans_rcv_response))

ans_snd_miliseconds = []
ans_rcv_miliseconds = []
for s in ans_snd_time:
    ans_snd_miliseconds.append(datetime.datetime.strptime(s,"%Y-%m-%d %H:%M:%S,%f"))
for s in ans_rcv_time:
    ans_rcv_miliseconds.append(datetime.datetime.strptime(s,"%Y-%m-%d %H:%M:%S,%f"))

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


########################################
#
# Plot it!
#
########################################
########################################
#
# RESPONSE TIME (individual)
#
########################################
ans_snd_miliseconds[:] = [(t-startDate).total_seconds() for t in ans_snd_miliseconds]
ans_rcv_miliseconds[:] = [(t-startDate).total_seconds() for t in ans_rcv_miliseconds]
plt.plot(ans_snd_miliseconds, ans_snd_response, 'b.', alpha=0.5, label="SEND (message send) - total: "+str(len(ans_snd_response)))
plt.plot(ans_rcv_miliseconds, ans_rcv_response, 'g.', alpha=0.5, label="RECEIVE (message peak/pop) - total: " +str(len(ans_rcv_response)))
plt.xlabel('Time since start of measurement [in seconds]')
plt.ylabel('Response time [in milliseconds]')
plt.title('Response time')
plt.legend(loc='upper right')
plt.grid(True)
plt.savefig(experimentId+"\experiment_server_"+experimentId+"_response_time.png")
#plt.show()
plt.clf()
########################################
#
# RESPONSE TIME (histogram)
#
########################################
weights1 = np.ones_like(ans_snd_response)/float(len(ans_snd_response))
weights2 = np.ones_like(ans_rcv_response)/float(len(ans_rcv_response))
common_params = dict(bins=50,
                     range=(0, 50),
                     #        normed=True,
                     alpha=0.5,
                     weights=(weights1,weights2),
                     label=("SEND","RECEIVE"))

plt.subplots_adjust(hspace=.4)
#plt.subplot(2, 1, 1)
n, bins, patches = plt.hist((ans_snd_response,ans_rcv_response), **common_params)
y1 = mlab.normpdf(bins, mean_ans_snd_response, stdev_ans_snd_response)
y2 = mlab.normpdf(bins, mean_ans_rcv_response, stdev_ans_rcv_response)
plt.plot(bins, y1, 'b--', label="SEND $\mu=" + str(round(mean_ans_snd_response,2))+"$, $\sigma="+str(round(stdev_ans_snd_response,2))+"$")
plt.plot(bins, y2, 'g--', label="RECEIVE $\mu=" + str(round(mean_ans_rcv_response,2))+"$, $\sigma="+str(round(stdev_ans_rcv_response,2))+"$")
plt.xlabel('Response time [in milliseconds]')
plt.ylabel('Percentage of messages')
plt.xticks(np.linspace(0,50,26))
plt.legend(loc='upper right')
formatter = FuncFormatter(to_percent)
plt.gca().yaxis.set_major_formatter(formatter)
#plt.title(r'Histogram of IQ: $\mu=100$, $\sigma=15$')
plt.savefig(experimentId+"\experiment_server_"+experimentId+"_hist_response_time.png")
plt.clf()
########################################
#
# Throughput over time
#
########################################
timestamps = np.array(range(int(total_seconds)+2))
throughput_values = np.zeros(int(total_seconds)+2)
#print(timestamps)
low_water_mark = 0
for i in range(0,len(ans_snd_miliseconds)):
    if ans_snd_miliseconds[i] < timestamps[low_water_mark+1]:
        throughput_values[low_water_mark] += 1
    else:
        low_water_mark = low_water_mark+1
        throughput_values[low_water_mark] += 1
low_water_mark = 0
for i in range(0,len(ans_rcv_miliseconds)):
    if ans_rcv_miliseconds[i] < timestamps[low_water_mark+1]:
        throughput_values[low_water_mark] += 1
    else:
        low_water_mark = low_water_mark+1
        throughput_values[low_water_mark] += 1
#print(throughput_values)
plt.plot(timestamps[0:len(timestamps)-3],throughput_values[0:len(timestamps)-3], 'b-', label="Throughput over time")
plt.xlabel('Time since start of measurement [in seconds]')
plt.ylabel('Average throughput [in messages/second] - 1s slots')
plt.savefig(experimentId+"\experiment_server_"+experimentId+"_throughput_overtime.png")
plt.clf()
########################################
#
# Response time over time
#
########################################
timestamps = np.array(range(int(total_seconds)+2))
ans_snd_response_time_value = np.zeros(int(total_seconds)+2)
#print(timestamps)
low_water_mark = 0
counter = 0
for i in range(0,len(ans_snd_miliseconds)):
    if ans_snd_miliseconds[i] < timestamps[low_water_mark+1]:
        ans_snd_response_time_value[low_water_mark] += ans_snd_response[i]
        counter += 1
    else:
        if counter > 0:
            ans_snd_response_time_value[low_water_mark] = ans_snd_response_time_value[low_water_mark]/counter
        counter = 1
        low_water_mark = low_water_mark+1
        ans_snd_response_time_value[low_water_mark] += ans_snd_response[i]
ans_rcv_response_time_value = np.zeros(int(total_seconds)+2)
low_water_mark = 0
counter = 0
for i in range(0,len(ans_rcv_miliseconds)):
    if ans_rcv_miliseconds[i] < timestamps[low_water_mark+1]:
        ans_rcv_response_time_value[low_water_mark] += ans_rcv_response[i]
        counter += 1
    else:
        ans_rcv_response_time_value[low_water_mark] = ans_rcv_response_time_value[low_water_mark]/counter
        counter = 1
        low_water_mark = low_water_mark+1
        ans_rcv_response_time_value[low_water_mark] += ans_rcv_response[i]
#print(throughput_values)
plt.plot(timestamps[0:len(timestamps)-3],ans_snd_response_time_value[0:len(timestamps)-3], 'b-', label="SEND")
plt.plot(timestamps[0:len(timestamps)-3],ans_rcv_response_time_value[0:len(timestamps)-3], 'g-', label="RECEIVE")
plt.xlabel('Time since start of measurement [in seconds]')
plt.ylabel('Average response time [in milliseconds] - 1s slots')
plt.legend(loc='upper right')
plt.savefig(experimentId+"\experiment_server_"+experimentId+"_response_time_overtime.png")
