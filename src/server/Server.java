/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server;

import client.Request;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 *
 * @author João
 */
public class Server implements Runnable {
    
    private static int ID_MESSAGE;
    private int port;
    
    public Server(int port){
        this.ID_MESSAGE = 0;
        this.port = port;
    }


    @Override
    public void run() {
        try {
            ServerSocket serverSocket = new ServerSocket(port);
            

            while (true) {
                Socket clienteSocket = serverSocket.accept();
                
                ObjectInputStream dis = new ObjectInputStream(clienteSocket.getInputStream());
                Request request = (Request) dis.readObject();
                request.setContent("Sucesso - atríbuído o ID Mensagem " + ID_MESSAGE);
                ID_MESSAGE++;
                
                ObjectOutputStream dos = new ObjectOutputStream(clienteSocket.getOutputStream());
                dos.writeObject(request);
            }
        }
        catch (IOException ex) {
            System.err.println("Erro IO \n" + ex.getLocalizedMessage());
        } 
        catch (ClassNotFoundException ex) {
            System.err.println("Erro na conversão da classe \n" + ex.getLocalizedMessage());
        }
    }
}

