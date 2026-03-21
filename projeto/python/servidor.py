from time import sleep
import zmq
from datetime import datetime

def log(service, msg):
    now = datetime.now().strftime("%Y-%m-%d %H:%M:%S")
    print(f"[{now}] [{service}] {msg}")


can = open("canais.txt","r+")
canais.append(can.readlines())

context = zmq.Context()
socket = context.socket(zmq.REP)
socket.connect("tcp://broker:5556")

#NOTE: seção do publisher 
context = zmq.Context()
pub = context.socket(zmq.PUB)
pub.connect("tcp://proxy:6665")



def adicionar(tarefa):
    try:
        hora = datetime.now().strftime("%H:%M")
        pub.send_string(f"canal {hora}_{tarefa}")
        sleep(0.3)
        return "mensagem enviada"
    except:
        return "erro ao enviar a mensagem"

while True:
    message = socket.recv_string()
    retorno = adicionar(message)

    socket.send_string(retorno)
