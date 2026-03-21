import zmq
from time import sleep
import random

#NOTE: seção do requester
context = zmq.Context()
socket = context.socket(zmq.REQ)
socket.connect("tcp://broker:5555")

#NOTE: seção do subscriber
context = zmq.Context()
sub = context.socket(zmq.SUB)
sub.setsockopt_string(zmq.SUBSCRIBE, "canal")
sub.connect("tcp://proxy:6666")


try:
    while True:
        oqfazer = f"mensagem_{random.randint(1,999)}"
        socket.send_string(oqfazer)
        resposta = socket.recv_string()
        print(resposta,flush=True)
        mensagens = sub.recv_string()
        print(mensagens,flush=True)
        sleep(0.5)
except KeyboardInterrupt:
    pass
finally:
    socket.close()
    context.term()


