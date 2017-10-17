/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package client;

import common.PropertiesManager;
import common.Message;
import java.io.IOException;
import java.net.Socket;
import java.util.Random;

/**
 *
 * @author João
 */
public class Client implements Runnable {

    private int id;
    private PropertiesManager props;
    private Socket socket;
    private int ID_REQUEST;
    private String leaderID;
    private Message request;
    private Message response;

    public Client(int id, PropertiesManager props) {
        this.id = id;
        this.props = props;
        ID_REQUEST = 0;
        leaderID = null;
        response = null;
        
        System.out.println("O cliente " + id + " arrancou!");
        

    }

    @Override
    public void run() {
        ClientRequest cr;

        while (true) {
            cr = new ClientRequest(this);
            setRequest();
            while (!cr.isFinishedRequest()) {
                try {
                    if (leaderID == null) {
                        leaderID = getRandomServer();
                        socket = new Socket("localhost", getServerPort(leaderID));
                    } else if (cr.isWrongLeader()) {
                        socket.close();
                        socket = new Socket("localhost", getServerPort(leaderID));
                    }
                    cr.request(request, socket);
                } 
                catch (IOException ex) {
                    System.err.println("O servidor " + leaderID +" contactado pelo cliente " + id + " não está disponível! \n" + ex.getLocalizedMessage());
                    leaderID = null;
                }
            }
        }
    }

    public int getId() {
        return id;
    }

    public void setLeaderID(String leaderID) {
        this.leaderID = leaderID;
    }
    
    private void setRequest(){
        if (response == null) {
            request = new Message("CL" + id + "-RQ" + ID_REQUEST, "", "");
        } else {
            ID_REQUEST++;
            request = new Message("CL" + id + "-RQ" + ID_REQUEST, "", "");
        }
    }
    
    public void setResponse(Message response) {
        this.response = response;
        System.out.println(response.getId() + " " + response.getContent());
    }

    private int getServerPort(String serverID) {
        return Integer.parseInt(props.getServerAdress(serverID)[1]);
    }

    private String getRandomServer() {
        Random rnd = new Random();
        return "srv" + rnd.nextInt(3);

    }

}
