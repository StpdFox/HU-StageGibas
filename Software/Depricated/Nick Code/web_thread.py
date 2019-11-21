from http.server import BaseHTTPRequestHandler, HTTPServer
from os import curdir, sep
from threading import Thread
import cgi
import helper_functions
import webbrowser


class Webserver(Thread, HTTPServer):
    # TODO: Implement status requests
    def __init__(self, ip, port, PTS):
        Thread.__init__(self)
        self.ip = ip
        self.port = port
        self.server_url = "http://" + str(self.ip) + ":" + str(self.port)

        def handler(*args):
            Handler(PTS, *args)

        self.webserver = HTTPServer((self.ip, self.port), handler)

        # This function starts the webserver and stays running forever

    def run(self):
        """
        This function is run with the class.start() function from the main. This is the main body of the thread.

        :return:
        """
        # Create a web server
        # webserver = HTTPServer((self.ip, self.port), MyHandler(self.pts))
        print(helper_functions.get_time() + "Webserver: Started on port: ", self.port)
        # Open webbrowser
        webbrowser.open_new_tab(self.server_url)
        # Wait forever for incoming http requests
        self.webserver.serve_forever()


class Handler(BaseHTTPRequestHandler):
    def __init__(self, PTS, *args):
        self.pts = PTS
        BaseHTTPRequestHandler.__init__(self, *args)
        self.path = "/"
        # self.pts.start()

    # Handler for the GET requests
    def do_GET(self):
        if self.path == "/" or self.path == "/index":
            print(helper_functions.get_time() + self.path)
            self.path = "/webpages/index.html"

        try:
            # Check the file extension required and
            # set the right mime type
            mimetype = ""
            send_reply = False
            if self.path.endswith(".html"):
                mimetype = "text/html"
                send_reply = True
            if self.path.endswith(".jpg"):
                mimetype = "image/jpg"
                send_reply = True
            if self.path.endswith(".gif"):
                mimetype = "image/gif"
                send_reply = True
            if self.path.endswith(".js"):
                mimetype = "application/javascript"
                send_reply = True
            if self.path.endswith(".css"):
                mimetype = "text/css"
                send_reply = True

            if send_reply:
                # Open the static file requested and send it
                f = open(curdir + sep + self.path)
                self.send_response(200)
                self.send_header("Content-type", mimetype)
                self.end_headers()
                self.wfile.write(f.read().encode())
                f.close()
            return

        except IOError:
            self.send_error(404, "httpserver: File Not Found: %s" % self.path)

            # Handler for the POST requests

    def do_POST(self):
        # Connect to the fields in the website
        form = cgi.FieldStorage(
            fp=self.rfile,
            headers=self.headers,
            environ={
                "REQUEST_METHOD": "POST",
                "CONTENT_TYPE": self.headers["Content-Type"],
            },
        )
        self.send_response(200)
        self.end_headers()

        # Send user to operation page
        if self.path == "/operation":
            f = open(curdir + sep + "/webpages/" + form["operation"].value + ".html")
            self.wfile.write(f.read().encode())

        # Add order to the queue and send confirmation page
        if self.path == "/send":
            # Prepare values to be added to the order
            operation = form["operation"].value
            location = form["location"].value
            carrier = form["carrier"].value
            # Set the locations
            if operation == "carrier_pick" or operation == "product_pick":
                location_origination = location
                location_destination = ""
            else:
                location_origination = ""
                location_destination = location
                # If more then one product, then comma separate them into a string
            products = ""
            for i in range(10):
                if form["product" + str(i)].value != "":
                    products = products + form["product" + str(i)].value + ","
            if products != "":
                products = products[:-1]  # trim last comma away
                # Add the order to the queue
            self.pts.add_order(
                operation=operation,
                sender="to be filled in",
                location_origination=location_origination,
                location_destination=location_destination,
                priority="normal",
                start_type="after previous",
                start_time="",
                carrier=carrier,
                products=products,
            )
            # Print the last order
            # print(
            #     helper_functions.get_time()
            #     + "Webserver: Added order: "
            #     + self.pts.get_last_order()
            # )
            print(helper_functions.get_time() + "Webserver: Added order")

            # Create confirmation webpage
            html_conf = (
                "<html><body>"
                "Your order has been succesfully send.<br>You ordered the following: "
                + "To be done."
                + "<form><input type='button' value='Back' "
                "onclick=\"window.location.href='/webpages/index.html'\"/>"
                "</form></body></html>"
            )
            # Send webpage to client
            self.wfile.write(html_conf.encode())
            return

            # Send status page and reset alarm if requested
        if self.path == "/status" or self.path == "/reset_alarm":
            # If alarm reset was requested, start the reset alarm function
            if self.path == "/reset_alarm":
                self.pts.reset_effimat_alarm()
                # Create queue list
            my_queue = ""
            # for elem in list(self.pts.OrderQueue.queue):
            # my_queue = my_queue + elem.activity_in_text() + "<br>"
            # Create current task
            # if self.pts.CurrentOrder.operation == "":
            my_task = "No current task"
            # else:
            # my_task = self.pts.CurrentOrder.activity_in_text()
            # Create status webpage
            html_stat = self.html_status_website(my_task)
            # Send webpage to client
            self.wfile.write(html_stat.encode())
            return

            # This function returns the html code for the status website

    def html_status_website(self, my_task):
        # Only show the alarm reset button when an alarm is active
        # TODO: FIX status website
        if "NO_ALARM" == "Alarm":
            alarm_code = (
                "<form method = 'POST' action='/reset_alarm'><input type='submit' "
                "value = 'Reset Effimat Alarm'/></form><br>"
            )
        else:
            alarm_code = ""

        html_code = (
            "<html><body>"
            # "Status mir: " + self.pts.get_mir_status() + "<br>"
            # "Status universal robot: " + self.pts.get_ur_status() + "<br>"
            "Status mir: " + "UNIMPLEMENTED" + "<br>"
            "Status universal robot: " + "UNIMPLEMENTED" + "<br>"
            "Status effimat: " + "UNIMPLEMENTED" + "<br>"
            "Status effimat function: "
            + "UNIMPLEMENTED"
            + "<br><br>"
            "Current task:<br>" + "UNIMPLEMENTED" + "<br><br>"
            "Queue:<br>"
            # TODO: Figure out what to do with this queue situation
            + "UNIMPLEMENTED"
            + "<br>"
            + alarm_code
            + "<form method = 'POST' action='/status'><input type='submit' value = 'Refresh'/></form>"
            "<br><form><input type='button' value='Back' "
            "onclick=\"window.location.href='/webpages/index.html'\"/>"
            "</form></body></html>"
        )
        return html_code
