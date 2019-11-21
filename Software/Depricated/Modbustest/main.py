import threading
import time

msg = ""


class ServerThread(threading.Thread):
    def run(self):
        print("{} started!".format(self.getName())
              )
        while True:
            print(msg)
            time.sleep(4)

        time.sleep(1)
        print("{} finished!".format(self.getName())
              )             # "Thread-x finished!"


class ModBusThread(threading.Thread):
    def run(self):
        print("{} started!".format(self.getName())
              )

        for i in range(4):
            msg = i
            time.sleep(5)
        print("{} finished!".format(self.getName())
              )


def main():
    ServerThread(None, name="Serverthread").start()

    ModBusThread(None, name="ModBusThread").start()
    time.sleep(.9)


if __name__ == '__main__':
    main()
