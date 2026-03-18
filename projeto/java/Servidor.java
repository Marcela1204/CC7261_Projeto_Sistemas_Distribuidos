import org.zeromq.ZMQ;
import java.util.*;

public class Servidor {

    private static List<String> canais = new ArrayList<>();
    private static List<String> users = new ArrayList<>();

    public static String create(String canal) {
        canais.add(canal);
        return "tarefa adicionada\n" + listar();
    }

    public static String createLogin(String user) {
        if (users.contains(user)) {
            return "falha ao logar";
        } else {
            users.add(user);
            return "usuario logado";
        }
    }

    public static String listar() {
        StringBuilder payload = new StringBuilder();
        for (String c : canais) {
            payload.append(c).append("\n");
        }
        System.out.println(payload.toString());
        return payload.toString();
    }

    public static void main(String[] args) {
        ZMQ.Context context = ZMQ.context(1);
        ZMQ.Socket socket = context.socket(ZMQ.REP);

        socket.connect("tcp://broker:5556");

        while (true) {
            String message = socket.recvStr();
            String[] parts = message.split(" ");

            String cmd = parts.length > 0 ? parts[0].toLowerCase() : "";
            String arg = parts.length > 1 ? parts[1] : null;

            String response;

            if (cmd.equals("adiciona") && arg != null) {
                response = create(arg);
            } else if (cmd.equals("logar") && arg != null) {
                response = createLogin(arg);
            } else {
                response = listar();
            }

            socket.send(response);
        }
    }
}