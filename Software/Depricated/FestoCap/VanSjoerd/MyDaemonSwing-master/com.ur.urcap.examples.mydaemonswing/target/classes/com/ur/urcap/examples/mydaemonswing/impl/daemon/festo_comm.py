# This program starts connection to the Festo drive and send and receives messages
# Its based on the example received by Festo, see _example_from_festo.py

import threading
from pyModbusTCP.client import ModbusClient
import main_shared_vars as var       # variables which are var between files
from main_functions import *         # contains general functions


def start_festo_comm(festo_ip):
	while True:
		try:
			var.d = ModbusClient(host=festo_ip, port=502, auto_open=True)
			global controlMsg
			global statusMsg
			controlMsg = [0, 0, 0, 0]  # start empty
			statusMsg = [0, 0, 0, 0]  # start empty

			# Create new thread
			print(get_time() + "Festo: Starting Modbus Poll")
			thread = myThread(1, "Thread-1", 1)

			# Start new Threads
			thread.start()

			while thread.isAlive():
				if var.requested_enable:
					enable()
				if var.requested_home:
					home()
				if var.requested_reset:
					reset()
				if var.requested_position:
					position()
				if var.requested_timeout and var.requested_timeout_parameters != -1:
					timeout(var.requested_timeout_parameters)
					var.requested_timeout_parameters = -1
				if var.requested_speed and var.requested_speed_parameters != -1:
					speed(var.requested_speed_parameters)
					var.requested_speed_parameters = -1
				if var.requested_move and var.requested_move_parameters != -1:
					move(var.requested_move_parameters)
					var.requested_move_parameters = -1
				time.sleep(var.main_cycle_time)

			# When thread is dead, close the modbusclient connection
			var.d.close()

		except:
			var.d.close()
			print("Festo main loop: Except activated")
			var.amount_of_errors = 10  # this will exit the thread
			time.sleep(0.1)


def readModbus():
	global statusMsg
	regs = var.d.read_holding_registers(0, 4)
	if not regs:
		var.amount_of_errors += 1
		print(get_time() + "Festo: read error, retry " + str(var.amount_of_errors))
		regs = [0, 0, 0, 0]
	else:
		var.amount_of_errors = 0
	statusMsg = regs
	return regs


def writeModbus():
	global controlMsg
	regs = var.d.write_multiple_registers(0, controlMsg)
	if not regs:
		var.amount_of_errors += 1
		print(get_time() + "Festo: write error, retry " + str(var.amount_of_errors))
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
	word1 = modBusWordToByte(fromBus[2])
	word2 = modBusWordToByte(fromBus[3])
	# getal 32 bits, 4 bytes to int
	return (word1[0] << 24) + (word1[1] << 16) + (word2[0] << 8) + (word2[1])


def makeModbusPosition(pos):
	# getal 32 bits, 4 bytes
	bytes = [(pos >> i & 255) for i in (24, 16, 8, 0)]
	# 4 bytes, 2 words (toBus)
	return [((bytes[0] << 8) + bytes[1]), ((bytes[2] << 8) + bytes[3])]


def home():
	# Reset trigger
	var.requested_home = False
	print(get_time() + "Festo: Home function started")
	# home controller
	status = sendRecieveModbus([773, 0, 0, 0])
	# Wait for the motion complete state from festo or wait for a timeout
	var.timeout = var.default_timeout
	while (var.timeout > 0) and (status[0] & 128 != 128):
		time.sleep(0.1)
		var.timeout = var.timeout - 0.1
		status = statusMsg  # get new status
	if var.timeout == 0:
		var.answer_back = "Timeout"
	else:
		var.answer_back = "Succeeded"
	print(get_time() + "Festo: Home function finished")
	return status


def reset():
	# Reset trigger
	var.requested_reset = False
	print(get_time() + "Festo: Reset function started")
	# Reset controller if fault detected
	status = sendRecieveModbus([2561, 0, 0, 0])
	# Wait for the healthy state from festo or wait for a timeout
	var.timeout = var.default_timeout
	while (var.timeout > 0) and (status[0] & 4096 != 4096):
		time.sleep(0.1)
		var.timeout = var.timeout - 0.1
		sendRecieveModbus([2561, 0, 0, 0])
		status = statusMsg  # get new status
	if var.timeout == 0:
		var.answer_back = "Timeout"
	else:
		var.answer_back = "Succeeded"
	print(get_time() + "Festo: Reset function finished")


def enable():
	# Reset trigger
	var.requested_enable = False
	print(get_time() + "Festo: Enable function started")
	# enable controller
	status = sendRecieveModbus([769, 0, 0, 0])
	# Wait for the enabled state from festo or wait for a timeout
	var.timeout = var.default_timeout
	while (var.timeout > 0) and (status[0] & 256 != 256):
		time.sleep(0.1)
		var.timeout = var.timeout - 0.1
		status = statusMsg  # get new status
		print("dit is de status: " + str(status))  ## sjoerd: remove this line
	if var.timeout == 0:
		var.answer_back = "Timeout"
	else:
		var.answer_back = "Succeeded"
	print(get_time() + "Festo: Enable function finished")


def position():
	# Reset trigger
	var.requested_position = False
	print(get_time() + "Festo: Position function started")
	# Read new status
	status = readModbus()
	var.answer_back = "[" + str(getModbusPosition(status)) + "]"
	print(get_time() + "Festo: Position function finished, this is the position: " + var.answer_back)


def speed(rotation_speed):
	# Reset trigger
	var.requested_speed = False
	print(get_time() + "Festo: Speed function started")
	# Set new speed
	var.rotation_speed = rotation_speed
	# Return
	var.answer_back = "Succeeded"
	print(get_time() + "Festo: Speed function finished, this is the new speed: " + str(rotation_speed))


def timeout(MyTimeout):
	# Reset trigger
	var.requested_timeout = False
	print(get_time() + "Festo: Timeout function started")
	# Set new speed
	var.default_timeout = MyTimeout
	# Return
	var.answer_back = "Succeeded"
	print(get_time() + "Festo: Timeout function finished, this is the new Timeout: " + str(MyTimeout))


def move(pos):
	# Reset trigger
	var.requested_move = False
	var.requested_move_parameters = 0.0
	print(get_time() + "Festo: Move function started")
	# Set rotation speed
	vel = var.rotation_speed
	# Set positions
	dst = makeModbusPosition(pos) 
	sendRecieveModbus([17153, vel, dst[0], dst[1]])  # Position
	time.sleep(0.3)  # wait for package being processed
	status = sendRecieveModbus([17155, vel, dst[0], dst[1]])  # Start
	time.sleep(0.3)	 # wait for package being processed
	# Wait for the acknowledge from festo or wait for a timeout
	var.timeout = var.default_timeout
	while (var.timeout > 0) and (status[0] & 2 != 2):
		time.sleep(0.1)
		var.timeout = var.timeout - 0.1
		status = statusMsg  # get new status
	if var.timeout == 0:
		var.answer_back = "Timeout"
		print(get_time() + "Festo: Move function finished")
		return
	print(get_time() + "Festo: Acknowledge received")
	# Send move command to Festo
	status = sendRecieveModbus([17153, vel, dst[0], dst[1]])  # Remove Start
	time.sleep(0.3)	 # wait for package being processed
	# Wait for the motion complete state from festo or wait for a timeout
	var.timeout = var.default_timeout
	while (var.timeout > 0) and (status[0] & 4 != 4):
		time.sleep(0.1)
		var.timeout = var.timeout - 0.1
		status = statusMsg  # get new status
	if var.timeout == 0:
		var.answer_back = "Timeout"
	else:
		var.answer_back = "Succeeded"
	print(get_time() + "Festo: Move function finished")


class myThread (threading.Thread):
	def __init__(self, threadID, name, q):
		threading.Thread.__init__(self)
		self.threadID = threadID
		self.name = name
		self.q = q
		# TCP auto connect on first modbus request

	def run(self):
		print(get_time() + "Festo: Starting " + self.name)
		while 1:
			writeModbus()
			time.sleep(0.1)
			readModbus()
			time.sleep(0.1)
			if var.amount_of_errors > 9:
				print("ending thread")
				var.amount_of_errors = 0
				exit()

