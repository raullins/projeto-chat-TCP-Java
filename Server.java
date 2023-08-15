import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server implements Runnable{

    private ArrayList<tratadorDeConexoes> listaDeConexoes;
    private ServerSocket server;
    private Boolean terminado;
    private ExecutorService pool;

    public Server(){
        listaDeConexoes = new ArrayList<>();
        terminado = false;
    }

    @Override
    public void run(){
        try {
            server = new ServerSocket(9999);
            pool = Executors.newCachedThreadPool();
            while(!terminado){
                Socket client = server.accept();
                tratadorDeConexoes tratador = new tratadorDeConexoes(client);
                listaDeConexoes.add(tratador);
                pool.execute(tratador);
            } 
        } catch (Exception e) {
            shutDown();
        }
    }

    private void shutDown() {
        try{
            terminado = true;
            pool.shutdown();
            if (!server.isClosed()){
                server.close();
            }

            for (tratadorDeConexoes ch : listaDeConexoes){
                ch.shutDown();
            }
        } catch (IOException e){
            //TODO handle
        }
        
    }

    class tratadorDeConexoes implements Runnable{

        private Socket client;
        private BufferedReader in;
        private PrintWriter out;
        private String apelido;

        public tratadorDeConexoes (Socket client){
            this.client = client;
        }

        @Override
        public void run() {
            try {
                out = new PrintWriter(client.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(client.getInputStream()));

                //out.println(); Para mandar uma mensagem para o cliente
                //in.readLine(); Para ler uma mensagem do cliente
                out.println("Por favor, digite um apelido: ");
                apelido = in.readLine();

                System.out.println(apelido + " está conectado!");
                broadcast(apelido + " juntou-se ao chat!");

                String mensagem;
                while((mensagem = in.readLine()) != null){
                    if (mensagem.startsWith("/quit", 0)){
                        broadcast(apelido + " saiu do chat");
                        shutDown();
                    } else if ((mensagem.startsWith("/apelido", 0))){
                        String[] mensagemDividida = mensagem.split(" ", 2);
                        if (mensagemDividida.length == 2){
                            broadcast(apelido + " mudou seu apelido para " + mensagemDividida[1]);
                            System.out.println(apelido + " mudou seu apelido para " + mensagemDividida[1]);
                            apelido = mensagemDividida[1];
                            out.println("Sucesso ao mudar apelido para: " + mensagemDividida[1]);
                        } else {
                            out.println("Apelido novo não foi fornecido");
                        }
                    } else {
                        broadcast(apelido + ": " + mensagem);
                    }
                }
            } catch (IOException e){
                shutDown();
            }
        }

        public void broadcast(String mensagem){
            for (tratadorDeConexoes ch : listaDeConexoes){
                if (ch != null){
                    ch.mandarMensagem(mensagem);
                }
            }
        }

        public void mandarMensagem(String mensagem){
            out.println(mensagem);
        }

        public void shutDown(){
            try {

                in.close();
                out.close();
                if (!client.isClosed()){
                    client.close();
                }

            } catch (Exception e) {}
           
            }

        }

        public static void main(String[] args) {

            Server server = new Server();
            server.run();
            
        }

    }


