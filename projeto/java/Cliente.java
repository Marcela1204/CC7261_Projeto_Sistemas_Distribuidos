import org.zeromq.ZMQ;
import java.util.*;

import java.time.LocalDateTime;

public class Cliente {

    public static void log(String service, String msg) {
        System.out.println("[" + LocalDateTime.now() + "] [" + service + "] " + msg);
    }

    public static void main(String[] args) throws InterruptedException {

        ZMQ.Context context = ZMQ.context(1);

        ZMQ.Socket req = context.socket(ZMQ.REQ);
        req.connect("tcp://broker:5555");

        ZMQ.Socket sub = context.socket(ZMQ.SUB);
        sub.connect("tcp://proxy:5558");

        ZMQ.Poller poller = context.poller(1);
        poller.register(sub, ZMQ.Poller.POLLIN);

        Random random = new Random();
        Set<String> inscritos = new HashSet<>();

        Thread.sleep(2000);

        while (true) {

            String user = "user_" + random.nextInt(100);
            req.send("logar " + user);
            String login = req.recvStr();

            if (!login.equals("usuario logado")) continue;

            // LISTAR CANAIS
            req.send("lista");
            String resposta = req.recvStr();

            List<String> canais = new ArrayList<>();
            if (!resposta.equals("nenhum canal")) {
                canais = Arrays.asList(resposta.split("\n"));
            }

            // CRIAR CANAL SE < 5
            if (canais.size() < 5) {
                String novo = "canal_" + random.nextInt(999);
                req.send("adiciona " + novo);
                req.recvStr();
                canais.add(novo);
            }

            // INSCREVER ATÉ 3
            if (inscritos.size() < 3 && !canais.isEmpty()) {
                String canal = canais.get(random.nextInt(canais.size()));
                if (!inscritos.contains(canal)) {
                    sub.subscribe(canal.getBytes(ZMQ.CHARSET));
                    inscritos.add(canal);
                    log("CLIENTE", "Inscrito em " + canal);
                }
            }

            // LOOP DE ENVIO
            for (int i = 0; i < 10; i++) {

                if (canais.isEmpty()) break;

                String canal = canais.get(random.nextInt(canais.size()));
                String mensagem = "msg_" + random.nextInt(999);

                req.send("publica " + canal + " " + mensagem);
                req.recvStr();

                // RECEBER
                if (poller.poll(100) > 0) {
                    if (poller.pollin(0)) {

                        String msg = sub.recvStr();

                        String[] parts = msg.split(" ", 3);

                        String canalMsg = parts[0];
                        String envio = parts[1];
                        String conteudo = parts[2];

                        String recebimento = LocalDateTime.now().toString();

                        log("SUB",
                                "Canal: " + canalMsg +
                                " | Enviado: " + envio +
                                " | Recebido: " + recebimento +
                                " | Msg: " + conteudo
                        );
                    }
                }

                Thread.sleep(1000);
            }
        }
    }
}