import org.zeromq.ZMQ;
import java.util.Random;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Cliente {

    public static void log(String service, String msg) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String now = LocalDateTime.now().format(formatter);
        System.out.println("[" + now + "] [" + service + "] " + msg);
    }

    public static void main(String[] args) throws InterruptedException {

        ZMQ.Context context = ZMQ.context(1);
        ZMQ.Socket socket = context.socket(ZMQ.REQ);

        socket.connect("tcp://broker:5555");

        String[] acoes = {"adiciona", "lista"};
        Random random = new Random();

        Thread.sleep(2000);

        while (true) {
            String user = "user_" + (random.nextInt(99) + 1);

            log("CLIENTE", "Tentando logar: " + user);
            socket.send("logar " + user);

            String respostaLogin = socket.recvStr();
            log("CLIENTE", "Resposta login: " + respostaLogin);

            if (respostaLogin.equals("usuario logado")) {

                String acao = acoes[random.nextInt(acoes.length)];
                String canal = "canal_" + (random.nextInt(999) + 1);
                String mensagem = acao + " " + canal;

                log("CLIENTE", "Enviando comando: " + mensagem);

                socket.send(mensagem);
                String resposta = socket.recvStr();

                log("CLIENTE", "Resposta servidor:\n" + resposta);

                Thread.sleep(500);

            } else {
                log("CLIENTE", "falha no login");
                Thread.sleep(500);
            }
        }
    }
}