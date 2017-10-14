/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package client;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 *
 * @author João
 */
public class ClientRequest implements Runnable {
    
    private Client client;
    private Request request;
    
    public ClientRequest (Client c, Request r){
        this.client = c;
        this.request = r;
    }

    @Override
    public void run() {

        client.setResponse(request(request));
        
    }

    private Request request(Request r) {
        Request response = null;
        try {
            Socket socket = new Socket("localhost", 233);
            ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
            oos.writeObject(r);
            oos.flush();
            
            ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
            response = (Request) ois.readObject();
        } 
        catch (IOException ex) {
            System.err.println("Erro IO \n" + ex.getLocalizedMessage());
        } 
        catch (ClassNotFoundException ex) {
            System.err.println("Erro na conversão da classe \n" + ex.getLocalizedMessage());
        }
        return response;
    }
    
}
