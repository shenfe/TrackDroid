import simplejson as json
import sys
import demjson
import subprocess
import time

logFilePrex = sys.argv[1] + '/presentation/data/' + sys.argv[3]
with open(logFilePrex + '_pipe.log', 'w') as f:
    inputFiles = [logFilePrex + '_input.log', logFilePrex + '_logcat.log', logFilePrex + '_capture.log']
    lineNum = 0
    lineStr = ''
    lineTime = ''
    lineType = ''
    lineValue = ''
    itemTypes = ['activity', 'input', 'method', 'event', 'api', 'para']
    lastActTime = '0000000000000'
    lastApi = 0
    lastApiName = ''
    lastApiTime = 0.0
    lastPara = 0
    paraStack = ''
    lastInputTime = 0.0
    activities = []
    inputs = []
    methods = []
    events = []
    apis = []
    inputTimeStart = 0.0
    activityTimeStart = 0.0
    activityTimeDelay = 0 # important
    logcatTimeStart = 0.0
    timeend = 0.0

    material = {'timestart': '0', 'timeend': '60000', 'series': {'activities': [], 'uievents': [], 'userinputs': [], 'apicalls': []}}

    ff = open(logFilePrex + '_logcat_starttime.log')
    for line in ff:
        if len(line.strip()) <= 0:
            continue
        logcatTimeStart = int(line[4:6]) * 3600 + int(line[6:8]) * 60 + int(line[8:])
        logcatTimeStart = logcatTimeStart * 1000
        print 'logcatTimeStart: ', logcatTimeStart
    for inputFile in inputFiles:
        with open(inputFile, 'r') as ff:
            #for line in iter(lambda: process.stdout.read(1), ''):
            for line in ff:
                lineNum = lineNum + 1
                lineStr = line.strip()
                if lineStr.startswith('mCurrentFocus'):
                    lineType = 'activity'
                    pos = lineStr.find('/')
                    if pos > 0:
                        lineValue = lineStr[(lineStr.rfind(' ', 0, pos) + 1):(lineStr.find(' ', pos, -1))]
                    else:
                        lineValue = 'loading'
                    lineTime = lastActTime
                    if activityTimeStart == 0.0:
                        activityTimeStart = lineTime
                        print 'activityTimeStart: ', activityTimeStart
                        lineTime = 0
                    else:
                        lineTime = lineTime - activityTimeStart
                    material['series']['activities'].append([lineTime, lineValue]) # new activity
                elif len(lineStr) == 13:
                    lastActTime = int(lineStr)
                    continue
                elif (lineStr.find('/dev/input/event') > 0) and (lineStr.find('add device') < 0):
                    lineType = 'input'
                    lineTime = float(lineStr[(lineStr.find('[') + 1):(lineStr.find(']'))].strip()) * 1000
                    if inputTimeStart == 0.0:
                        inputTimeStart = lineTime
                        print 'inputTimeStart: ', inputTimeStart
                        lineTime = 0
                    else:
                        lineTime = lineTime - inputTimeStart
                    inputs.append(lineTime)
                    lineValue = lineStr[(lineStr.find(':') + 1):].strip()
                    material['series']['userinputs'].append([lineTime, lineValue]) # new userinput
                elif lineStr.find('V/trackdroid') > 0:
                    lineTimeParts = lineStr[0:(lineStr.find('V/trackdroid'))].strip().split(' ')[1].split(':')
                    lineTime = int(lineTimeParts[0]) * 3600000 + int(lineTimeParts[1]) * 60000 + float(lineTimeParts[2]) * 1000
                    lineTime = lineTime - logcatTimeStart
                    lineTime = lineTime + activityTimeDelay
                    lineValue = ''
                    if lineStr.find('Call Method:') > 0:
                        if lastApi == 1:
                            if lastApiTime >= 0:
                                material['series']['apicalls'].append([lastApiTime, lastApiName, paraStack]) # new apicall
                            paraStack = ''
                            lastPara = 0
                            lastApi = 0
                        lineType = 'method'
                        lineValue = lineStr[(lineStr.find('Call Method:') + 13):]
                        if lineTime >= 0:
                            material['series']['apicalls'].append([lineTime, lineValue]) # new methodcall
                    elif lineStr.find('Call Event:') > 0:
                        if lastApi == 1:
                            if lastApiTime >= 0:
                                material['series']['apicalls'].append([lastApiTime, lastApiName, paraStack]) # new apicall
                            paraStack = ''
                            lastPara = 0
                            lastApi = 0
                        lineType = 'event'
                        lineValue = lineStr[(lineStr.find('Call Event:') + 12):]
                        if lineTime >= 0:
                            material['series']['uievents'].append([lineTime, lineValue]) # new uievent
                    elif lineStr.find('Call Api:') > 0:
                        if lastApi == 1:
                            if lastApiTime >= 0:
                                material['series']['apicalls'].append([lastApiTime, lastApiName, paraStack]) # new apicall
                            paraStack = ''
                            lastPara = 0
                        lastApi = 1
                        lineType = 'api'
                        lineValue = lineStr[(lineStr.find('Call Api:') + 10):]
                        lastApiName = lineValue
                        lastApiTime = lineTime
                    else:
                        lineType = 'para'
                        lineValue = lineStr[(lineStr.find('):') + 2):]
                        if len(paraStack) == 0:
                            paraStack = lineValue
                        else:
                            paraStack = paraStack + ', ' + lineValue
                        lastPara = 1
                        lastApi = 1
                else:
                    continue
                if lineTime >= 0:
                    if lineTime > timeend:
                        timeend = lineTime
                    lineStr = str(lineTime) + ', ' + lineType + ', ' + lineValue
                    #print (lineStr)
                    f.write(lineStr+'\n')
                else:
                    print 'lineTime < 0: ', lineTime, lineType, lineValue
            ff.close()
    f.close()
    material['timeend'] = str(timeend)
    content = ('var material = ' + json.dumps(material, indent=4))#demjson.encode(material))#.encode('utf-8')
    print 'now write into js files.'
    f = open(sys.argv[1] + '/presentation/data.js', 'w')
    f.write(content)
    f.close()
    f = open(sys.argv[1] + '/presentation/data_' + sys.argv[3] + '_' + time.strftime('%Y%m%d%H%M%S', time.localtime(time.time())) + '.js', 'w')
    f.write(content)
    f.close()
    subprocess.call(["xdg-open", sys.argv[1] + '/presentation/index.html'])
    #exit()