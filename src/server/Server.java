/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server;

import common.Message;
import common.PropertiesManager;
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
            new Thread(new ProcessClients(this)).start();
            
 
        
    }

    public String getServerID() {
        return serverID;
    }

    public String getLeaderID() {
        return leaderID;
    }
    
    public PropertiesManager getServersProps() {
        return serversProps;
    }

    public PropertiesManager getClientsProps() {
        return clientsProps;
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

