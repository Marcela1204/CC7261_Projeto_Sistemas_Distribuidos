import zmq
import time
import json
import os
import chat_pb2

context = zmq.Context()
socket = context.socket(zmq.REP)
socket.bind("tcp://*:5555")

DATA_FILE = "data.json"

if not os.path.exists(DATA_FILE):
    with open(DATA_FILE, "w") as f:
        json.dump({"users": [], "channels": []}, f)

def load_data():
    with open(DATA_FILE) as f:
        return json.load(f)

def save_data(data):
    with open(DATA_FILE, "w") as f:
        json.dump(data, f)

print("Servidor Python iniciado")

while True:

    msg = socket.recv()

    req = chat_pb2.Request()
    req.ParseFromString(msg)

    print("Recebido:", req)

    data = load_data()

    resp = chat_pb2.Response()
    resp.timestamp = int(time.time())

    if req.type == "login":

        if req.username not in data["users"]:
            data["users"].append(req.username)
            save_data(data)

        resp.success = True
        resp.message = "login ok"

    elif req.type == "create_channel":

        if req.channel in data["channels"]:
            resp.success = False
            resp.message = "canal já existe"
        else:
            data["channels"].append(req.channel)
            save_data(data)
            resp.success = True
            resp.message = "canal criado"

    elif req.type == "list_channels":

        resp.success = True
        resp.channels.extend(data["channels"])

    socket.send(resp.SerializeToString())