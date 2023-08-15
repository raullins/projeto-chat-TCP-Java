import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Client implements Runnable{

    private Socket client;
    private BufferedReader in;
    private PrintWriter out;
    private boolean terminado;

    @Override
    public void run() {
        try {
            client = new Socket("127.0.0.1", 9999);
            out = new PrintWriter(client.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(client.getInputStream()));

            tratadorDeInput tratadorIn = new tratadorDeInput();
            Thread t = new Thread(tratadorIn);
            t.start();

            String mensagemIn;
            while ((mensagemIn = in.readLine()) != null){
                System.out.println(mensagemIn);
            }
        } catch (IOException e) {
            shutDown();
        }
    }

    public void shutDown(){
        terminado = true;
        try{
            in.close();
            out.close();
            if (!client.isClosed()){
                client.close();
            }
        } catch (IOException e){
            //ignorar
        }
    }

    class tratadorDeInput implements Runnable {

        @Override
        public void run() {
            try{
                BufferedReader leitorIn = new BufferedReader(new InputStreamReader(System.in));
                while (!terminado){
                    String mensagem = leitorIn.readLine();
                    if (mensagem.equals("/quit")){
                        out.println(mensagem);
                        leitorIn.close();
                        shutDown();
                    } else {
                        out.println(mensagem);
                    }
                }
            } catch (IOException e){
                shutDown();
            }
        }
        
    }

    public static void main(String[] args) {
        Client client = new Client();
        client.run();
    }
    
}
