package com.gibas.javamodbus;

import java.io.IOException;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

import de.re.easymodbus.exceptions.ModbusException;
import de.re.easymodbus.modbusclient.*;

public class App {
	static String host = "172.16.0.20";
	static String testhost = "192.168.8.76";
	static int port = 502;
	static int testport = 502;
	static ModbusClient modbusClient = new ModbusClient(host, port);
	static boolean dorun = true;
	static int controlMsg[] = { 0, 0, 0, 0 };
	static int statusMsg[] = { 0, 0, 0, 0 };
	static int bufferMsg[] = statusMsg;
	static int controlMsgCheck[] = controlMsg;

	/**
	 * Main loop that connects to a Modbus Slave.
	 * Also starts a separate thread that loops
	 * reads and writes to the slave.
	 * @param args
	 * @throws InterruptedException
	 * @throws UnknownHostException
	 * @throws SocketException
	 * @throws ModbusException
	 * @throws IOException
	 */
	public static void main(String[] args)
			throws InterruptedException, UnknownHostException, SocketException, ModbusException, IOException {

		try {
			System.out.println("Connecting to Slav");
			modbusClient.Connect();
		} catch (Exception e) {
			System.out.println(e);
		}
		System.out.println("Connection succesful");
		/**
		 * Starts communication update thread, 
		 * messages will be set on the @param statusMsg and @param controlMsg 
		 * for @param thread to write it to the Modbus connection.
		 */
		Thread thread = new Thread() {
			public void run() {
				System.out.println("Starting thread");
				while (dorun) {
					try {
						writeModbus();
					} catch (ModbusException | IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					try {
						Thread.sleep(300);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					try {
						readModbus();
					} catch (ModbusException | IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					try {
						Thread.sleep(300);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

				}
				System.out.println("Exiting thread");
			};

		};

		thread.start();
		System.out.println("Initializing Modbus");
		System.out.println("Initializing Drive");
		TimeUnit.SECONDS.sleep(2);
		enable();
		homing();
		move(100, 300);
		move(100, 1000);
		System.out.println("Initializing succesfull");
		while ((statusMsg[0] & 256) == 256) {
			Scanner reader = new Scanner(System.in);
			System.out.println("Enable	= 1 ");
			System.out.println("Home 	= 2 ");
			System.out.println("getPos	= 3 ");
			System.out.println("Move 	= 4 ");
			System.out.println("Enter a command : ");
			int n = reader.nextInt();
//			reader.close();
			switch (n) {
			case 1:
				enable();
				break;
			case 2:
				homing();
				break;
			case 3:
				System.out.println(getModbusPosition(readModbus()));
				break;
			case 4:
				System.out.println("Enter a position : ");
				reader = new Scanner(System.in);
				int pos = reader.nextInt();
				move(100, pos);
				System.out.println("Motion Complete");
				break;
			}
		}

	}
/**
 * Reads Holding Registers 0-4 and puts them in @param regs[] an integer array.
 * Updates the @param statusMsg to the received msg.
 * @return
 * @throws UnknownHostException
 * @throws SocketException
 * @throws ModbusException
 * @throws IOException
 */
	public static int[] readModbus() throws UnknownHostException, SocketException, ModbusException, IOException {
		int regs[] = modbusClient.ReadHoldingRegisters(0, 4);

		if (bufferMsg == statusMsg) {
			System.out.println("\nReading from " + host + ": " + Arrays.toString(regs));
		}

		bufferMsg = statusMsg;
		statusMsg = regs;

		return regs;
	}

	public static int[] writeModbus() throws UnknownHostException, SocketException, ModbusException, IOException {
		modbusClient.WriteMultipleRegisters(0, controlMsg);
		if (Arrays.toString(controlMsgCheck) == Arrays.toString(controlMsg)) {
			System.out.println("\nWriting from " + host + ": " + Arrays.toString(controlMsg));

		}

		controlMsgCheck = controlMsg;
		return controlMsg;
	}

	public static int[] sendRecieveModbus(int[] content) {
		controlMsg = content;

		return statusMsg;
	}

	public static int twoRegistersToInt(int[] registers) {
		int[] tempRegs = { registers[1], registers[0] };
		int i = ModbusClient.ConvertRegistersToDouble(tempRegs);

		return i;

	}

	public static int[] intToTwoRegisters(int i) {
		int[] tempRegs = ModbusClient.ConvertDoubleToTwoRegisters(i);
		int[] regs = { tempRegs[1], tempRegs[0] };

		return regs;
	}

	public static int getModbusPosition(int[] fromBus)
			throws UnknownHostException, SocketException, ModbusException, IOException {

		return twoRegistersToInt(modbusClient.ReadHoldingRegisters(2, 2));
	}

	public static int[] homing() throws InterruptedException {
		System.out.println("Homing System");
		int msg[] = { 773, 0, 0, 0 };
		int[] status = sendRecieveModbus(msg);

		while ((status[0] & 128) != 128) {
			status = statusMsg; // get new status message
			TimeUnit.MILLISECONDS.sleep(300);
		}
		System.out.println("System Homed");

		return status;
	}

	public static int[] enable() throws InterruptedException {
		// reset controller if fault detected
		System.out.println("Enabling Systems");
		int resetMsg[] = { 2561, 0, 0, 0 };
		while ((statusMsg[0] & 4096) != 4096) {

			sendRecieveModbus(resetMsg);
			TimeUnit.MILLISECONDS.sleep(30);
		}
		int enableMsg[] = { 769, 0, 0, 0 };
		// enable controller
		int[] status = sendRecieveModbus(enableMsg);

		while ((status[0] & 256) != 256) { // " Dit kan wel wat minder gay " - luke
//  		Debug stuff
//			System.out.println((status[0] & 256));
//			System.out.println((status[0]));
//			System.out.println((status[0] & 256) != 256);

			status = statusMsg; // get new status message
			TimeUnit.MILLISECONDS.sleep(30);
		}
		System.out.println("System Enable");
		return status;
	}

	public static int[] move(int vel, int pos) throws InterruptedException {
		System.out.println("Starting Motion");
		int destination[] = intToTwoRegisters(pos);
		int moveMsg[] = { 17153, vel, destination[0], destination[1] }; // Position
		sendRecieveModbus(moveMsg);
		TimeUnit.MILLISECONDS.sleep(30);
		int startMsg[] = { 17155, vel, destination[0], destination[1] }; // Start
		int status[] = sendRecieveModbus(startMsg);
		TimeUnit.SECONDS.sleep(1);
		while ((status[0] & 2) != 2) { // wait for start Ack
			status = statusMsg; // get new status message
			TimeUnit.MILLISECONDS.sleep(30);
		}
		System.out.println("Ack Recieved");
		status = sendRecieveModbus(moveMsg); // Remove Start
		TimeUnit.SECONDS.sleep(1);
		// wait for motion complete
		while ((status[0] & 4) != 4) {
			status = statusMsg; // get new status
			TimeUnit.MILLISECONDS.sleep(30);
		}
		System.out.println("Motion Complete");
		return status;

	}

}