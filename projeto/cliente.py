import zmq
from time import sleep
import random

context = zmq.Context()

#NOTE: seção do requester para mensagens
socket = context.socket(zmq.REQ)
socket.connect("tcp://broker:5555")
print("request iniciado")

#NOTE: seção do subscriber
sub = context.socket(zmq.SUB)
sub.setsockopt_string(zmq.SUBSCRIBE, "canal")
sub.connect("tcp://proxy:6665")
print("subscriber iniciado")

#NOTE: seção criador de canais
socketChan = context.socket(zmq.REQ)
socketChan.connect("tcp://broker2:4445")
print("canais iniciado")

acoes = ["adiciona", "lista"]

# Poller para subscriber (não bloqueante)
poller = zmq.Poller()
poller.register(sub, zmq.POLLIN)

try:
    while True:
        # Tenta logar com usuário aleatório
        socketChan.send_string(f"logar user_{random.randint(1,99)}")
        resposta_login = socketChan.recv_string()

        if resposta_login == "usuario logado":
            print(f"✓ {resposta_login}")

            # Escolhe ação aleatória
            acao = random.choice(acoes)
            if acao == "adiciona":
                canal = f"canal_{random.randint(1,999)}"
                socketChan.send_string(f"adiciona {canal}")
                resposta = socketChan.recv_string()
                print(f"✓ {resposta}")
            else:  # lista
                socketChan.send_string("lista")
                resposta = socketChan.recv_string()
                print(f"✓ Canais: {resposta}")

            # Envia mensagem para publicar
            mensagem = f"mensagem_{random.randint(1,999)}"
            socket.send_string(mensagem)
            resposta_pub = socket.recv_string()
            print(f"✓ {resposta_pub}")

            # Verifica se há mensagens do subscriber (não bloqueante)
            eventos = dict(poller.poll(timeout=100))  # 100ms timeout
            if sub in eventos:
                mensagens = sub.recv_string()
                print(f"📨 Recebido: {mensagens}")

            sleep(0.5)
        else:
            print(f"✗ {resposta_login}")
            sleep(0.5)

except KeyboardInterrupt:
    print("\nCliente interrompido")
finally:
    socket.close()
    sub.close()
    socketChan.close()
    context.term()


