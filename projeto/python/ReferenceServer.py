import zmq
import time
import threading

servidores = {}
heartbeat = {}
rank_counter = 1

TIMEOUT = 10000  # 10s

def main():
    global rank_counter
    context = zmq.Context()
    rep = context.socket(zmq.REP)
    rep.bind("tcp://*:6000")

    print("Reference Server iniciado")

    # Thread para remover servidores mortos
    def cleanup_thread():
        while True:
            now = int(time.time() * 1000)
            to_remove = [nome for nome, last in heartbeat.items() if now - last > TIMEOUT]
            for nome in to_remove:
                del servidores[nome]
                del heartbeat[nome]
            time.sleep(2)

    threading.Thread(target=cleanup_thread, daemon=True).start()

    while True:
        msg = rep.recv_string()
        parts = msg.split()
        cmd = parts[0]

        if cmd == "REGISTER":
            nome = parts[1]
            if nome not in servidores:
                servidores[nome] = rank_counter
                rank_counter += 1
            heartbeat[nome] = int(time.time() * 1000)
            rep.send_string(f"RANK {servidores[nome]}")

        elif cmd == "LIST":
            sb = ",".join(f"{nome}:{rank}" for nome, rank in servidores.items())
            rep.send_string(sb)

        elif cmd == "HEARTBEAT":
            nome = parts[1]
            heartbeat[nome] = int(time.time() * 1000)
            rep.send_string("OK")

if __name__ == "__main__":
    main()