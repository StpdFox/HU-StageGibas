# Libraries
import time                             # Standard library, used for reading current time and sleeping
import datetime                         # Standard library, used for converting time to readable format


# Returns the current time as a stamp, used for printing to terminal
def get_time():
    ts = time.time()
    st = datetime.datetime.fromtimestamp(ts).strftime('%Y-%m-%d %H:%M:%S')
    return st + " "

