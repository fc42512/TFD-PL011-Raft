/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server;

import common.Message;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 *
 * @author João
 */
public class ProcessServers implements Runnable {

    private Server server;
    private ServerSocket socketForServers;
    private Socket serverSocket;

    @Override
    public void run() {

        /* Criar Socket para escutar os servidores */
        try {
            socketForServers = new ServerSocket(Integer.parseInt(server.getServersProps().getServerAdress(server.getServerID())[1]));
            socketForServers.setReuseAddress(true);

            /* Processar os mensagens dos servidores */
            while (true) {
                serverSocket = socketForServers.accept();
                ObjectInputStream dis = new ObjectInputStream(serverSocket.getInputStream());
                Message message = (Message) dis.readObject();
                processMessageFromServer(message);//executa o método que processa a mensagem


            }

        } catch (IOException ex) {
            System.err.println("Erro no estabelecimento da ligação com o cliente \n" + ex.getLocalizedMessage());
        } catch (ClassNotFoundException ex) {
            System.err.println("Erro na conversão da classe Request\n" + ex.getLocalizedMessage());
        }
    }

    public void sendMessageToServer(Message m, Socket s) throws IOException {
        BufferedOutputStream bos = new BufferedOutputStream(s.getOutputStream());
        ObjectOutputStream osw = new ObjectOutputStream(bos);
        osw.writeObject(m);//Envia a mensagem
        osw.flush();
        osw.close();
        bos.close();
        s.close();//Fecha a ligação
    }
    
    private void processMessageFromServer(Message m){
        
    }
}
