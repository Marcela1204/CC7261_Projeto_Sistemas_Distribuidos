package chat;

import org.zeromq.ZMQ;
import java.util.*;

public class Server {

    static Set<String> users = new HashSet<>();
    static Set<String> channels = new HashSet<>();

    public static void main(String[] args) throws Exception {

        ZMQ.Context context = ZMQ.context(1);
        ZMQ.Socket socket = context.socket(ZMQ.REP);

        socket.bind("tcp://*:5556");

        System.out.println("Servidor Java iniciado");

        while(true){

            byte[] msg = socket.recv();

            Request req = Request.parseFrom(msg);

            System.out.println("Recebido: " + req);

            Response.Builder resp = Response.newBuilder();

            resp.setTimestamp(System.currentTimeMillis());

            switch(req.getType()){

                case "login":

                    users.add(req.getUsername());

                    resp.setSuccess(true);
                    resp.setMessage("login ok");

                    break;

                case "create_channel":

                    if(channels.contains(req.getChannel())){

                        resp.setSuccess(false);
                        resp.setMessage("canal existe");

                    } else {

                        channels.add(req.getChannel());

                        resp.setSuccess(true);
                        resp.setMessage("canal criado");

                    }

                    break;

                case "list_channels":

                    resp.setSuccess(true);
                    resp.addAllChannels(channels);

                    break;
            }

            socket.send(resp.build().toByteArray());
        }
    }
}