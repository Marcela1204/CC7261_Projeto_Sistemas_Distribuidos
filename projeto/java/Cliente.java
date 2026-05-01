import org.zeromq.ZMQ;
import java.util.*;
import java.time.LocalDateTime;

public class Cliente {

    private static LogicalClock clock = new LogicalClock();

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

            // LOGIN
            req.send(clock.increment() + "|" + "logar " + user);

            String respLogin = req.recvStr();
            String[] pLogin = respLogin.split("\\|", 2);

            clock.update(Integer.parseInt(pLogin[0]));
            String login = pLogin[1];

            if (!login.equals("usuario logado")) continue;

            // LISTAR CANAIS
            req.send(clock.increment() + "|" + "lista");
            String respLista = req.recvStr();

            String[] pLista = respLista.split("\\|", 2);
            clock.update(Integer.parseInt(pLista[0]));
            String resposta = pLista[1];

            
            List<String> canais = new ArrayList<>();

            if (resposta != null && !resposta.equals("nenhum canal") && !resposta.trim().isEmpty()) {
                String[] lista = resposta.split("\n");

                for (String c : lista) {
                    if (!c.trim().isEmpty()) {
                        canais.add(c);
                    }
                }
            }

            // CRIAR CANAL SE NECESSÁRIO
            if (canais.size() < 5) {
                String novo = "canal_" + random.nextInt(999);

                req.send(clock.increment() + "|" + "adiciona " + novo);
                req.recvStr();

                // adiciona localmente
                canais.add(novo);
            }

            // INSCREVER EM CANAIS
            if (inscritos.size() < 3 && !canais.isEmpty()) {
                String canal = canais.get(random.nextInt(canais.size()));

                if (!inscritos.contains(canal)) {
                    sub.subscribe(canal.getBytes(ZMQ.CHARSET));
                    inscritos.add(canal);
                    log("CLIENTE", "Inscrito em " + canal);
                }
            }

            // LOOP DE PUBLICAÇÃO
            for (int i = 0; i < 10; i++) {

                if (canais.isEmpty()) break;

                String canal = canais.get(random.nextInt(canais.size()));
                String mensagem = "msg_" + random.nextInt(999);

                // PUBLICAR
                req.send(clock.increment() + "|" + "publica " + canal + " " + mensagem);
                req.recvStr();

                // RECEBER SUB
                if (poller.poll(100) > 0 && poller.pollin(0)) {

                    String msg = sub.recvStr();

                    String[] partsClock = msg.split("\\|", 2);
                    clock.update(Integer.parseInt(partsClock[0]));

                    String payload = partsClock[1];

                    log("SUB", payload);
                }

                Thread.sleep(1000);
            }
        }
    }
}