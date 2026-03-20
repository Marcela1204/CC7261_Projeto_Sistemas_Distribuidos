import zmq
from datetime import datetime

def log(service, msg):
    now = datetime.now().strftime("%Y-%m-%d %H:%M:%S")
    print(f"[{now}] [{service}] {msg}")

canais = []
users = []

can = open("canais.txt","w")
canais.append(can.readlines())

context = zmq.Context()
socket = context.socket(zmq.REP)
socket.connect("tcp://broker:5556")

def create(canal):
    canais.append(canal)
    log("SERVIDOR", f"Canal criado: {canal}")
    return "tarefa adicionada\n" + listar()

def createlogin(user):
    if user in users:
        log("SERVIDOR", f"Falha no login: {user}")
        return "falha ao logar"
    else:
        users.append(user)
        log("SERVIDOR", f"Usuario logado: {user}")
        return "usuario logado"

def listar():
    payload = ""
    for i in canais:
        payload += (i + "\n")
    log("SERVIDOR", f"Lista de canais:\n{payload}")
    return payload

while True:
    message = socket.recv_string()
    log("SERVIDOR", f"Mensagem recebida: {message}")

    parts = message.split()
    cmd = parts[0].lower() if parts else ""
    arg = parts[1] if len(parts) > 1 else None

    if cmd == 'adiciona' and arg:
        response = create(arg)
    elif cmd == 'logar' and arg:
        response = createlogin(arg)
    else:
        response = listar()

    socket.send_string(response)
    can.write(listar())
