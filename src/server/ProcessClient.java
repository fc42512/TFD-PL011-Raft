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
import java.net.Socket;

/**
 *
 * @author João
 */
public class ProcessClient implements Runnable {

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
        osw.writeObject(m);//Envia a mensagem
        osw.flush();
        if (finishedConnection) {
            osw.close();
            bos.close();
            s.close();//Fecha a ligação
        }
    }

    /* Este método decide o que fazer com uma mensagem:
    -> Se o servidor for o líder, coloca a mensagem numa fila para ser processada
    -> Se não for líder, responde a dizer que não é o líder e fornece ID do líder actual
     */
    private Message processRequest(Message request) {
        Message response = null;
        if (request != null) {
            if (server.getState().equals("LEADER")) {
                server.appendMessageClientQueue(request);
                Thread lt = server.getLeaderThread();
                synchronized (lt) {
                    try {
                        System.out.println("Aguarda a resposta do líder...");
                        lt.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    response = new Message(request.getId(), request.getSource(), "RESPONSE", request.getContent() + " Sucesso - atribuído o ID " + server.getIDMESSAGE());
                    server.incrementIDMessage();
                }
            } else {
                response = new Message(request.getId(), request.getSource(), "REJECT", server.getLeaderID());
                finishedConnection = true;
            }
        }
        return response;
    }
}
