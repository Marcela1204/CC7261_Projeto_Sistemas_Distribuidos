import zmq

def main():
    context = zmq.Context()

    xsub = context.socket(zmq.XSUB)
    xsub.bind("tcp://*:5557")

    xpub = context.socket(zmq.XPUB)
    xpub.bind("tcp://*:5558")

    print("Proxy iniciado")

    zmq.proxy(xsub, xpub, None)

if __name__ == "__main__":
    main()