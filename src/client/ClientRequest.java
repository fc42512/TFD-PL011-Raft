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
import java.util.Random;

/**
 *
 * @author João
 */
public class ClientRequest implements Runnable {

    private Client client;
    private Message request;
    private boolean finishedRequest;
    private String serverContacted;

    public ClientRequest(Client c, Message r) {
        this.client = c;
        this.request = r;
        this.finishedRequest = false;
    }

    @Override
    public void run() {
        while (!finishedRequest) {
            if (client.getLeaderID() != null) {
                serverContacted = client.getLeaderID();
                request(request, getServerPort(client.getLeaderID()));
            } else {
                serverContacted = getRandomServer();
                request(request, getServerPort(serverContacted));
            }
        }
    }

    private void request(Message r, int port) {
        Message response = null;

        try {
            Socket socket = new Socket("localhost", port);
            ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
            oos.writeObject(r);
            oos.flush();
            

            ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
            response = (Message) ois.readObject();
            processResponse(response);

        } catch (IOException ex) {
            System.err.println("Erro IO \n" + ex.getLocalizedMessage());

        } catch (ClassNotFoundException ex) {
            System.err.println("Erro na conversão da classe \n" + ex.getLocalizedMessage());
        }
    }

    private void processResponse(Message response) {
        if (response != null) {
            if (response.getMessageType().equals("REJECT")) {
                client.setLeaderID(response.getContent());
                finishedRequest = false; 
            }
            else if (response.getMessageType().equals("RESPONSE")){
                client.setResponse(response);
                client.setLeaderID(serverContacted);
                finishedRequest = true;
            }
        }
    }

    private int getServerPort(String serverID) {
        return Integer.parseInt(client.getProps().getServerAdress(serverID)[1]);
    }

    private String getRandomServer() {
        Random rnd = new Random();
        return "srv" + rnd.nextInt(3);

    }
}
