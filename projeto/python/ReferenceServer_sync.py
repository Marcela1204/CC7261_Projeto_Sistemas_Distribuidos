import zmq
import time
import threading
import os

servidores = {}
heartbeat = {}
rank_counter = 1
channels = set()

TIMEOUT = 10000  # 10s
CHANNELS_FILE = "reference_channels.txt"


def load_channels():
    if not os.path.exists(CHANNELS_FILE):
        print("Nenhum arquivo de canais de referência encontrado.")
        return
    with open(CHANNELS_FILE, "r") as f:
        for line in f:
            canal = line.strip()
            if canal:
                channels.add(canal)
    print(f"Canais de referência carregados: {sorted(channels)}")


def save_channels():
    with open(CHANNELS_FILE, "w") as f:
        for canal in sorted(channels):
            f.write(canal + "\n")


def merge_channels(new_channels):
    updated = False
    for canal in new_channels:
        canal = canal.strip()
        if canal and canal not in channels:
            channels.add(canal)
            updated = True
    if updated:
        save_channels()
    return updated


def channel_list_response():
    if not channels:
        return "nenhum canal"
    return "\n".join(sorted(channels))


def main():
    global rank_counter
    load_channels()

    context = zmq.Context()
    rep = context.socket(zmq.REP)
    rep.bind("tcp://*:6000")

    print("Reference Server iniciado")

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
        parts = msg.split(" ", 2)
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

        elif cmd == "GET_CHANNELS":
            rep.send_string(channel_list_response())

        elif cmd == "CHANNEL_UPDATE":
            canal = parts[2] if len(parts) > 2 else ""
            if canal:
                added = canal not in channels
                channels.add(canal)
                if added:
                    save_channels()
                rep.send_string("OK")
            else:
                rep.send_string("ERROR")

        elif cmd == "SYNC_CHANNELS":
            if len(parts) > 2 and parts[2]:
                new_channels = parts[2].split(",")
                merge_channels(new_channels)
            rep.send_string(channel_list_response())

        else:
            rep.send_string("UNKNOWN_COMMAND")


if __name__ == "__main__":
    main()
