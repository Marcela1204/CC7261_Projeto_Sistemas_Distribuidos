from datetime import datetime
import zmq

context = zmq.Context()

msg_socket = context.socket(zmq.REP)
msg_socket.bind("tcp://*:7777")
 
while True:
    keepalive = msg_socket.recv_string()
    
    msg_socket.send_string(datetime.now().strftime("%d-%m-%y %H:%M:%S"))
