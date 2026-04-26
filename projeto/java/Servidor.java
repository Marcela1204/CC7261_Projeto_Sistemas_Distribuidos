import org.zeromq.ZMQ;
import java.util.*;
import java.time.LocalDateTime;
import java.io.FileWriter;

public class Servidor {

    private static List<String> canais = new ArrayList<>();
    private static List<String> users = new ArrayList<>();

    private static LogicalClock clock = new LogicalClock();
    private static int msgCount = 0;
    private static String serverName = "server_" + UUID.randomUUID();

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
        if (!canais.contains(canal)) canais.add(canal);
        return listar();
    }

    public static String createLogin(String user) {
        if (users.contains(user)) return "falha ao logar";
        users.add(user);
        return "usuario logado";
    }

    public static String listar() {
        if (canais.isEmpty()) return "nenhum canal";
        return String.join("\n", canais);
    }

    public static String publicar(ZMQ.Socket pub, String canal, String mensagem) {
        try {
            int sendClock = clock.increment();

            String timestamp = LocalDateTime.now().toString();
            String payload = canal + " " + timestamp + " " + mensagem;

            pub.send(sendClock + "|" + payload);

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

        // Reference
        ZMQ.Socket ref = context.socket(ZMQ.REQ);
        ref.connect("tcp://reference:6000");

        // REGISTER
        ref.send("REGISTER " + serverName);
        String rank = ref.recvStr();
        log("SERVIDOR", "Registrado: " + rank);

        log("SERVIDOR", "Servidor iniciado");

        while (true) {

            String message = rep.recvStr();

            // clock|mensagem
            String[] split = message.split("\\|", 2);
            int receivedClock = Integer.parseInt(split[0]);
            String payload = split[1];

            clock.update(receivedClock);

            salvarLog("REQ", payload);

            String[] parts = payload.split(" ", 3);

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

            int sendClock = clock.increment();
            rep.send(sendClock + "|" + response);

            // HEARTBEAT
            msgCount++;
            if (msgCount % 10 == 0) {
                ref.send("HEARTBEAT " + serverName);
                String resp = ref.recvStr();

                String[] p = resp.split(" ");
                long millis = Long.parseLong(p[1]);

                String dataFormatada = java.time.Instant.ofEpochMilli(millis)
                        .atZone(java.time.ZoneId.systemDefault())
                        .toLocalDateTime()
                        .toString();

                log("SERVIDOR", "Heartbeat OK | Hora ref: " + dataFormatada);
            }
        }
    }
}