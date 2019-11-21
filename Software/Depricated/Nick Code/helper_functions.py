import time
import datetime


def get_time():
    """
    Function to return current system time

    :return: datetime containing current system time
    """
    ts = time.time()
    st = datetime.datetime.fromtimestamp(ts).strftime("%Y-%m-%d %H:%M:%S")
    return st + " "
