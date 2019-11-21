import effimat
import web_thread
import pts
import socket
import time


def main():
    # Dynamically retrieve IP
    hostname = socket.gethostname()
    my_ip = str(socket.gethostbyname(hostname))
    effimat_ip = "192.168.71.3"

    # Instantiate Effimat towers
    tower1 = effimat.Effimat("1", effimat_ip, 3000)
    towers = [tower1]
    # Start Effimat processes
    for process in towers:
        process.start()

    # Instantiate PTS
    PTS = pts.PTS(towers)
    # Start PTS process
    PTS.start()

    # Instantiate webserver
    webserver = web_thread.Webserver(my_ip, 9000, PTS)
    # Start webserver process
    webserver.start()


if __name__ == "__main__":
    main()
