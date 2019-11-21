#!/usr/bin/env python
import struct
import socket
import math
import sys
import os
import threading
import time
#import win_inet_pton
from Queue import Queue
import xmlrpclib
from SimpleXMLRPCServer import SimpleXMLRPCServer
# from SimpleXMLRPCServer import SimpleXMLRPCRequestHandler

from client import ModbusClient
from threading import Thread
import logging

delaytimer = 0.5
cur_Done = "0"


def readModbus():
    global statusMsg
    regs = d.read_holding_registers(0, 4)
    # if regs:
    #       print "read ",regs
    if not regs:
        print("read error")
        regs = [0, 0, 0, 0]
    statusMsg = regs
    return regs


def writeModbus():
    global controlMsg
    regs = d.write_multiple_registers(0, controlMsg)
    # if regs:
    #       print "write ",controlMsg
    if not regs:
        print("write error")
    return controlMsg


def sendRecieveModbus(content):
    global controlMsg
    controlMsg = content
    return statusMsg


def byteToModbusWord(bytes):
    return ((bytes[0] << 8) + bytes[1])


def modBusWordToByte(word):
    return [(word >> i & 255) for i in (8, 0)]


def int32(x):
    if x > 0xFFFFFFFF:
        raise OverflowError
    if x > 0x7FFFFFFF:
        x = int(0x100000000 - x)
        if x < 2147483648:
            return -x
        else:
            return -2147483648
    return x


def getModbusPosition(fromBus):
    # getal 32 bits, 2 words (fromBus) naar 4 bytes
    word1 = modBusWordToByte(fromBus[2])
    word2 = modBusWordToByte(fromBus[3])
    #print "word1:" + str(word1)
    #print "word2:" + str(word2)
    # getal 32 bits, 4 bytes naar int
    unsinged_int = ((word1[0] << 24) + (word1[1] << 16) + (word2[0] << 8) + (word2[1]))
    signed_int = int32(unsinged_int)
    return signed_int


def makeModbusPosition(pos):
    # getal 32 bits, 4 bytes
    bytes = [(pos >> i & 255) for i in (24, 16, 8, 0)]
    # 4 bytes, 2 words (toBus)
    return [((bytes[0] << 8) + bytes[1]), ((bytes[2] << 8) + bytes[3])]


def homing():
    # home controller
    status = sendRecieveModbus([773, 0, 0, 0])
    time.sleep(2)
    print "wait for homing"
    # wait for being homed // not implemented
    while (status[0] & 4 != 4):
        status = statusMsg  # get new status
    print "System Homed"
    time.sleep(3)
    return status


def enable():
    # reset controller
    sendRecieveModbus([2561, 0, 0, 0])
    time.sleep(delaytimer)
    # enable controller
    status = sendRecieveModbus([769, 0, 0, 0])
    while (status[0] & 256 != 256):
        status = statusMsg  # get new status
    print "System Enabled"
    return status


def move(vel, pos):
    global cur_Done
    cur_Done = "0"
    print cur_Done
    dst = makeModbusPosition(pos)
    time.sleep(0.1)
    sendRecieveModbus([17153, vel, dst[0], dst[1]])  # Position
    print "disabled Start"
    time.sleep(0.1)
    status = sendRecieveModbus([17155, vel, dst[0], dst[1]])  # Start
    print "enabled Start"
    time.sleep(0.1)
    while (status[0] & 2 != 2):  # wait for start Ack
        status = statusMsg  # get new status
    print "Ack Recieved"
    status = sendRecieveModbus([17153, vel, dst[0], dst[1]])  # Remove Start
    print "disabled Start"
    # Wait for motion complete
    time.sleep(0.1)
    while (status[0] & 4 != 4):
        status = statusMsg  # get new status
    print "Motion Complete"
    cur_Done = "1"

    return status


title = ""
cur_Vel = ""
cur_Pos = ""
cur_En = ""
cur_Home = ""
last_cur_Home = ""
last_cur_Vel = ""
last_cur_Pos = ""
last_cur_En = ""
actual_position = ""


def set_title(new_title):
    global title
    title = new_title
    return title


def get_title():
    tmp = ""
    if str(title):
        tmp = title
    else:
        tmp = "No title set"
    return tmp


def get_actual_pos():
    global actual_position
    tmp = ""
    if str(actual_position):
        tmp = actual_position
    else:
        tmp = "0"
    return tmp


def get_Done():
    global cur_Done
    tmp = ""
    if str(cur_Done):
        tmp = cur_Done
    else:
        tmp = "0"
    return tmp


def get_message_vel(Vel):
    global cur_Vel
    global cur_Done
    if str(Vel):
        cur_Vel = Vel

        # logging.info(str(name))
        return str(cur_Vel)  # original vel
    else:
        return "0"


def get_message_pos(pos):
    global cur_Pos
    if str(pos):
        cur_Pos = pos
        # logging.info(str(name))
        return str(pos)
    else:
        return "0"


def get_pos():
    global cur_Pos
    return cur_Pos


def get_Vel():
    global cur_Vel
    return cur_Vel


def get_message_enable(enable):
    global cur_En
    if str(enable):
        cur_En = enable
        # logging.info(str(name))
        return str(enable)
    else:
        return "0"


def get_message_homing(homing):
    global cur_Home
    if str(homing):
        cur_Home = homing
        # logging.info(str(name))
        return str(homing)
    else:
        return "No Home set"


def thread1(threadname, a):
    print("xmlrpc connecto start shit")
    sys.stdout.write("MyDaemon daemon started")
    sys.stderr.write("MyDaemon daemon started")
    server = SimpleXMLRPCServer(("127.0.0.1", 40404))
    server.register_function(set_title, "set_title")
    server.register_function(get_title, "get_title")
    server.register_function(get_message_vel, "get_message_vel")
    server.register_function(get_message_pos, "get_message_pos")
    server.register_function(get_message_enable, "get_message_enable")
    server.register_function(get_message_homing, "get_message_homing")
    server.register_function(get_actual_pos, "get_actual_pos")
    server.register_function(get_Done, "get_Done")
    server.serve_forever()


queue = Queue()
thread1 = Thread(target=thread1, args=("Thread-1", queue))
thread1.start()

doloop = True

while doloop:
    time.sleep(0.1)
    try:
        socket.inet_aton(get_title())
        # print "legaal ip adress" + get_title()
        doloop = False
    except socket.error:
        print "Voer IP adress in" + get_title()

d = ModbusClient(host=get_title(), port=502, auto_open=True)
controlMsg = [0, 0, 0, 0]  # start empty
statusMsg = [0, 0, 0, 0]  # start empty


class myThread(threading.Thread):
    def __init__(self, threadID, name, q):
        threading.Thread.__init__(self)
        self.threadID = threadID
        self.name = name
        self.q = q

    # TCP auto connect on first modbus request
    def run(self):
        print "Starting " + self.name
        while dorun:
            writeModbus()
            time.sleep(0.1)
            readModbus()
            time.sleep(0.1)
        print "Exiting " + self.name


print("Starting Modbus Poll")
# Create new threads
thread = myThread(1, "Thread-2", 1)

# Start new Threads
dorun = True  # keep while loop active
thread.start()

start = 0


def thread3(threadname3, message):
    print "Starting Program"

    print "enabling"

    status = enable()
    status = homing()
    time.sleep(2)
    testje = -1000
    factor = 8738.1270698438496
    verplaatsing = (int(testje) * factor)
    move(100, int(verplaatsing))
    global cur_En
    global cur_Pos
    global cur_Vel
    global cur_Done
    global cur_Home
    global last_cur_Home
    global last_cur_Vel
    global last_cur_Pos
    global last_cur_En

    cur_Vel = "0"
    cur_Pos = "0"
    cur_En = "0"
    cur_Home = "0"

    while True:  # while drive enabled?
        time.sleep(0.1)
        # print get_Vel()
        # print get_pos()
        local_vel = int(get_Vel())
        factor = 8738.1270698438496
        local_pos = int((int(get_pos()) * factor))

        # print cur_Pos
        # print cur_Pos
        if last_cur_Home == 0:
            if int(cur_Home) == 1:
                homing()

        if int(cur_Vel) != 0:
            if int(local_pos) != last_cur_Pos:
                # print "im over here Pos"
                move(int(local_vel), int(local_pos))

        #    time.sleep(0.1)
        #    s.send(cur_Pos)

        last_cur_Vel = int(local_vel)
        last_cur_Pos = int(local_pos)
        last_cur_Home = int(cur_Home)


def thread4(threadname3, message):
    while True:
        time.sleep(0.2)
        global statusMsg
        global actual_position
        position = getModbusPosition(statusMsg)
        factor = 8738.1270698438496
        ref_pos = position / factor
        print "Position:" + str(int(round(ref_pos)))
        actual_position = str(int(round(ref_pos)))


thread3 = Thread(target=thread3, args=("Thread-3", queue))
thread4 = Thread(target=thread4, args=("Thread-4", queue))
thread3.start()
thread4.start()
thread1.join()
thread3.join()
thread4.join()