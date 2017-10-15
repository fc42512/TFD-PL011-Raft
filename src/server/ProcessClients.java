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
public class ProcessClients implements Runnable {

    private Server server;
    private ServerSocket socketForClients;
    private Socket clientSocket;
    private int ID_MESSAGE = 0;

    public ProcessClients(Server server) {
        this.server = server;
    }

    @Override
    public void run() {

        /* Criar Socket para escutar os clientes */
        try {
            socketForClients = new ServerSocket(Integer.parseInt(server.getClientsProps().getServerAdress(server.getServerID())[1]));
            socketForClients.setReuseAddress(true);

            /* Processar os pedidos dos clientes */
            while (true) {
                clientSocket = socketForClients.accept();
                ObjectInputStream dis = new ObjectInputStream(clientSocket.getInputStream());
                Message request = (Message) dis.readObject();
                Message response = processRequest(request);//executa o método que processa a mensagem
//                System.out.println("Recebida msg");

                if (response != null) {
                    sendMessageToClient(response, clientSocket);
//                    System.out.println("Enviada msg");
                }
            }

        } catch (IOException ex) {
            System.err.println("Erro no estabelecimento da ligação com o cliente \n" + ex.getLocalizedMessage());
        } catch (ClassNotFoundException ex) {
            System.err.println("Erro na conversão da classe Request\n" + ex.getLocalizedMessage());
        }

    }

    private void sendMessageToClient(Message m, Socket s) throws IOException {
        BufferedOutputStream bos = new BufferedOutputStream(s.getOutputStream());
        ObjectOutputStream osw = new ObjectOutputStream(bos);
        osw.writeObject(m);//Envia a mensaem
        osw.flush();
        osw.close();
        bos.close();
        s.close();//Fecha a ligação
    }
    
    /* Este método decide o que fazer com uma mensagem:
    -> Se o servidor for o líder, coloca a mensagem numa fila para ser processada
    -> Se não for líder, responde a dizer que não é o líder e fornece ID do líder actual
    */
    private Message processRequest(Message request) {
        Message response = null;
        if (request != null) {
            if (server.getState().equals("LEADER")) {
                server.getClientQueue().add(request);

//                response = new Message(request.getId(), "RESPONSE", "Sucesso - atribuído o ID " + ID_MESSAGE);
//                ID_MESSAGE++;
            } else {
                response = new Message(request.getId(), "REJECT", server.getLeaderID());
            }
        }
        return response;
    }
}
