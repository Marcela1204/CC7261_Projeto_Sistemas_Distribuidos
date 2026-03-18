import org.zeromq.ZMQ;
import java.util.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Servidor {

    private static List<String> canais = new ArrayList<>();
    private static List<String> users = new ArrayList<>();

    public static void log(String service, String msg) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String now = LocalDateTime.now().format(formatter);
        System.out.println("[" + now + "] [" + service + "] " + msg);
    }

    public static String create(String canal) {
        canais.add(canal);
        log("SERVIDOR", "Canal criado: " + canal);
        return "tarefa adicionada\n" + listar();
    }

    public static String createLogin(String user) {
        if (users.contains(user)) {
            log("SERVIDOR", "Login falhou: " + user);
            return "falha ao logar";
        } else {
            users.add(user);
            log("SERVIDOR", "Usuario logado: " + user);
            return "usuario logado";
        }
    }

    public static String listar() {
        StringBuilder payload = new StringBuilder();
        for (String c : canais) {
            payload.append(c).append("\n");
        }
        log("SERVIDOR", "Lista de canais:\n" + payload);
        return payload.toString();
    }

    public static void main(String[] args) {
        ZMQ.Context context = ZMQ.context(1);
        ZMQ.Socket socket = context.socket(ZMQ.REP);

        socket.connect("tcp://broker:5556");

        while (true) {
            String message = socket.recvStr();
            log("SERVIDOR", "Mensagem recebida: " + message);

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