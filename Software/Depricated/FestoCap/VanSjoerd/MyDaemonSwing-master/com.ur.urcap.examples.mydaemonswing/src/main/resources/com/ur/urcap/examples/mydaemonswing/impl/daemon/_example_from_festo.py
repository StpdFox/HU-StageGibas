import threading
import socket
import time
from pyModbusTCP.client import ModbusClient

d = ModbusClient(host="172.168.0.30", port=502, auto_open=True)
controlMsg = [0,0,0,0] # start empty
statusMsg=[0,0,0,0] # start empty

def readModbus():
	global statusMsg
	regs = d.read_holding_registers(0, 4)
	if not regs:
		print("read error")
		regs = [0,0,0,0]
		
	statusMsg = regs
	return regs

def writeModbus():
	global controlMsg
	regs = d.write_multiple_registers(0, controlMsg)
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
	return [(word >> i & 255) for i in (8,0)]
	
def getModbusPosition(fromBus):
	# getal 32 bits, 2 words (fromBus) to 4 bytes
	print( fromBus[2])
	print( fromBus[3])
	word1 = modBusWordToByte(fromBus[2])
	word2 = modBusWordToByte(fromBus[3])
	# getal 32 bits, 4 bytes to int
	return ((word1[0] << 24) + (word1[1] << 16)  + (word2[0] << 8)+ (word2[1]))

def makeModbusPosition(pos):
	# getal 32 bits, 4 bytes
	bytes=[(pos >> i & 255) for i in (24,16,8,0)]
	# 4 bytes, 2 words (toBus)
	return [((bytes[0] << 8) + bytes[1]),((bytes[2] << 8) + bytes[3])]

def homing():
	# home controller
	status = sendRecieveModbus([773,0,0,0])
	# wait for being homed // not implemented
	while( status[0] & 128 != 128):
		status = statusMsg # get new status
	print ("System Homed")
	return status

def enable():
	# reset controller if fault detected
	while( statusMsg[0] & 4096 != 4096):
		sendRecieveModbus([2561,0,0,0])
	# enable controller
	status = sendRecieveModbus([769,0,0,0])
	while( status[0] & 256 != 256): # wait until enabled 
		status = statusMsg # get new status
	print ("System Enabled")
	return status

def move(vel, pos):
	dst = makeModbusPosition(pos) 
	sendRecieveModbus([17153,vel,dst[0],dst[1]])# Position
	time.sleep(0.3) # wait for package being processed
	status = sendRecieveModbus([17155,vel,dst[0],dst[1]])# Start
	time.sleep(0.3) # wait for package being processed
	while( status[0] & 2 != 2):# wait for start Ack 
		status = statusMsg # get new status
	print ("Ack Recieved")
	status = sendRecieveModbus([17153,vel,dst[0],dst[1]])# Remove Start
	time.sleep(0.3) # wait for package being processed
	# Wait for motion complete 
	while( status[0] & 4 != 4):
		status = statusMsg # get new status
	print ("Motion Complete")
	return status
	
class myThread (threading.Thread):
	def __init__(self, threadID, name, q):
		threading.Thread.__init__(self)
		self.threadID = threadID
		self.name = name
		self.q = q
		# TCP auto connect on first modbus request
	def run(self):
		print( "Starting " + self.name)
		while dorun:
			writeModbus()
			time.sleep(0.1)
			readModbus()
			time.sleep(0.1)
		print ("Exiting " + self.name)

print("Starting Modbus Poll")
# Create new threads
thread = myThread(1, "Thread-1", 1)

# Start new Threads
dorun = True # keep while loop active
thread.start()

print( "Starting Program")
print("Initializing Modbus")	

print ("Initializing Drive")
status = enable() # Enable drive
status = homing() # Start homing
status = move(100,300) # move with 100% from the setpoint value direct mode to position 30000
status = move(100,1000) # move with 100% from the setpoint value direct mode to position 1000


print ("Connection Accepted, running while enabled" )

while ( (statusMsg[0] & 256) == 256 ): # while drive enabled?
		msg = input("Give command(Enable ,Home ,getPos , Move ) > ")
		print ("Request = ", msg)
		if msg == "Enable":
			enable()
		if msg == "Home":
			homing()
		if msg == "getPos":
			status = readModbus() # get new status
			print (status)
			festo_position= getModbusPosition(status)
			print (festo_position)# send position
		if msg == "Move":
			print ("move modus"); # convert to string?
			msg = input("Give position > ")
			#conn.send("pos recieved"); # convert to string?
			status = move(100,int(msg))
			print ("Motion Complete") # if motion complete?
		print ("")
	
print ("drive not enabled, exiting")
conn.close()
s.close()
print ("Program finished")
print ("Closing polling Thread")
dorun = False # disabling polling thread loop
thread.join() # wait for polling thread to exit
