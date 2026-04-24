from datetime import datetime
import zmq
import time

context = zmq.Context()

socket = context.socket(zmq.PUB)
socket.bind("tcp://*:7777")
 
while True:
    socket.send_string(datetime.now().strftime("%d-%m-%y %H:%M:%S"))
    time.sleep(1)
