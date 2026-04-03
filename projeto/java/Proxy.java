import org.zeromq.ZMQ;

public class Proxy {
    public static void main(String[] args) {

        ZMQ.Context context = ZMQ.context(1);

        ZMQ.Socket xsub = context.socket(ZMQ.XSUB);
        xsub.bind("tcp://*:5557");

        ZMQ.Socket xpub = context.socket(ZMQ.XPUB);
        xpub.bind("tcp://*:5558");

        System.out.println("Proxy iniciado");

        ZMQ.proxy(xsub, xpub, null);
    }
}