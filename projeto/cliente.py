import zmq
from time import sleep
import random

#NOTE: seção do requester
context = zmq.Context()
socket = context.socket(zmq.REQ)
socket.connect("tcp://broker:5555")
print("request iniciado")

#NOTE: seção do subscriber
context = zmq.Context()
sub = context.socket(zmq.SUB)
sub.setsockopt_string(zmq.SUBSCRIBE, "canal")
sub.connect("tcp://proxy:6665")
print("subscriber iniciado")

#NOTE: seção criador de canais
context = zmq.Context()
socketChan = context.socket(zmq.REQ)
socketChan.connect("tcp://broker:4445")
print("canais iniciado")


acoes = ["adiciona", "lista"]

try:
    while True:
        socketChan.send_string(f"logar user_{random.randint(1,99)}")
        if socketChan.recv_string() == "usuario logado":
            oqfazer = f"{random.choice(acoes)} canal_{random.randint(1,999)}"
            socketChan.send_string(oqfazer)
            resposta = socketChan.recv_string()
            print(resposta)
            oqfazer = f"mensagem_{random.randint(1,999)}"
            socket.send_string(oqfazer)
            resposta = socket.recv_string()
            print(resposta,flush=True)
            mensagens = sub.recv_string()
            print(mensagens,flush=True)
            sleep(0.5)
        else:
            print("falha no login, usuario ja logado")
            sleep(0.5)
except KeyboardInterrupt:
    pass
finally:
    socket.close()
    context.term()


