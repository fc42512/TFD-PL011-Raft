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
import common.OperationType;

/**
 *
 * @author TFD-GRUPO11-17/18
 */
public class Client {

    private final int id;
    private final PropertiesManager props;
    private Socket socket;
    private int ID_REQUEST;
    private final ClientRequest clientRequest;
    private String leaderID;
    private Message request;
    private Message response;
    private boolean connectionAlive;

    public Client(int id, PropertiesManager props) {
        this.id = id;
        this.props = props;
        this.ID_REQUEST = 0;
        this.leaderID = null;
        this.response = null;
        this.connectionAlive = false;
        this.clientRequest = new ClientRequest(this);

        System.out.println("O cliente " + id + " arrancou!");

    }

    private void establishConnection() {
        while (!connectionAlive) {
            try {
                if (leaderID == null) {
                    leaderID = getRandomServer();
                    socket = new Socket(getServerIP(leaderID), getServerPort(leaderID));
                    this.connectionAlive = true;
                } else if (clientRequest.isWrongLeader()) {
                    socket.close();
                    socket = new Socket(getServerIP(leaderID), getServerPort(leaderID));
                    this.connectionAlive = true;
                }
            } catch (IOException ex) {
                System.err.println("O servidor " + leaderID + " contactado pelo cliente " + id + " não está disponível! \n" + ex.getLocalizedMessage());
                leaderID = null;
                this.connectionAlive = false;
            }
        }
    }
    
    public void sendRequest(OperationType opType, String value){
        if(!connectionAlive){
            establishConnection();
        }
        if (response != null) {
            ID_REQUEST++;
            request = new Message("CL" + id + "-RQ" + ID_REQUEST, id, "REQUEST", opType, "CL" + id + "-KEY", value);
        }
        else {
            if(ID_REQUEST == 0){
               request = new Message("CL" + id + "-RQ" + ID_REQUEST, id, "REQUEST", opType, "CL" + id + "-KEY", value); 
            }
        }
        clientRequest.request(request, socket);
        while(!clientRequest.isFinishedRequest()){
            connectionAlive = false;
            establishConnection();
            clientRequest.request(request, socket);
        }
    }

    public int getId() {
        return id;
    }

    public void setLeaderID(String leaderID) {
        this.leaderID = leaderID;
    }

    public void setResponse(Message response) {
        this.response = response;
        System.out.println(response.getContent());
    }

    private String getServerIP(String serverID) {
        return props.getServerAdress(serverID)[0];
    }
    
    private int getServerPort(String serverID) {
        return Integer.parseInt(props.getServerAdress(serverID)[1]);
    }

    private String getRandomServer() {
        Random rnd = new Random();
        return "srv" + rnd.nextInt(props.getHashMapProperties().size());
    }
}
