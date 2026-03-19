import zmq
from time import sleep
import random
from datetime import datetime

def log(service, msg):
    now = datetime.now().strftime("%Y-%m-%d %H:%M:%S")
    print(f"[{now}] [{service}] {msg}")

context = zmq.Context()
socket = context.socket(zmq.REQ)
socket.connect("tcp://broker:5555")

acoes = ["adiciona", "lista"]

try:
    while True:
        user = f"user_{random.randint(1,99)}"
        log("CLIENTE", f"Tentando logar: {user}")

        socket.send_string(f"logar {user}")
        resposta_login = socket.recv_string()

        log("CLIENTE", f"Resposta login: {resposta_login}")

        if resposta_login == "usuario logado":
            oqfazer = f"{random.choice(acoes)} canal_{random.randint(1,999)}"
            
            log("CLIENTE", f"Enviando comando: {oqfazer}")

            socket.send_string(oqfazer)
            resposta = socket.recv_string()

            log("CLIENTE", f"Resposta servidor:\n{resposta}")

            sleep(0.5)
        else:
            log("CLIENTE", "falha no login, usuario ja logado")
            sleep(0.5)

except KeyboardInterrupt:
    pass
finally:
    socket.close()
    context.term()