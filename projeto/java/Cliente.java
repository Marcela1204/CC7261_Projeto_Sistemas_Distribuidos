import org.zeromq.ZMQ;
import java.util.Random;

public class Cliente {

    public static void main(String[] args) throws InterruptedException {

        ZMQ.Context context = ZMQ.context(1);
        ZMQ.Socket socket = context.socket(ZMQ.REQ);

        socket.connect("tcp://broker:5555");

        String[] acoes = {"adiciona", "lista"};
        Random random = new Random();

        try {
            while (true) {
                String user = "user_" + (random.nextInt(99) + 1);
                socket.send("logar " + user);

                String respostaLogin = socket.recvStr();

                if (respostaLogin.equals("usuario logado")) {

                    String acao = acoes[random.nextInt(acoes.length)];
                    String canal = "canal_" + (random.nextInt(999) + 1);

                    String mensagem = acao + " " + canal;

                    socket.send(mensagem);
                    String resposta = socket.recvStr();

                    System.out.println(resposta);

                    Thread.sleep(500);

                } else {
                    System.out.println("falha no login, usuario ja logado");
                    Thread.sleep(500);
                }
            }

        } finally {
            socket.close();
            context.close();
        }
    }
}