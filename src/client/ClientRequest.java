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

/**
 *
 * @author João
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
            ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
            oos.writeObject(m);
            oos.flush();
            
            ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
            response = (Message) ois.readObject();
            processResponse(response);

        } catch (IOException ex) {
            System.err.println("O servidor contactado pelo cliente " + client.getId() + " deu erro na leitura/escrita!" + ex.getLocalizedMessage());

        } catch (ClassNotFoundException ex) {
            System.err.println("Erro na conversão da classe \n" + ex.getLocalizedMessage());
        }
    }

    private void processResponse(Message response) {
        if (response != null) {
            if (response.getMessageType().equals("REJECT")) {
                client.setLeaderID(response.getContent());
                isFinishedRequest = false; 
            }
            else if (response.getMessageType().equals("RESPONSE")){
                client.setResponse(response);
                isFinishedRequest = true;
                isWrongLeader = true;
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
