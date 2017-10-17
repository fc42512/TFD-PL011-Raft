/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server;

import common.Message;
import common.PropertiesManager;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.concurrent.LinkedBlockingQueue;


/**
 *
 * @author João
 */
public class Server implements Runnable {
    
    private String serverID;
    private String leaderID;
    private PropertiesManager serversProps;
    private PropertiesManager clientsProps;
    private int ID_MESSAGE = 0;
    private String state;
    
    private LinkedBlockingQueue<Message> clientQueue;
    private LinkedBlockingQueue<Message> serverQueue;

    
    public Server(String id, PropertiesManager serversProps, PropertiesManager clientsProps){
        this.serverID = id;
        this.leaderID = "srv0";
        this.serversProps = serversProps;
        this.clientsProps = clientsProps;
        clientQueue = new LinkedBlockingQueue<>();
        serverQueue = new LinkedBlockingQueue<>();
        System.out.println("O servidor " + serverID + " arrancou!");
    }


    @Override
    public void run() {

                      
            /*Set State to Server */
            if(Integer.parseInt(serverID.substring(3)) == 0){
                state = "LEADER";
            }
            else{
                state = "FOLLOWER";
            }
            
            
            /* Criar Socket para escutar os clientes */
        try {
            ServerSocket socketForClients = new ServerSocket(Integer.parseInt(clientsProps.getServerAdress(serverID)[1]));
            socketForClients.setReuseAddress(true);

            /* Processar os pedidos dos clientes */
            while (true) {
                new Thread(new ProcessClient(this, socketForClients.accept())).start();
                
            }
        } 
        catch (IOException ex) {
            System.err.println("O servidor " + serverID + " não consegue ativar a sua ligação \n" + ex.getLocalizedMessage());
        }
    }
            


    public String getServerID() {
        return serverID;
    }

    public String getLeaderID() {
        return leaderID;
    }

    public int getIDMESSAGE() {
        return ID_MESSAGE;
    }
    
    public void incrementIDMessage() {
        ID_MESSAGE++;
    }


    public String getState() {
        return state;
    }

    public LinkedBlockingQueue<Message> getClientQueue() {
        return clientQueue;
    }

    public LinkedBlockingQueue<Message> getServerQueue() {
        return serverQueue;
    }

}

