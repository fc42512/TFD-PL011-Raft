/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package client;

import common.Message;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Objects;

/**
 *
 * @author TFD-GRUPO11-17/18
 */
public class ClientRequest {

    private Client client;
    private boolean isFinishedRequest;
    private boolean isWrongLeader;

    public ClientRequest(Client c) {
        this.client = c;
        this.isFinishedRequest = false;
        this.isWrongLeader = false;
    }

    public void request(Message m, Socket socket) {
        Message response = null;
        try {
            System.out.println("A preparar para enviar pedido");
            ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
            oos.writeObject(m);
            oos.flush();
            System.out.println("Enviado pedido");
            
            ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
            response = (Message) ois.readObject();
            processResponse(response);
            System.out.println("Recebido pedido de volta");

        } catch (IOException ex) {
            System.err.println("O servidor contactado pelo cliente " + client.getId() + " deu erro na leitura/escrita!" + ex.getLocalizedMessage());
            isFinishedRequest = false;
            client.setLeaderID(null);

        } catch (ClassNotFoundException ex) {
            System.err.println("Erro na conversão da classe \n" + ex.getLocalizedMessage());
        }
    }

    private void processResponse(Message response) {
        if (response != null) {
            if (Objects.equals(response.getMessageType(),"REJECT")) {
                client.setLeaderID(response.getContent());
                isFinishedRequest = false;
                isWrongLeader = true;
//                System.out.println("O seu pedido não foi tratado, por favor repita a operação!");
            }
            else if (Objects.equals(response.getMessageType(),"RESPONSE")){
                client.setResponse(response);
                isFinishedRequest = true;
                
            }
        }
    }

    public boolean isFinishedRequest() {
        return isFinishedRequest;
    }

    public boolean isWrongLeader() {
        return isWrongLeader;
    }
    
    
}
