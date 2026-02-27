import grpc
import helloworld_pb2
import helloworld_pb2_grpc
from time import sleep

print("Cliente conectando com servidor", flush=True)

porta = "50051"
endereco = "servidor"

with grpc.insecure_channel(f"{endereco}:{porta}") as channel:
    stub = helloworld_pb2_grpc.GreeterStub(channel)
    while True:
        resposta = stub.HelloWorld(helloworld_pb2.MsgCliente(mensagem="ola"))
        print(f"Resposta do servidor: {resposta.mensagem}", flush=True)
        sleep(0.5)
