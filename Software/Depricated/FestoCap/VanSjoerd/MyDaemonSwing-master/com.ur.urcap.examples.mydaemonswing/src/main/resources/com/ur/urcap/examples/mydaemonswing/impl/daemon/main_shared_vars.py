# global variables
requested_home = False              # trigger for homing
requested_reset = False             # trigger for reset
requested_enable = False            # trigger for enable
requested_move = False              # trigger for move
requested_speed = False             # trigger for speed
requested_position = False          # trigger for get position function
requested_timeout = False           # trigger for timeout function
requested_speed_parameters = -1     # contains the new rotation speed
requested_move_parameters = -1      # contains the move radius
requested_timeout_parameters = -1   # contains the function timeout
rotation_speed = 100                # contains the speed it rotates with
default_timeout = 180               # amount of seconds for timeout
timeout = 0                         # current time to wait
answer_back = ""                    # contains a text message to be send to the UR
answer_back_number = 0              # contains a number to be send to the UR
main_cycle_time = 1                 # cycle time in seconds in case of disconnection
amount_of_errors = 0                # amount of festo errors
