import zmq

def main():
    context = zmq.Context()

    frontend = context.socket(zmq.ROUTER)
    frontend.bind("tcp://*:5555")

    backend = context.socket(zmq.DEALER)
    backend.bind("tcp://*:5556")

    zmq.proxy(frontend, backend, None)

    frontend.close()
    backend.close()
    context.term()

if __name__ == "__main__":
    main()