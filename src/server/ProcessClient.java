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
import java.io.Serializable;
import java.net.Socket;
import java.util.Objects;

/**
 *
 * @author João
 */
public class ProcessClient implements Runnable, Serializable {

    private Server server;
    private Socket clientSocket;
    private boolean finishedConnection;

    public ProcessClient(Server s, Socket clientSocket) {
        this.server = s;
        this.clientSocket = clientSocket;
        this.finishedConnection = false;
    }

    @Override
    public void run() {

        try {


            /* Processar os pedidos do cliente */
            while (!finishedConnection) {

                ObjectInputStream dis = new ObjectInputStream(clientSocket.getInputStream());
                Message request = (Message) dis.readObject();
                Message response = processRequest(request);//executa o método que processa a mensagem
                System.out.println("Recebida msg. Sou o " + server.getServerID());

                if (response != null) {
                    sendMessageToClient(response);
                    System.out.println("Enviada msg");
                }
            }

        } catch (IOException ex) {
            System.err.println("Erro no estabelecimento da ligação com o cliente \n" + ex.getLocalizedMessage());
        } catch (ClassNotFoundException ex) {
            System.err.println("Erro na conversão da classe Request\n" + ex.getLocalizedMessage());
        }

    }

    public void sendMessageToClient(Message m) throws IOException {
        BufferedOutputStream bos = new BufferedOutputStream(clientSocket.getOutputStream());
        ObjectOutputStream osw = new ObjectOutputStream(bos);
        osw.writeObject(m);//Envia a mensagem
        osw.flush();
        if (finishedConnection) {
            osw.close();
            bos.close();
            clientSocket.close();//Fecha a ligação
        }
    }

    /* Este método decide o que fazer com uma mensagem:
    -> Se o servidor for o líder, coloca a mensagem numa fila para ser processada
    -> Se não for líder, responde a dizer que não é o líder e fornece ID do líder actual
     */
    private Message processRequest(Message request) {
        Message response = null;
        if (request != null) {
            System.out.println(server.getState());
            if (Objects.equals(server.getState(), "LEADER")) {
                server.addSocket(request.getSource(), this);
                server.appendMessageClientQueue(request);
                System.out.println("Cliente");

            } else {
                response = new Message(request.getId(), request.getSource(), "REJECT", server.getLeaderID());
                finishedConnection = true;
            }
        }
        return response;
    }
}
