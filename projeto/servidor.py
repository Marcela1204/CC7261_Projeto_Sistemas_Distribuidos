import zmq

canais = []
users = []

context = zmq.Context()
socket = context.socket(zmq.REP)
socket.connect("tcp://broker:5556")

def create(canal):
    canais.append(canal)
    return "tarefa adicionada\n" + listar()
def createlogin(user):
    if user in users:
        return "falha ao logar"
    else:
        users.append(user)
        return "usuario logado"

# def remove(tarefa):
#     try:
#         tarefas.remove(tarefa)
#         return "tarefa removida\n" + listar()
#     except ValueError:
#         return f"a tarefa {tarefa} não existe"


def listar():
    payload = ""
    for i in canais:
        payload += (i + "\n")
    print(payload)
    return payload

while True:
    message = socket.recv_string()
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

