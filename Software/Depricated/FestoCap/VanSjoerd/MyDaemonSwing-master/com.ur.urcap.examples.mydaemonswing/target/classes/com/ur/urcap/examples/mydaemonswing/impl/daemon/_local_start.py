
from main import *

# Start the TCP socket server to the UR as a thread (Thread-1)
MainProgram = threading.Thread(target=main_program, args=(), )
MainProgram.start()
