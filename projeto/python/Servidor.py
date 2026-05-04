import zmq
import time
import threading
import uuid
import os
from datetime import datetime
from LogicalClock import LogicalClock

canais = []
users = []
relogio_logico = LogicalClock()
server_name = None
coordenador = None
mensagens_desde_sync = 0
SYNC_INTERVAL = 15

ALL_SERVERS = ["servidor-1", "servidor-2"]

def formatar_hora(timestamp):
    dt = datetime.fromtimestamp(timestamp / 1000.0)
    return dt.strftime("%H:%M:%S.%f")[:-3]

def log(msg):
    print(f"[{datetime.now()}] [{server_name}] {msg}")

def main():
    global server_name, coordenador, mensagens_desde_sync

    hostname = os.getenv("HOSTNAME")
    if hostname and (hostname.startswith("servidor-") or hostname.startswith("servidor_")):
        server_name = hostname
    else:
        server_name = f"servidor-{str(uuid.uuid4())[:8]}"

    log("Iniciando servidor...")

    context = zmq.Context()

    rep = context.socket(zmq.REP)
    rep.connect("tcp://broker:5556")

    pub = context.socket(zmq.PUB)
    pub.connect("tcp://proxy:5557")

    ref = context.socket(zmq.REQ)
    ref.connect("tcp://reference:6000")

    ref.send_string(f"REGISTER {server_name}")
    rank = ref.recv_string()
    log(f"Registrado: {rank}")

    time.sleep(5)

    # ELEIÇÃO
    ordenados = sorted(ALL_SERVERS)
    coordenador = ordenados[0]
    sou_coord = coordenador == server_name

    log(f"SERVIDORES: {ALL_SERVERS}")
    log(f"COORDENADOR ELEITO: {coordenador}")
    log(f"EU SOU COORDENADOR? {sou_coord}")

    pub.send_string(f"{relogio_logico.increment()}|servers {coordenador}")

    coord_socket = None
    if sou_coord:
        coord_socket = context.socket(zmq.REP)
        coord_socket.bind("tcp://*:5998")
        log("COORDENADOR: Ouvindo requisições de hora na porta 5998")

    eleicao_socket = context.socket(zmq.REP)
    eleicao_socket.bind("tcp://*:5999")

    def eleicao_thread():
        while True:
            msg = eleicao_socket.recv_string()
            if msg:
                eleicao_socket.send_string("OK")

    threading.Thread(target=eleicao_thread, daemon=True).start()

    if sou_coord:
        def coord_thread():
            while True:
                req = coord_socket.recv_string()
                if req == "REQ_HORA":
                    hora = int(time.time() * 1000)
                    coord_socket.send_string(f"REP_HORA|{hora}")
                    log(f"Forneci hora: {formatar_hora(hora)}")

        threading.Thread(target=coord_thread, daemon=True).start()

    log("Servidor pronto para processar mensagens")

    while True:
        mensagem = rep.recv_string()
        partes_clock = mensagem.split("|", 1)
        clock_recebido = int(partes_clock[0])
        comando = partes_clock[1]

        relogio_logico.update(clock_recebido)

        tokens = comando.split(" ", 2)
        cmd = tokens[0]
        resposta = ""

        if cmd == "logar":
            resposta = "usuario logado"
        elif cmd == "adiciona":
            canal = tokens[1]
            if canal not in canais:
                canais.append(canal)
            resposta = "\n".join(canais)
        elif cmd == "lista":
            resposta = "nenhum canal" if not canais else "\n".join(canais)
        elif cmd == "publica":
            canal = tokens[1]
            msg = tokens[2]
            timestamp = str(datetime.now())
            pub.send_string(f"{relogio_logico.increment()}|{canal} {timestamp} {msg}")
            log(f"Publicado em {canal}: {msg}")
            resposta = "OK"

        rep.send_string(f"{relogio_logico.increment()}|{resposta}")

        mensagens_desde_sync += 1

        if mensagens_desde_sync % 10 == 0:
            ref.send_string(f"HEARTBEAT {server_name}")
            hb_resp = ref.recv_string()
            log(f"Heartbeat: {hb_resp}")
            log(f"Coordenador atual: {coordenador}")

        if mensagens_desde_sync % SYNC_INTERVAL == 0 and not sou_coord:
            log("INICIANDO SINCRONIZAÇÃO BERKELEY...")

            sync_client = context.socket(zmq.REQ)
            sync_client.setsockopt(zmq.RCVTIMEO, 3000)

            try:
                sync_client.connect(f"tcp://{coordenador}:5998")

                t1 = int(time.time() * 1000)
                sync_client.send_string("REQ_HORA")
                resposta_hora = sync_client.recv_string()
                t2 = int(time.time() * 1000)

                if resposta_hora and resposta_hora.startswith("REP_HORA|"):
                    hora_coord = int(resposta_hora.split("|")[1])
                    rtt = t2 - t1
                    hora_ajustada = hora_coord + (rtt // 2)
                    diferenca = hora_ajustada - int(time.time() * 1000)

                    log(f"Hora do coordenador: {formatar_hora(hora_coord)}")
                    log(f"RTT: {rtt}ms")
                    log(f"Hora ajustada:     {formatar_hora(hora_ajustada)}")
                    log(f"Diferença: {diferenca:+}ms")
                    log(f"Relógio lógico: {relogio_logico.get_time()}")

            except Exception as e:
                log(f"ERRO: Coordenador {coordenador} falhou!")
                log("Iniciando nova eleição...")

                idx = ALL_SERVERS.index(coordenador)
                if idx >= 0 and idx + 1 < len(ALL_SERVERS):
                    coordenador = ALL_SERVERS[idx + 1]
                else:
                    coordenador = ALL_SERVERS[0]
                log(f"NOVO COORDENADOR: {coordenador}")
                pub.send_string(f"{relogio_logico.increment()}|servers {coordenador}")

            sync_client.close()

if __name__ == "__main__":
    main()