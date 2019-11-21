from threading import Thread
import sqlite3
import helper_functions
import json
import time


class PTS(Thread):
    def __init__(self, effimat_container):
        """
        Initialiser for the PTS

        :param effimat_container:
        """
        Thread.__init__(self)

        # Turns the list into a dict, so that given orders can be added to the Effimat
        self.effimat_list = {i: -1 for i in effimat_container}
        # TODO: Implement containers for MiR and UR once they are ready
        # self.mir_list = mir_container
        # self.ur_list = ur_container

    def run(self):
        """
        This function is run with the class.start() function from the main. This is the main body of the thread.

        :return:
        """
        while True:
            self.validate_new_order()
            self.handle_effimats()
            time.sleep(1)

    def handle_effimats(self):
        """
        This functions handles all pure orders for the Effimat -> Retrieve and Store of carriers

        :return:
        """
        for effimat in self.effimat_list:
            effimat_status = effimat.get_status()

            if effimat.is_ready_to_receive_order():
                if self.effimat_list[effimat] == -1:

                    effimat_order = self.get_next_effimat_operation()

                    if not effimat_order:
                        continue
                    else:
                        if effimat_order["operation"] == "effimat_store":
                            if effimat.store_order(
                                effimat_order["carrier"],
                                effimat_order["location_destination"],
                            ):
                                self.update_task_status(
                                    effimat_order, effimat_order["task_1"], "active"
                                )
                                self.effimat_list[effimat] = effimat_order
                        elif effimat_order["operation"] == "effimat_retrieve":
                            if effimat.retrieve_order(
                                effimat_order["carrier"],
                                effimat_order["location_destination"],
                            ):
                                self.update_task_status(
                                    effimat_order, effimat_order["task_1"], "active"
                                )
                                self.effimat_list[effimat] = effimat_order
                        else:
                            print(
                                "This isn't purely an Effimat order!"
                                + effimat_order["operation"]
                            )
                            self.update_task_status(
                                effimat_order, effimat_order["task_1"], "rejected"
                            )
                else:
                    self.update_task_status(
                        self.effimat_list[effimat],
                        self.effimat_list[effimat]["task_1"],
                        "finished",
                    )
                    self.effimat_list[effimat] = -1
            else:
                print("Effimat status: " + effimat_status)

    def retrieve_product(self, order):
        """
        Function that retrieves a specific product -> Uses MiR, UR and Effimat

        :param order:
        :return:
        """
        order = order + 1
        # TODO: Requires the implementation of all other robots

    def store_product(self, order):
        """
        Function that stores a specific product -> Uses MiR, UR and Effimat

        :param order:
        :return:
        """
        order = order + 1
        # TODO: Requires the implementation of all other robots

    def add_order(
        self,
        operation,
        sender,
        location_origination="",
        location_destination="",
        priority="normal",
        start_type="after previous",
        start_time="",
        carrier="",
        products="",
    ):
        """
        Adds an order to the database

        :param operation:
        :param sender:
        :param location_origination:
        :param location_destination:
        :param priority:
        :param start_type:
        :param start_time:
        :param carrier:
        :param products:
        :return:
        """
        # Fill in the non user columns
        status = "added"
        time_eta = ""
        start_order = "-1"
        time_ordered = helper_functions.get_time()

        # Fill in the tasks
        # TODO: Dit soort dingen geven me altijd een beetje de kriebels,. dit kan vast netter
        if operation == "product_pick":
            task_1 = "warehouse_to_ur"
            task_2 = "to_effimat_front"
            task_3 = "effimat_to_mir"
            task_4 = "front_to_warehouse"
            task_5 = "to_delivery_point"
            task_1_device = "effimat"
            task_2_device = "mir"
            task_3_device = "ur"
            task_4_device = "effimat"
            task_5_device = "mir"
        if operation == "product_put_back":
            task_1 = "to_pickup_point"
            task_2 = "to_effimat_front"
            task_3 = "mir_to_effimat"
            task_4 = "front_to_warehouse"
            task_5 = "-"
            task_1_device = "mir"
            task_2_device = "mir"
            task_3_device = "ur"
            task_4_device = "effimat"
            task_5_device = "-"
        if operation == "carrier_pick":
            task_1 = "receive_from_effimat_back"
            task_2 = "warehouse_to_outfeed"
            task_3 = "outfeed_to_delivery_point"
            task_4 = "-"
            task_5 = "-"
            task_1_device = "mir"
            task_2_device = "effimat"
            task_3_device = "mir"
            task_4_device = "-"
            task_5_device = "-"
        if operation == "carrier_put_back":
            task_1 = "to_pickup_point"
            task_2 = "deliver_to_effimat_back"
            task_3 = "infeed_to_warehouse"
            task_4 = "-"
            task_5 = "-"
            task_1_device = "mir"
            task_2_device = "mir"
            task_3_device = "effimat"
            task_4_device = "-"
            task_5_device = "-"
        if operation == "effimat_store":
            task_1 = "to_warehouse"
            task_2 = "-"
            task_3 = "-"
            task_4 = "-"
            task_5 = "-"
            task_1_device = "effimat"
            task_2_device = "-"
            task_3_device = "-"
            task_4_device = "-"
            task_5_device = "-"
        if operation == "effimat_retrieve":
            task_1 = "warehouse_to"
            task_2 = "-"
            task_3 = "-"
            task_4 = "-"
            task_5 = "-"
            task_1_device = "effimat"
            task_2_device = "-"
            task_3_device = "-"
            task_4_device = "-"
            task_5_device = "-"

        # Add new order to the database
        query = (
            "INSERT INTO orders ("
            "operation, location_origination, location_destination, status, time_ordered, time_eta, sender, "
            "priority, start_type, start_time, start_order, carrier, task_1, task_1_device, task_2, task_2_device, "
            "task_3, task_3_device, task_4, task_4_device, task_5, task_5_device, products) "
        )
        query_values = (
            "VALUES ('"
            + operation
            + "','"
            + location_origination
            + "','"
            + location_destination
            + "','"
            + status
            + "','"
            + time_ordered
            + "','"
            + time_eta
            + "','"
            + sender
            + "','"
            + priority
            + "','"
            + start_type
            + "','"
            + start_time
            + "',"
            + start_order
            + ","
            + carrier
            + ",'"
            + task_1
            + "','"
            + task_1_device
            + "','"
            + task_2
            + "','"
            + task_2_device
            + "','"
            + task_3
            + "','"
            + task_3_device
            + "','"
            + task_4
            + "','"
            + task_4_device
            + "','"
            + task_5
            + "','"
            + task_5_device
            + "','"
            + products
            + "')"
        )

        # Add the values to the query
        query = query + query_values
        # Execute the query
        self.query_db(query)

    def validate_new_order(self):
        """
        Function that validates newly 'added' orders, checks the entire database.
        Will turn then to 'scheduled' if the order is valid, otherwise it will become 'declined'.

        :return:
        """
        # Retrieve the order with the highest ID
        query = "SELECT id FROM orders WHERE status = 'added'"
        myresult = self.query_db(query)
        # Set each order to status "scheduled" In a later stage there should be some checks here
        for order_id in myresult:
            query = "UPDATE orders SET status= 'scheduled' WHERE id=" + str(order_id[0])
            self.query_db(query)
            print(
                helper_functions.get_time()
                + "Order: Changed status to scheduled of order_id "
                + str(order_id[0])
            )

    def update_task_status(self, my_order, my_task, new_status):
        """
        Updates the status of a specific task of a specific order to the new_status given in the database.

        :param my_order: String containing the name of the order
        :param my_task: String containing the number of the task
        :param new_status: String containing the new status
        :return:
        """
        # Find which fieldname to change
        if my_order["task_1"] == my_task:
            field = "task_1_status"
        elif my_order["task_2"] == my_task:
            field = "task_2_status"
        elif my_order["task_3"] == my_task:
            field = "task_3_status"
        elif my_order["task_4"] == my_task:
            field = "task_4_status"
        else:
            field = "task_5_status"
        # Update the status of the order
        query = (
            "UPDATE orders SET "
            + field
            + " = '"
            + new_status
            + "' WHERE id="
            + str(my_order["id"])
        )
        self.query_db(query)

    def get_next_effimat_operation(self):
        """
        Returns the first pure Effimat operation from the database

        :return:
        """
        query = (
            "SELECT * FROM orders WHERE ( operation = 'effimat_store' "
            "OR operation = 'effimat_retrieve' )"
            "AND status = 'scheduled' "
            "AND task_1_status = 'open'"
        )
        myresult = self.query_db_as_json(query, True)
        if myresult:
            return myresult

    def get_next_order(self, device):
        """
        This function returns the next order for a specific device
        The following order has the priority:
        1) status = active
        2) priority = high
        3) timed order <-- not finished
        4) order with the lowest id

        :param device:
        :return:
        """
        # Set up the device specific query
        device_query = (
            "((task_1_device = '" + device + "' and task_1_status = 'open') OR "
            "(task_2_device = '" + device + "' and task_2_status = 'open') OR "
            "(task_3_device = '" + device + "' and task_3_status = 'open') OR "
            "(task_4_device = '" + device + "' and task_4_status = 'open') OR "
            "(task_5_device = '" + device + "' and task_5_status = 'open'))"
        )

        # 1) Check for an order with status active
        query = (
            "SELECT * FROM orders WHERE id = (SELECT min(id) FROM orders WHERE status = 'active' AND "
            + device_query
            + ")"
        )
        # myresult = self.query_db_as_json(query, return_one=True)
        myresult = self.query_db_as_json(query)
        if myresult:
            return myresult

        # 2) Check for an order with priority high and which is scheduled
        query = (
            "SELECT * FROM orders WHERE id = (SELECT min(id) FROM orders WHERE status = 'scheduled' AND "
            "priority = 'high' AND " + device_query + ")"
        )
        myresult = self.query_db_as_json(query)
        # myresult = self.query_db_as_json(query, return_one=True)
        if myresult:
            return myresult

        # 3) Check for an order which start on time and time has expired
        query = (
            "SELECT * FROM orders WHERE id = (SELECT min(id) FROM orders WHERE status = 'scheduled' AND "
            "start_type = 'time' AND start_time = 0 AND " + device_query + ")"
        )
        myresult = self.query_db_as_json(query)
        # myresult = self.query_db_as_json(query, return_one=True)
        if myresult:
            return myresult

            # 4) Check for a scheduled order with the lowest id number
        query = (
            "SELECT * FROM orders WHERE id = (SELECT min(id) FROM orders WHERE status = 'scheduled' AND "
            + device_query
            + ")"
        )
        myresult = self.query_db_as_json(query)
        # myresult = self.query_db_as_json(query, return_one=True)
        if myresult:
            return myresult
        else:
            return "no next order"

    def get_last_order(self):
        """
        Function that returns the latest placed order from the database

        :return: json.dumps (string) of the latest placed order
        """
        # Retrieve the order with the highest ID
        query = "SELECT * FROM orders WHERE id = (SELECT max(id) FROM orders)"
        my_result = self.query_db_as_json(query)
        # Return the result
        return json.dumps(my_result)

        # Executes a query on the SQL database

    def query_db(self, query, return_one=False):
        # TODO: This function can technically be added to helper functions and be reused
        """
        Executes a query on the SQL database

        :param query:		string containing the query to the database
        :param return_one:	boolean representing whether just one result needs to be returned or not

        :return:			list or single result from the query
        """
        # Create a cursor to the database
        conn_orders = sqlite3.connect("../databases/main_orders.db")
        orders = conn_orders.cursor()
        # Execute the query
        orders.execute(query)
        # Get the result of the query
        r = orders.fetchall()
        # Commit changes and close the database
        orders.connection.commit()
        orders.connection.close()
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
        conn_orders = sqlite3.connect("../databases/main_orders.db")
        orders = conn_orders.cursor()
        orders.execute(query)
        r = [
            dict((orders.description[i][0], value) for i, value in enumerate(row))
            for row in orders.fetchall()
        ]
        orders.connection.commit()
        orders.connection.close()
        # Return the result
        if r:
            if return_one:
                return r[0]
            else:
                return r
        else:
            return None

    def reset_effimat_alarm(self, effimat_number=0):
        """
        Function that calls the reset_alarm() function of the given Effimat tower

        :param effimat_number: integer representing the Effimat tower; standard is 0 for 1 tower
        :return:
        """
        self.effimat_list[effimat_number].reset_alarm()

    def get_effimat_status(self, effimat_number):
        """
        Function that returns the state of the given Effimat tower

        :param effimat_number: integer representing the Effimat tower; standard is 0 for 1 tower
        :return: string representing the state of the given Effimat tower
        """
        if self.effimat_list:
            return self.effimat_list[effimat_number].get_status()
        return "No Effimats registered"

    def get_effimat_function_status(self, effimat_number):
        """
        Function that returns the function_state of the given Effimat tower

        :param effimat_number: integer representing the Effimat tower; standard is 0 for 1 tower
        :return: string representing the function_state of the given Effimat tower
        """
        if self.effimat_list:
            return self.effimat_list[effimat_number].get_function_status()
        return "No Effimats registered"
