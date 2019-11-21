# This program sends a message to a socket client
# Its based on the examples in following websites
# Source 1 : http://www.zacobria.com/universal-robots-knowledge-base-tech-support-forum-hints-tips/knowledge-base/script-client-server/
# Source 2 : https://www.techbeamers.com/python-tutorial-write-multithreaded-python-server/
# Source 3 : https://stackoverflow.com/questions/23828264/how-to-make-a-simple-multithreaded-socket-server-in-python-that-remembers-client


# Libraries:
import main_shared_vars as var       # variables which are var between files
import socket
import threading
from main_functions import *         # contains general functions
import struct


class TCPSocketServer(object):
    def __init__(self, host, port):
        self.host = host
        self.port = port
        self.sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        self.sock.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
        self.sock.bind((self.host, self.port))

    def listen(self):
        self.sock.listen(5)
        while True:

            # Accept connection
            client, address = self.sock.accept()
            print(get_time() + "Universal: Connection accepted from: " +
                  str(address[0]))
            client.settimeout(300)
            # Starting Thread
            threading.Thread(target=self.listenToClient,
                             args=(client, address)).start()
            print(get_time() + "Universal: Thread started")

    def listenToClient(self, client, address):
        data_previous = ""
        size = 2048
        while True:
            try:
                # Read data from client
                data = client.recv(size)
                # if data is new -> print data
                if data != data_previous:
                    print(
                        get_time() + "Universal: Received the following data: " + str(data))
                    data_previous = data
                # if there is data -> analyse data
                if data:
                    analyse_socket_data(data, client)
                # if there is no data -> close connection
                else:
                    print(get_time() +
                          "Universal: Closed connection: Data variable empty")
                    time.sleep(var.main_cycle_time)
                    client.close()
                    var.ur_isReady = False
                    return False

            except Exception as e:
                print(get_time() + "Universal: Closed connection: " + str(e))
                client.close()
                return False


def analyse_socket_data(data, client):
        # Check which data has been send from client
        # Home function requested
    if data.find(b"Home") != -1:
        var.requested_home = True
    # Enable function requested
    elif data.find(b"Enable") != -1:
        var.requested_enable = True
    # Reset function requested
    elif data.find(b"Reset") != -1:
        var.requested_reset = True
    # Position function requested
    elif data.find(b"Position") != -1:
        var.requested_position = True
    # Move function requested
    elif data.find(b"Move") != -1:
        var.requested_move = True
        var.answer_back = "Succeeded"
    # SetSpeed function requested
    elif data.find(b"SetSpeed") != -1:
        var.requested_speed = True
        var.answer_back = "Succeeded"
    # SetTimeout function requested
    elif data.find(b"SetTimeout") != -1:
        var.requested_timeout = True
        var.answer_back = "Succeeded"
    # UR is waiting for feedback
    elif data.find(b"Waiting") != -1:  # -1 means not found
        if var.answer_back:
                # Send command to client
            print(get_time() + "Universal: Data send to client: " + var.answer_back)
            client.send(var.answer_back.encode())
            var.answer_back = ""

    # Speed number send
    elif var.requested_speed:
        data_int = struct.unpack("!i", data)[0]
        var.requested_speed_parameters = data_int
        print(get_time() + "Universal: received number: " + str(data_int))
    # Timeout number send
    elif var.requested_timeout:
        data_int = struct.unpack("!i", data)[0]
        var.requested_timeout_parameters = data_int
        print(get_time() + "Universal: received number: " + str(data_int))
    # Move number send
    elif var.requested_move:
        data_int = struct.unpack("!i", data)[0]
        var.requested_move_parameters = data_int
        print(get_time() + "Universal: received number: " + str(data_int))


def start_ur_server(ip, port):
    socketserver = TCPSocketServer(ip, port)
    print(get_time() + "Universal: started on port: ", port)
    socketserver.listen()
