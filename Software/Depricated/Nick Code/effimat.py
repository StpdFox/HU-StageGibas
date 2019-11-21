from threading import Thread, Lock
import queue
import time
import socket
import sqlite3
import helper_functions
import warehouse_loc
import order as order_adt


class Effimat(Thread):
    """
    Effimat is a class that implements functionality for controlling the Effimat robot

    """
    # Class objects; shared between every instance of Effimat

    # Dicts containing readable statuses for the Effimat; used like an enum
    status_list = {
        0: "Ready",
        1: "Idle",
        10: "Busy",
        100: "Done",
        200: "Alarm",
        201: "Resetting alarm",
        202: "Alarm reset, ready for continue",
        203: "Continuing",
        300: "Automatic Mode",
        301: "Manual Mode",
    }

    function_status_list = {
        1: "Command received",
        100: "Movement finished",
        200: "Movement error",
    }

    # Variable that is used for updating the database, to decrease the amount of magic numbers
    no_carrier = -1

    # threading.Lock object to ensure that the database is never accessed by more than one Effimat at a time
    db_lock = Lock()

    def __init__(self, tower_ID, effimat_ip, effimat_port):
        """
        Initialiser for Effimat class

        :param tower_ID: 	    string containing tower number
        :param effimat_ip: 		string containing IP address
        :param effimat_port: 	integer containing port the socket is located
        """

        # Variables that set up the communication with the Effimat
        self.tower = tower_ID
        self.ip = effimat_ip
        self.port = effimat_port

        # Initialise Thread
        Thread.__init__(self)

        # Set up socket + lock on socket the make sure the socket is never accessed by two instances at the same time
        self.socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        self.socket_lock = Lock()

        # Internal variables representing the actual status of the Effimat
        self.status = "Not initialized"
        self.function_status = "Not initialized"

        # Internal variable denoting whether or not the Effimat may receive an order or not
        self.ready_to_receive_order = False

        # Order queue of size 1, the PTS adds an order to this queue. The Effimat may never receive more than 1 order
        # at any given time
        self.order_queue = queue.Queue(maxsize=1)

        # Variables used to store the messages received from the PLC
        self.answer = ""
        self.previous_answer = ""

    def __hash__(self):
        """
        Hash function
        Necessary to use an object of type Effimat as key for a dict

        :return: Hashed integer of the tower number and the port
        """
        return hash((self.tower, self.port))

    def __eq__(self, other):
        """
        Equals function which checks whether two Effimat objects are equal, using the tower number and port
        Necessary to use an object of type Effimat as key for a dict

        :param other: A different object of type Effimat

        :return: Boolean representing whether or not self and other are equal
        """
        return (self.tower, self.port) == (other.number, other.port)

    def __repr__(self):
        """
        Repr functionality, prints identifiable parts of the Effimat object it is called upon

        :return: string containing identifiable parts of the object
        """
        return "Effimat(tower_id=" + str(self.tower) + ")"

    def run(self):
        """
        TASK: Sets up the socket connection to the Effimat and listens to incoming messages
        This is the function called through the start() method in the main/calling class
        Consider this the main of the thread

        :return:
        """
        # Open socket
        # global s
        self.socket.connect((self.ip, self.port))
        print(helper_functions.get_time() + "Effimat: Listening to PLC")

        # Set the initial status of the Effimat, to ensure it is functioning
        self.set_initial_status()

        # Keep listening to incoming messages from effimat, and send a message when one is available
        while True:
            self.update_status()
            # Wait for answer
            # self.receive_message()

            # If queue is empty: skip and try again
            if self.order_queue.empty():
                time.sleep(1)
                continue

            # Else: retrieve order from queue, and execute it
            order = self.order_queue.get()
            print(
                order.order_type + "\t" + str(order.carrier_id) + "\t" + order.location
            )
            if order.order_type == "retrieve":
                self.retrieve(order.carrier_id, order.location)
            elif order.order_type == "store":
                self.store(order.carrier_id, order.location)
            else:
                print("Error..?")
            # self.update_status()
            time.sleep(1)

    # Public functions

    def retrieve_order(self, carrier_id, destination):
        """
        Interface method that calls the necessary member functions to add a retrieve order to the queue

        :param carrier_id:  Integer representing the ID of the carrier (equal to database entry)
        :param destination: String representing the name of the exit point for the carrier

        :return: Boolean to represent whether or not placing the order succeeded
        """
        if self.order_queue.empty():
            order = order_adt.Order("retrieve", carrier_id, destination)
            self.order_queue.put(order)
            self.ready_to_receive_order = False
            return True
        return False

    def store_order(self, carrier_id, pickup_location):
        """
        Interface method that calls the necessary member functions to add a store order to the queue

        :param carrier_id:  Integer representing the ID of the carrier (equal to database entry)
        :param pickup_location: String representing the name of the entry point for the carrier

        :return: Boolean to represent whether or not placing the order succeeded
        """
        if self.order_queue.empty():
            order = order_adt.Order("store", carrier_id, pickup_location)
            self.order_queue.put(order)
            self.ready_to_receive_order = False
            return True
        return False

    def is_ready_to_receive_order(self):
        """
        Interface method that returns whether or not the Effimat is ready to receive an order

        :return: self.ready_to_receive_order: Boolean representing the above
        """
        return self.ready_to_receive_order

    def get_status(self):
        """
        Interface which returns self.status

        :return: String that is stored in self.status
        """
        return self.status

    def get_function_status(self):
        """
        Interface which returns self.function_status

        :return: String that is stored in self.function_status
        """
        return self.function_status

    def reset_alarm(self):
        """
        Interface that resets an error state: RE, ID, Tower

        :return:
        """
        self.send_command("RE,1," + self.tower)

    # Private functions
    def send_command(self, message):
        """
        Claims the socket lock and sends a message through the socket to the Effimat PLC
        The message is printed for debugging purposes

        :param message: String containing the the message in a format the Effimat PLC understands

        :return:
        """
        with self.socket_lock:
            self.print_message(message)
            message = message + "\n"
            self.socket.send(message.encode())

    def print_message(self, message):
        """
        Function that prints a constructed for the Effimat including a timestamp
        Timestamp represents when the function was called, not when the message argument was created

        :param message: String containing the message in a format the Effimat PLC understands

        :return:
        """
        print(
            helper_functions.get_time()
            + "Effimat: Data sent to PLC: "
            + message
        )

    def send_done_received(self):
        """
        Function that sends a Done Received (acknowledge) message to the Effimat. Check the official communications
        protocol guidelines to see when this is necessary.
        It then waits for a Done Received Received answer back from the PLC

        :return:
        """
        self.send_command("DR,1," + self.tower)
        while self.answer.find("DRR,1," + self.tower + ",100") == -1:
            self.receive_message()
            time.sleep(0.1)

    def receive_message(self):
        """
        Function that claims the socket lock, and listens to the socket untill a message is received.
        It passes this message, after decoding, to the self.answer variable.
        The message is then printed to the console for potential debugging purposes

        :return:
        """
        with self.socket_lock:
            try:
                self.answer = self.socket.recv(1024).decode()
                self.print_answer()
            except ValueError:
                self.answer = self.previous_answer
                print("Value error; value of answer set to: " + self.answer)
                pass

    def print_answer(self):
        """
        Function that prints the self.answer variable, including a timestamp
        Timestamp represents when the function was called, not when the self.answer variable was changed

        :return:
        """
        print(
            helper_functions.get_time()
            + "Effimat: Data received from PLC: "
            + self.answer
        )

    def set_initial_status(self):
        """
        Function that loops while the Effimat's status has not been initialised yet. Keeps sending a status request
        until an answer is received

        :return:
        """
        # First time run: Ask for the status
        while self.status == "Not initialized":
            time.sleep(5)
            # Status request: SR, ID, Tower
            self.send_command("SR,1," + self.tower)
            self.receive_message()
            if self.answer:
                return

    def update_status(self):
        """
        Method that updates the internal status variable for the Effimat

        :return:
        """
        if self.answer != self.previous_answer:
            self.previous_answer = self.answer

            # Split the answer on the comma and remove the first character because that's corrupted
            split_answer = self.answer[1:].split(",")
            function = split_answer[0]

            try:
                status_code = int(split_answer[3])
            except ValueError as e:
                print("Oops!" + str(e) + "\t" + str(split_answer[3]))
                while True:
                    pass

            # TODO: This part requires some attention
            # Check if the response ia Status Request or Automated status response
            if function == "SRR" or function == "STR":
                self.status = self.status_list.get(
                    status_code, "Unknown Status: " + str(status_code)
                )
                self.ready_to_receive_order = True

            # Check if the response is from a Move Box, Deliver Box or Done Received
            if function == "MBR" or function == "BHR" or function == "DRR":
                self.function_status = self.function_status_list.get(
                    status_code, "Unknown Status: " + str(status_code)
                )

            # If the function status is a movement error, set the effimat status on Alarm
            if self.function_status == "Movement error":
                self.status = "Alarm"
                self.ready_to_receive_order = False

            # If alarm has been reset, send a continue command to Effimat
            if self.status == "Alarm reset, ready for continue":
                # Continue operation: CO, ID, Tower
                self.send_command("CO,1," + self.tower)
                self.ready_to_receive_order = True

    def open_front(self):
        """
        Disables the light curtain alarm, allowing a user to interact with the opening at the front

        :return:
        """
        self.send_command("AO,1," + self.tower + ",O,N,N,N,N,N,N,N")

        while self.answer.find("AOR,1," + self.tower + ",100") == -1:
            print("Waiting for answer; AOR open")
            self.receive_message()
            time.sleep(0.1)

        self.send_done_received()

    def close_front(self):
        """
        Enables the light curtain alarm

        :return:
        """
        self.send_command("AO,1," + self.tower + ",C,N,N,N,N,N,N,N")

        while self.answer.find("AOR,1," + self.tower + ",100") == -1:
            print("Waiting for answer; AOR close")
            self.receive_message()
            time.sleep(0.1)

        self.send_done_received()

    def retrieve(self, carrier_id, destination):
        """
        Retrieves a carrier with carrier_id from the warehouse, places it at destination

        :param carrier_id:  Integer representing the ID of the carrier, as noted in the warehouse database
        :param destination: String representing the destination the carrier has to go to

        :return:
        """
        # Close the front opening
        self.close_front()

        # Get the warehouse location of the destination
        end_point = self.get_warehouse_location(destination)

        # Send a move action to the effimat PLC
        self.move_carrier(carrier_id, end_point)

        # Open the front opening
        self.open_front()

        self.ready_to_receive_order = True

    def store(self, carrier_id, pickup_location):
        # TODO: Have the user select in the webinterface whether it's a large or small storage container
        # TODO: Have that passed to this function eventually
        """
        Stores a carrier with carrier_id in the warehouse, picking it up at pickup_location

        :param carrier_id:  Integer representing the ID of the carrier, as noted in the warehouse database
        :param pickup_location: String representing where the carrier has to be picked up

        :return:
        """
        # Close the front opening
        self.close_front()

        # Use the store function to move the carrier in an open spot
        self.store_carrier(carrier_id, "large_storage", pickup_location)

        # Open the front opening
        self.open_front()

        self.ready_to_receive_order = True

    def store_carrier(self, carrier_id, carrier_type, pickup_location):
        """
        Function that finds out the origination of the carrier, calls a function to find a place to store
        and calls the function that actually sends the move commands to the Effimat

        :param carrier_id: 		integer representing the ID of the carrier
        :param carrier_type: 	string representing the physical size of the effimat, denotes where it can be stored
        :param pickup_location: WarehouseLocation representing where the carrier should be picked up

        :return:
        """
        # Step 1: Find pickup location
        origination = self.get_warehouse_location(pickup_location)
        origination = origination.split(",")
        # Update database
        self.update_db(origination[0], origination[1], origination[2], carrier_id)

        # Step 2 : Find empty location in the warehouse
        store_location = self.find_store_location(carrier_type)
        print("store_location = " + store_location)

        if store_location:
            # Step 3: Send move command to effimat
            self.move_carrier(carrier_id, store_location)
        else:
            print("No free location found of type" + carrier_type)

    def move_carrier(self, carrier_id, destination):
        """
        Sends commands to the Effimat to move the elevator and updates the database accordingly. Also calls the function
        to move a carrier horizontal, if necessary (and if the path is free)

        :param carrier_id: 	integer representing the ID of the carrier
        :param destination: string representing where the carrier needs to go
        :return:
        """

        # Set origination
        origination = self.get_carrier_location(carrier_id).split(",")
        loc_origination = warehouse_loc.WarehouseLocation(
            origination[0], origination[1], origination[2]
        )
        destination = destination.split(",")
        loc_destination = warehouse_loc.WarehouseLocation(
            destination[0], destination[1], destination[2]
        )
        print(
            loc_destination.row
            + ","
            + loc_destination.column
            + ","
            + loc_destination.side
        )

        # Set the carrier on the elevator
        self.load_elevator(loc_origination)

        # Update the database
        self.update_db(
            loc_origination.row,
            loc_origination.column,
            loc_origination.side,
            self.no_carrier,
        )  # empty old
        self.update_db(1, loc_origination.column, "E", carrier_id)  # fill new location

        # Move carrier to position on elevator
        if loc_origination.column != loc_destination.column:
            # Send move command to Effimat
            if self.check_free_horizontal_path(
                int(loc_origination.column), int(loc_destination.column)
            ):
                self.move_carrier_horizontal(loc_origination, loc_destination)
                # Update the database
                self.update_db(
                    1, loc_origination.column, "E", self.no_carrier
                )  # empty old location
                self.update_db(
                    1, loc_destination.column, "E", carrier_id
                )  # fill new location
            else:
                print("The carrier cannot be moved to the column of the destination - the path is blocked")
                # TODO: Have the Effimat return the carrier if this is the case
                # TODO: Alternatively; generate a popup screen asking for a new destination
                return

        # Set the carrier on the destination
        self.unload_elevator(loc_destination)

        # Update the database
        self.update_db(1, loc_destination.column, "E", -1)  # empty old location
        self.update_db(
            loc_destination.row,
            loc_destination.column,
            loc_destination.side,
            carrier_id,
        )

        # End message
        print(helper_functions.get_time() + "Effimat: Move function finished")

    def load_elevator(self, location):
        # TODO: Make this accept a variable amount of parameters, to load multiple positions
        """
        Loads elevator with a carrier from given location

        :param location: WarehouseLocation containing info on where the carriers needs to go
        :return:
        """
        action = "R" + location.side
        columns = (
            ((int(location.column) - 1) * "N,")
            + action
            + ((5 - int(location.column)) * ",N")
        )
        command = "BH,1," + self.tower + "," + str(location.row) + "," + columns
        self.send_command(command)

        while self.answer.find("BHR,1," + self.tower + ",100") == -1:
            self.receive_message()
            time.sleep(0.1)
        self.send_done_received()

    def unload_elevator(self, location):
        # TODO: Make this accept a variable amount of parameters, to unload multiple positions
        """
        Unloads elevator with a carrier from given location

        :param location: WarehouseLocation containing info on where the carriers needs to come from
        :return:
        """
        action = "D" + location.side
        columns = (
            ((int(location.column) - 1) * "N,")
            + action
            + ((5 - int(location.column)) * ",N")
        )
        command = "BH,1," + self.tower + "," + str(location.row) + "," + columns
        self.send_command(command)

        while self.answer.find("BHR,1," + self.tower + ",100") == -1:
            self.receive_message()
            time.sleep(0.1)
        self.send_done_received()

    def move_carrier_horizontal(self, origination, destination):
        """
        Moves a carrier on the lift horizontally

        :param origination: WarehouseLocation containing info on where the carrier came from
        :param destination: WarehouseLocation containing info on where the carrier needs to go
        :return:
        """
        if (0 < int(origination.column) < 6) and (0 < int(destination.column) < 6):
            columns = origination.column + "-" + destination.column
            command = (
                "MB,1,1," + self.tower + origination.row + ",F," + columns + ",N,N,N,N"
            )
            self.send_command(command)
            
            while self.answer.find("MBR,1," + self.tower + ",100") == -1:
                self.receive_message()
                time.sleep(0.1)
            self.send_done_received()

    def check_free_horizontal_path(self, origin_column, destin_column):
        """
        Function that checks whether or not a given horizontal path is free or not.

        :param origin_column: integer representing the column where the carries comes from
        :param destin_column: integer representing the column where the carries needs to go
        :return: boolean representing whether or not the path is free
        """
        query = "SELECT column, carrier_id FROM carriers WHERE type = 'Elevator'"
        result = self.query_db_as_json(query)

        if origin_column > destin_column:
            for results in result:
                if origin_column < int(results["column"]) <= destin_column:
                    if int(results["carrier_id"]) != -1:
                        return False
        elif origin_column > destin_column:
            for results in reversed(result):
                if origin_column > int(results["column"]) >= destin_column:
                    if int(results["carrier_id"]) != -1:
                        return False
        return True

    # Database interactions

    def get_carrier_location(self, carrier_id):
        """
        Function to retrieve the current location of a specific carrier, retrieved from SQLiteDatabase

        :param carrier_id: integer representing the carrier
        :return: string containing row, column and side
        """
        query = "SELECT row,column,side FROM carriers WHERE carrier_id = " + str(
            carrier_id
        )
        result = str(self.query_db(query, True))
        result = "".join(e for e in result if e.isalnum() or e == ",")
        return result

    def get_warehouse_location(self, location_type):
        """
        Returns the first spot of given location_type. If this is meant for one of the openings of the effimat
        It will always return exactly that spot (since there is only one spot with that specific typing)

        :param location_type: 	string representing the location_type
        :return: 				string containing the row, column and side
        """
        query = (
            "SELECT row, column, side FROM carriers WHERE type = '"
            + location_type
            + "'"
        )
        result = str(self.query_db(query, True))
        result = "".join(e for e in result if e.isalnum() or e == ",")
        return result

    def find_store_location(self, carrier_type):
        """
        Function to find the closest free location with given carrier_type as type

        :param carrier_type: string representing the type of the carrier that needs to be stored
        :return:
        """
        query = (
            "SELECT row,column,side FROM carriers WHERE carrier_id = -1 and type = '"
            + carrier_type
            + "' ORDER BY height"
        )
        result = str(self.query_db(query, True))
        result = "".join(e for e in result if e.isalnum() or e == ",")
        return result

    def update_db(self, row, column, side, carrier):
        """
        Modifies existing records in the carrier table

        :param row:		integer representing the row in the machine
        :param column: 	integer representing the column in the machine
        :param side: 	string representing the side (F or B) of the machine
        :param carrier: integer representing the carrier_id
        :return:
        """
        query = (
            "UPDATE carriers SET 'carrier_id'="
            + str(carrier)
            + " WHERE row="
            + str(row)
            + " and column="
            + str(column)
            + " and side='"
            + side
            + "'"
        )
        self.query_db(query)

    def query_db(self, query, return_one=False):
        # TODO: This function can technically be added to helper functions and be reused
        """
        Executes a query on the SQL database

        :param query:		string containing the query to the database
        :param return_one:	boolean representing whether just one result needs to be returned or not
        :return:			list or single result from the query
        """
        with self.db_lock:
            # Create a cursor to the database
            conn_warehouse = sqlite3.connect("../databases/effimat_warehouse.db")
            warehouse = conn_warehouse.cursor()
            # Execute the query
            warehouse.execute(query)
            # Get the result of the query
            r = warehouse.fetchall()
            # Commit changes and close the database
            warehouse.connection.commit()
            warehouse.connection.close()
            if return_one:
                return r[0]
            else:
                return r

    def query_db_as_json(self, query, return_one=False):
        # TODO: This function can technically be added to helper functions and be reused
        """
        Executes a query on the SQL database and returns the data as json format
        Source: https://stackoverflow.com/questions/3286525/return-sql-table-as-json-in-python/3287775

        :param query:		string containing the query to the database
        :param return_one:	boolean representing whether just one result needs to be returned or not
        :return:			list or single result from the query
        """
        with self.db_lock:
            conn_warehouse = sqlite3.connect("../databases/effimat_warehouse.db")
            warehouse = conn_warehouse.cursor()
            warehouse.execute(query)
            r = [
                dict(
                    (warehouse.description[i][0], value) for i, value in enumerate(row)
                )
                for row in warehouse.fetchall()
            ]
            warehouse.connection.commit()
            warehouse.connection.close()
            # Return the result
            if r:
                if return_one:
                    return r[0]
                else:
                    return r
            else:
                return None
