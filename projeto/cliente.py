import zmq
from time import sleep
import random

context = zmq.Context()
socket = context.socket(zmq.REQ)
socket.connect("tcp://broker:5555")

acoes = ["adiciona", "lista"]

try:
    while True:
        socket.send_string(f"logar user_{random.randint(1,99)}")
        if socket.recv_string() == "usuario logado":
            oqfazer = f"{random.choice(acoes)} canal_{random.randint(1,999)}"
            socket.send_string(oqfazer)
            resposta = socket.recv_string()
            print(resposta)
            sleep(0.5)
        else:
            print("falha no login, usuario ja logado")
            sleep(0.5)
except KeyboardInterrupt:
    pass
finally:
    socket.close()
    context.term()


