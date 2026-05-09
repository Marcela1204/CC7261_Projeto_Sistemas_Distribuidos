import org.zeromq.ZMQ;
import java.util.*;
import java.io.*;
import java.nio.file.*;

public class ReferenceServer {

    private static Map<String, Integer> servidores = new HashMap<>();
    private static Map<String, Long> heartbeat = new HashMap<>();
    private static Set<String> channels = new TreeSet<>();
    private static int rankCounter = 1;

    private static final long TIMEOUT = 10000; // 10s
    private static final String CHANNELS_FILE = "reference_channels.txt";

    private static void loadChannels() {
        if (!Files.exists(Paths.get(CHANNELS_FILE))) {
            System.out.println("Nenhum arquivo de canais de referência encontrado.");
            return;
        }
        try (BufferedReader reader = Files.newBufferedReader(Paths.get(CHANNELS_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (!line.isEmpty()) {
                    channels.add(line);
                }
            }
            System.out.println("Canais de referência carregados: " + channels);
        } catch (IOException e) {
            System.out.println("Erro ao carregar canais de referência: " + e.getMessage());
        }
    }

    private static void saveChannels() {
        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(CHANNELS_FILE))) {
            for (String canal : channels) {
                writer.write(canal);
                writer.newLine();
            }
        } catch (IOException e) {
            System.out.println("Erro ao salvar canais de referência: " + e.getMessage());
        }
    }

    private static boolean mergeChannels(Collection<String> newChannels) {
        boolean updated = false;
        for (String canal : newChannels) {
            canal = canal.trim();
            if (!canal.isEmpty() && !channels.contains(canal)) {
                channels.add(canal);
                updated = true;
            }
        }
        if (updated) {
            saveChannels();
        }
        return updated;
    }

    private static String channelListResponse() {
        if (channels.isEmpty()) {
            return "nenhum canal";
        }
        return String.join("\n", channels);
    }

    public static void main(String[] args) {

        loadChannels();

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
            String[] parts = msg.split(" ", 3);
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
                rep.send("OK");

            } else if (cmd.equals("GET_CHANNELS")) {

                rep.send(channelListResponse());

            } else if (cmd.equals("CHANNEL_UPDATE")) {

                String canal = parts.length > 2 ? parts[2] : "";
                if (!canal.isEmpty()) {
                    boolean added = channels.add(canal);
                    if (added) {
                        saveChannels();
                    }
                    rep.send("OK");
                } else {
                    rep.send("ERROR");
                }

            } else if (cmd.equals("SYNC_CHANNELS")) {

                if (parts.length > 2 && !parts[2].isEmpty()) {
                    String[] newChannels = parts[2].split(",");
                    mergeChannels(Arrays.asList(newChannels));
                }
                rep.send(channelListResponse());

            }
        }
    }
}