import zmq
import time
import random
from datetime import datetime
from LogicalClock import LogicalClock

clock = LogicalClock()

def log(service, msg):
    print(f"[{datetime.now()}] [{service}] {msg}")

def main():
    context = zmq.Context()

    req = context.socket(zmq.REQ)
    req.connect("tcp://broker:5555")

    sub = context.socket(zmq.SUB)
    sub.connect("tcp://proxy:5558")

    poller = zmq.Poller()
    poller.register(sub, zmq.POLLIN)

    inscritos = set()

    time.sleep(2)

    while True:
        user = f"user_{random.randint(0, 99)}"

        # LOGIN
        req.send_string(f"{clock.increment()}|logar {user}")
        resp_login = req.recv_string()
        p_login = resp_login.split("|", 1)
        clock.update(int(p_login[0]))
        login = p_login[1]

        if login != "usuario logado":
            continue

        # LISTAR CANAIS
        req.send_string(f"{clock.increment()}|lista")
        resp_lista = req.recv_string()
        p_lista = resp_lista.split("|", 1)
        clock.update(int(p_lista[0]))
        resposta = p_lista[1]

        canais = []
        if resposta and resposta != "nenhum canal" and resposta.strip():
            lista = resposta.split("\n")
            for c in lista:
                if c.strip():
                    canais.append(c)

        # CRIAR CANAL SE NECESSÁRIO
        if len(canais) < 5:
            novo = f"canal_{random.randint(0, 998)}"
            req.send_string(f"{clock.increment()}|adiciona {novo}")
            req.recv_string()
            canais.append(novo)

        # INSCREVER EM CANAIS
        if len(inscritos) < 3 and canais:
            canal = random.choice(canais)
            if canal not in inscritos:
                sub.setsockopt_string(zmq.SUBSCRIBE, canal)
                inscritos.add(canal)
                log("CLIENTE", f"Inscrito em {canal}")

        # LOOP DE PUBLICAÇÃO
        for _ in range(10):
            if not canais:
                break

            canal = random.choice(canais)
            mensagem = f"msg_{random.randint(0, 998)}"

            # PUBLICAR
            req.send_string(f"{clock.increment()}|publica {canal} {mensagem}")
            req.recv_string()

            # RECEBER SUB
            socks = dict(poller.poll(100))
            if sub in socks and socks[sub] == zmq.POLLIN:
                msg = sub.recv_string()
                parts_clock = msg.split("|", 1)
                clock.update(int(parts_clock[0]))
                payload = parts_clock[1]
                log("SUB", payload)

            time.sleep(1)

if __name__ == "__main__":
    main()