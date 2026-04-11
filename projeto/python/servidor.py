from time import sleep
import zmq
from datetime import datetime

context = zmq.Context()

# REP via broker para mensagens principais (REQ dos clientes)
msg_socket = context.socket(zmq.REP)
msg_socket.connect("tcp://broker:5556")

# REP via broker2 para comandos (login/canal/listar)
cmd_socket = context.socket(zmq.REP)
cmd_socket.connect("tcp://broker2:4446")

# PUB via proxy para broadcast
pub = context.socket(zmq.PUB)
pub.connect("tcp://proxy:6666")

canais = []
users = []

def printdata():
    return datetime.now().strftime("%d-%m-%y %H:%M:%S")

def create_canal(canal):
    if canal in canais:
        return "canal já existe"
    canais.append(canal)
    return "canal criado\n" + listar_canais()


def login_user(user):
    if user in users:
        return f"{printdata()} falha ao logar"
    users.append(user)
    return f"{printdata()} usuario logado"


def listar_canais():
    if not canais:
        return "nenhum canal cadastrado"
    return "\n".join(canais)


def broadcast(mensagem):
    try:
        hora = datetime.now().strftime("%H:%M")
        pub.send_string(f"canal {hora}_{mensagem}")
        return f"{printdata()} mensagem publicada"
    except Exception as e:
        print("Erro no publish:", e)
        return f"{printdata()} erro ao publicar"


poller = zmq.Poller()
poller.register(cmd_socket, zmq.POLLIN)
poller.register(msg_socket, zmq.POLLIN)

print("Servidor iniciado (REP cmd+msg, PUB proxy)...")

while True:
    eventos = dict(poller.poll())

    if cmd_socket in eventos:
        message = cmd_socket.recv_string()
        parts = message.split()
        cmd = parts[0].lower() if parts else ""
        arg = parts[1] if len(parts) > 1 else None

        if cmd == 'adiciona' and arg:
            resposta = create_canal(arg)
        elif cmd == 'logar' and arg:
            resposta = login_user(arg)
        elif cmd == 'lista':
            resposta = listar_canais()
        else:
            resposta = "comando invalido"

        cmd_socket.send_string(resposta)

    if msg_socket in eventos:
        mensagem = msg_socket.recv_string()
        resposta_broadcast = broadcast(mensagem)
        msg_socket.send_string(resposta_broadcast)
        # opcional: sleep para dar tempo a SUB conectar e receber
        sleep(0.05)

