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
public class Follower implements Runnable {

    private Server server;
    private ServerSocket serverSocket;

    public Follower(Server s, ServerSocket ss) {
        this.server = s;
        this.serverSocket = ss;
    }

    @Override
    public void run() {
        try {
            Socket liderSocket = serverSocket.accept();

            /* Processar os pedidos do líder */
            while (true) {

                ObjectInputStream dis = new ObjectInputStream(liderSocket.getInputStream());
                Message request = (Message) dis.readObject();
                Message response = processRequest(request);//executa o método que processa a mensagem
                System.out.println("Recebida msg");

                if (response != null) {
                    sendMessageToLeader(response, liderSocket);
                    System.out.println("Enviada msg");
                }
            }

        } catch (IOException ex) {
            System.err.println(server.getServerID() + " - Erro no estabelecimento da ligação com o líder \n" + ex.getLocalizedMessage());
        } catch (ClassNotFoundException ex) {
            System.err.println("Erro na conversão da classe Request\n" + ex.getLocalizedMessage());
        }
    }

    private void sendMessageToLeader(Message m, Socket s) throws IOException {
        BufferedOutputStream bos = new BufferedOutputStream(s.getOutputStream());
        ObjectOutputStream osw = new ObjectOutputStream(bos);
        osw.writeObject(m);//Envia a mensagem
        osw.flush();
    }

    private Message processRequest(Message request) {
        Message response = null;
        if (request != null) {
            response = new Message(request.getId(), request.getSource(), "RESPONSE", "Sucesso - mensagem recebida pelo follower " + server.getServerID());

        }
        return response;
    }
}

