/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package client;

import common.Request;
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
    private Request request;
    private boolean requestTreated;

    public ClientRequest(Client c, Request r) {
        this.client = c;
        this.request = r;
        this.requestTreated=false;
    }

    @Override
    public void run() {

        while (requestTreated) {
            
         
            client.setResponse(request(request, getServerPort(getRandomServer())));   
        }

        
    }

    private Request request(Request r, int port) {
        Request response = null;

        try {
            Socket socket = new Socket("localhost", port);
            ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
            oos.writeObject(r);
            oos.flush();

            ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
            response = (Request) ois.readObject();
        } catch (IOException ex) {
            System.err.println("Erro IO \n" + ex.getLocalizedMessage());

        } catch (ClassNotFoundException ex) {
            System.err.println("Erro na conversão da classe \n" + ex.getLocalizedMessage());
        }
        return response;
    }

    private int getServerPort(int serverID) {
        return Integer.parseInt(client.getProps().getServerAdress("srv" + serverID)[1]);
    }

    private int getRandomServer() {
        Random rnd = new Random();
        return rnd.nextInt(4);

    }
}
