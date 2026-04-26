import org.zeromq.ZMQ;
import java.util.*;

public class ReferenceServer {

    private static Map<String, Integer> servidores = new HashMap<>();
    private static Map<String, Long> heartbeat = new HashMap<>();
    private static int rankCounter = 1;

    private static final long TIMEOUT = 10000; // 10s

    public static void main(String[] args) {

        ZMQ.Context context = ZMQ.context(1);
        ZMQ.Socket rep = context.socket(ZMQ.REP);
        rep.bind("tcp://*:6000");

        System.out.println("Reference Server iniciado");

        // Thread para remover servidores mortos
        new Thread(() -> {
            while (true) {
                long now = System.currentTimeMillis();

                servidores.keySet().removeIf(nome -> {
                    long last = heartbeat.getOrDefault(nome, 0L);
                    return (now - last) > TIMEOUT;
                });

                try { Thread.sleep(2000); } catch (Exception e) {}
            }
        }).start();

        while (true) {

            String msg = rep.recvStr();
            String[] parts = msg.split(" ");
            String cmd = parts[0];

            if (cmd.equals("REGISTER")) {

                String nome = parts[1];

                if (!servidores.containsKey(nome)) {
                    servidores.put(nome, rankCounter++);
                }

                heartbeat.put(nome, System.currentTimeMillis());

                rep.send("RANK " + servidores.get(nome));

            } else if (cmd.equals("LIST")) {

                StringBuilder sb = new StringBuilder();

                for (String nome : servidores.keySet()) {
                    sb.append(nome)
                      .append(":")
                      .append(servidores.get(nome))
                      .append(",");
                }

                rep.send(sb.toString());

            } else if (cmd.equals("HEARTBEAT")) {

                String nome = parts[1];
                heartbeat.put(nome, System.currentTimeMillis());

                long now = System.currentTimeMillis();

                rep.send("OK " + now);
            }
        }
    }
}