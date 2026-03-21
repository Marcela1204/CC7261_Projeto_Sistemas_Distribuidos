import zmq

context = zmq.Context()

client_socket = context.socket(zmq.ROUTER)
client_socket.bind("tcp://*:4445")

server_socket = context.socket(zmq.DEALER)
server_socket.bind("tcp://*:4446")

print("Broker iniciado...")

# 🔥 proxy automático
zmq.proxy(client_socket, server_socket)
