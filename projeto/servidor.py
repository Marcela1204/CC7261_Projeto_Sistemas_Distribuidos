from time import sleep
import zmq
from datetime import datetime


#NOTE: seção do reply
context = zmq.Context()
socket = context.socket(zmq.REP)
socket.connect("tcp://broker:5556")

#NOTE: seção do publisher 
context = zmq.Context()
pub = context.socket(zmq.PUB)
pub.connect("tcp://proxy:6666")

#NOTE: Seção do canal
context = zmq.Context()
socketChan = context.socket(zmq.REP)
socketChan.connect("tcp://broker:4446")

canais = []
users = []

can = open("canais.txt","r+")
canais.append(can.readlines())


def create(canal):
    canais.append(canal)
    return "tarefa adicionada\n" + listar()
def createlogin(user):
    if user in users:
        return "falha ao logar"
    else:
        users.append(user)
        return "usuario logado"
def listar():
    payload = ""
    for i in canais:
        payload += (i + "\n")
    print(payload)
    return payload



def adicionar(tarefa):
    message = socketChan.recv_string()
    parts = message.split()
    cmd = parts[0].lower() if parts else ""
    arg = parts[1] if len(parts) > 1 else None

    if cmd == 'adiciona' and arg:
        response = create(arg)
    elif cmd == 'logar' and arg:
        response = createlogin(arg)
    else:
        response = listar()

    socketChan.send_string(response)
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
