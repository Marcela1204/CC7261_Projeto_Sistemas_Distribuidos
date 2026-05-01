import org.zeromq.ZMQ;
import java.util.*;
import java.time.LocalDateTime;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.io.FileWriter;

public class Servidor {

    private static List<String> canais = new ArrayList<>();
    private static List<String> users = new ArrayList<>();
    private static LogicalClock relogioLogico = new LogicalClock();
    private static String serverName;
    private static String coordenador = null;
    private static int mensagensDesdeSync = 0;
    private static final int SYNC_INTERVAL = 15;
    
    private static final List<String> ALL_SERVERS = Arrays.asList(
        "servidor-1", "servidor-2"
    );

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");

    public static String formatarHora(long timestamp) {
        return Instant.ofEpochMilli(timestamp)
                .atZone(java.time.ZoneId.systemDefault())
                .format(FORMATTER);
    }

    public static void log(String msg) {
        System.out.println("[" + LocalDateTime.now() + "] [" + serverName + "] " + msg);
    }

    public static void main(String[] args) throws Exception {
        
        String hostname = System.getenv("HOSTNAME");
        if (hostname != null && (hostname.startsWith("servidor-") || hostname.startsWith("servidor_"))) {
            serverName = hostname;
        } else {
            serverName = "servidor-" + UUID.randomUUID().toString().substring(0, 8);
        }
        
        log("Iniciando servidor...");

        ZMQ.Context context = ZMQ.context(1);

        ZMQ.Socket rep = context.socket(ZMQ.REP);
        rep.connect("tcp://broker:5556");

        ZMQ.Socket pub = context.socket(ZMQ.PUB);
        pub.connect("tcp://proxy:5557");

        ZMQ.Socket ref = context.socket(ZMQ.REQ);
        ref.connect("tcp://reference:6000");

        ref.send("REGISTER " + serverName);
        String rank = ref.recvStr();
        log("Registrado: " + rank);

        Thread.sleep(5000);

        // ELEIÇÃO
        List<String> ordenados = new ArrayList<>(ALL_SERVERS);
        Collections.sort(ordenados);
        coordenador = ordenados.get(0);
        final boolean souCoord = coordenador.equals(serverName);
        
        //log("==========================================");
        log("SERVIDORES: " + ALL_SERVERS);
        log("COORDENADOR ELEITO: " + coordenador);
        log("EU SOU COORDENADOR? " + souCoord);
        //log("==========================================");
        
        pub.send(relogioLogico.increment() + "|servers " + coordenador);

        final ZMQ.Socket coordSocket;
        if (souCoord) {
            coordSocket = context.socket(ZMQ.REP);
            coordSocket.bind("tcp://*:5998");
            log("COORDENADOR: Ouvindo requisições de hora na porta 5998");
        } else {
            coordSocket = null;
        }

        final ZMQ.Socket eleicaoSocket = context.socket(ZMQ.REP);
        eleicaoSocket.bind("tcp://*:5999");
        
        new Thread(() -> {
            while (true) {
                String msg = eleicaoSocket.recvStr();
                if (msg != null) {
                    eleicaoSocket.send("OK");
                }
            }
        }).start();

        if (souCoord) {
            new Thread(() -> {
                while (true) {
                    String req = coordSocket.recvStr();
                    if (req != null && req.equals("REQ_HORA")) {
                        long hora = System.currentTimeMillis();
                        coordSocket.send("REP_HORA|" + hora);
                        log("Forneci hora: " + formatarHora(hora));
                    }
                }
            }).start();
        }

        log("Servidor pronto para processar mensagens");

        while (true) {
            String mensagem = rep.recvStr();
            String[] partesClock = mensagem.split("\\|", 2);
            int clockRecebido = Integer.parseInt(partesClock[0]);
            String comando = partesClock[1];
            
            relogioLogico.update(clockRecebido);
            
            String[] tokens = comando.split(" ", 3);
            String cmd = tokens[0];
            String resposta = "";
            
            if (cmd.equals("logar")) {
                resposta = "usuario logado";
            } else if (cmd.equals("adiciona")) {
                String canal = tokens[1];
                if (!canais.contains(canal)) canais.add(canal);
                resposta = String.join("\n", canais);
            } else if (cmd.equals("lista")) {
                resposta = canais.isEmpty() ? "nenhum canal" : String.join("\n", canais);
            } else if (cmd.equals("publica")) {
                String canal = tokens[1];
                String msg = tokens[2];
                String timestamp = LocalDateTime.now().toString();
                pub.send(relogioLogico.increment() + "|" + canal + " " + timestamp + " " + msg);
                log("Publicado em " + canal + ": " + msg);
                resposta = "OK";
            }
            
            rep.send(relogioLogico.increment() + "|" + resposta);
            
            mensagensDesdeSync++;
            
            if (mensagensDesdeSync % 10 == 0) {
                ref.send("HEARTBEAT " + serverName);
                String hbResp = ref.recvStr();
                log("Heartbeat: " + hbResp);
                log("Coordenador atual: " + coordenador);
            }
            
            
            if (mensagensDesdeSync % SYNC_INTERVAL == 0 && !souCoord) {
                log("INICIANDO SINCRONIZAÇÃO BERKELEY...");
                
                ZMQ.Socket syncClient = context.socket(ZMQ.REQ);
                syncClient.setReceiveTimeOut(3000);
                
                try {
                    syncClient.connect("tcp://" + coordenador + ":5998");
                    
                    long t1 = System.currentTimeMillis();
                    syncClient.send("REQ_HORA");
                    String respostaHora = syncClient.recvStr();
                    long t2 = System.currentTimeMillis();
                    
                    if (respostaHora != null && respostaHora.startsWith("REP_HORA|")) {
                        long horaCoord = Long.parseLong(respostaHora.split("\\|")[1]);
                        long rtt = t2 - t1;
                        long horaAjustada = horaCoord + (rtt / 2);
                        long diferenca = horaAjustada - System.currentTimeMillis();
                        

                        log("Hora do coordenador: " + formatarHora(horaCoord));
                        log("RTT: " + rtt + "ms");
                        log("Hora ajustada:     " + formatarHora(horaAjustada));
                        log("Diferença: " + (diferenca >= 0 ? "+" : "") + diferenca + "ms");
                        log("Relógio lógico: " + relogioLogico.getTime());
                    }
                    
                } catch (Exception e) {
                    log("ERRO: Coordenador " + coordenador + " falhou!");
                    log("Iniciando nova eleição...");
                    
                    int idx = ALL_SERVERS.indexOf(coordenador);
                    if (idx >= 0 && idx + 1 < ALL_SERVERS.size()) {
                        coordenador = ALL_SERVERS.get(idx + 1);
                    } else {
                        coordenador = ALL_SERVERS.get(0);
                    }
                    log("NOVO COORDENADOR: " + coordenador);
                    pub.send(relogioLogico.increment() + "|servers " + coordenador);
                }
                syncClient.close();
            }
        }
    }
}