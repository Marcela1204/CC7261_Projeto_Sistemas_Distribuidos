import org.zeromq.ZMQ;
import java.util.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.io.FileWriter;

public class Servidor {

    private static List<String> canais = new ArrayList<>();
    private static List<String> users = new ArrayList<>();

    public static void log(String service, String msg) {
        String now = LocalDateTime.now().toString();
        System.out.println("[" + now + "] [" + service + "] " + msg);
    }

    public static void salvarLog(String tipo, String conteudo) {
        try {
            FileWriter fw = new FileWriter("log.txt", true);
            fw.write(tipo + ";" + conteudo + ";" + LocalDateTime.now() + "\n");
            fw.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String create(String canal) {
        if (!canais.contains(canal)) {
            canais.add(canal);
            log("SERVIDOR", "Canal criado: " + canal);
        }
        return listar();
    }

    public static String createLogin(String user) {
        if (users.contains(user)) {
            return "falha ao logar";
        }
        users.add(user);
        return "usuario logado";
    }

    public static String listar() {
        if (canais.isEmpty()) return "nenhum canal";

        return String.join("\n", canais);
    }

    public static String publicar(ZMQ.Socket pub, String canal, String mensagem) {
        try {
            String timestamp = LocalDateTime.now().toString();

            String payload = canal + " " + timestamp + " " + mensagem;

            pub.send(payload.getBytes(ZMQ.CHARSET), 0);

            salvarLog("PUB", payload);

            log("SERVIDOR", "Publicado: " + payload);

            return "OK";

        } catch (Exception e) {
            return "ERRO";
        }
    }

    public static void main(String[] args) {

        ZMQ.Context context = ZMQ.context(1);

        ZMQ.Socket rep = context.socket(ZMQ.REP);
        rep.connect("tcp://broker:5556");

        ZMQ.Socket pub = context.socket(ZMQ.PUB);
        pub.connect("tcp://proxy:5557");

        log("SERVIDOR", "Servidor iniciado");

        while (true) {

            String message = rep.recvStr();
            salvarLog("REQ", message);

            String[] parts = message.split(" ", 3);

            String cmd = parts[0];
            String arg = parts.length > 1 ? parts[1] : null;
            String extra = parts.length > 2 ? parts[2] : null;

            String response;

            if (cmd.equals("logar")) {
                response = createLogin(arg);

            } else if (cmd.equals("adiciona")) {
                response = create(arg);

            } else if (cmd.equals("lista")) {
                response = listar();

            } else if (cmd.equals("publica")) {
                response = publicar(pub, arg, extra);

            } else {
                response = "comando invalido";
            }

            rep.send(response);
        }
    }
}