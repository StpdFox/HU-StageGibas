
# Libraries:
from universal_thread import *          # contains Universal Robot functions
from festo_comm import *                # contains Festo functions
import main_shared_vars as var          # variables which are var between files
import threading                        # Standard library, used for running threads
import time                             # Standard library, used for time functions


# Update the following variables:
my_ip = "127.0.0.1"                                     # IP Address of host
festo_ip = "172.16.0.20"                                # IP Address of festo
port_for_ur = 9001                                      # Port number for communication with Universal Robot

def main_program():
    # Start the TCP socket server to the UR as a thread (Thread-1)
    URSocketTCPServer = threading.Thread(target=start_ur_server, args=(my_ip, port_for_ur), )
    URSocketTCPServer.start()

    # Start the Festo communication (Thread-2)
    FestoComm = threading.Thread(target=start_festo_comm, args=(festo_ip,), )
    FestoComm.start()
    # Print IP to command prompt
    print(get_time() + "Main: Servers are running on " + my_ip)
    # Wait forever
    URSocketTCPServer.join()

