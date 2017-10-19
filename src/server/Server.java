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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

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

    private int currentTerm;
    private String votedFor; //candidateId que recebeu o voto no termo atual (null se não há)
    private ArrayList<LogEntry> log;
    private int commitIndex;//indice do último log entry commitado
    private int lastApplied;//indice do último log entry executado pela máquina de estados

    private Thread leaderThread;
    private LinkedList<Message> clientQueue;
    private HashMap<Integer, Message> stateMachine;

    public Server(String id, PropertiesManager serversProps, PropertiesManager clientsProps) {
        this.serverID = id;
        this.leaderID = "srv0";
        this.serversProps = serversProps;
        this.clientsProps = clientsProps;
        System.out.println("O servidor " + serverID + " arrancou!");

        this.currentTerm = 0;
        this.votedFor = null;
        this.log = new ArrayList<LogEntry>();
        this.commitIndex = 0;
        this.lastApplied = 0;

        this.leaderThread = null;
        clientQueue = new LinkedList<>();

    }

    @Override
    public void run() {

        /* Criar Socket para escutar os clientes */
        try {
            ServerSocket socketForClients = new ServerSocket(Integer.parseInt(clientsProps.getServerAdress(serverID)[1]));
            socketForClients.setReuseAddress(true);

            /* Criar Socket para escutar os servidores */
            ServerSocket socketForServers = new ServerSocket(Integer.parseInt(serversProps.getServerAdress(serverID)[1]));
            socketForClients.setReuseAddress(true);

            /*Set State to Server */
            if (Integer.parseInt(serverID.substring(3)) == 0) {
                state = "LEADER";

                leaderThread = new Thread(new Leader(this, getNextIndex()));
                leaderThread.start();

                String serverToTalk;
                for (int i = 0; i < serversProps.getHashMapProperties().size(); i++) {
                    serverToTalk = "srv" + i;
                    if (!serverID.equals(serverToTalk)) {
                        new Thread(new TalkToFollower(this, Integer.parseInt(serversProps.getServerAdress(serverToTalk)[1]))).start();
                    }
                }

            } else {
                state = "FOLLOWER";
                new Thread(new Follower(this, socketForServers)).start();
            }

            while (true) {
                /* Processar os pedidos dos clientes */
                new Thread(new ProcessClient(this, socketForClients.accept())).start();

//                /* Processar os pedidos dos servidores */
//                new Thread(new ProcessClient(this, socketForServers.accept())).start();
            }
        } catch (IOException ex) {
            System.err.println("O servidor " + serverID + " não consegue ativar a sua ligação \n" + ex.getLocalizedMessage());
        }
    }

    /**
     * *******************************************************************
     ****************** GETTER's e SETTER's*******************************
     ********************************************************************
     */
    public String getServerID() {
        return serverID;
    }

    public String getLeaderID() {
        return leaderID;
    }

    public int getIDMESSAGE() {
        return ID_MESSAGE;
    }

    public String getState() {
        return state;
    }

    public PropertiesManager getServersProps() {
        return serversProps;
    }

    public int getCurrentTerm() {
        return currentTerm;
    }

    public Thread getLeaderThread() {
        return leaderThread;
    }

    /**
     * *******************************************************************
     ********************* MÉTODOS SERVIDOR ******************************
     ********************************************************************
     */
    public void incrementIDMessage() {
        ID_MESSAGE++;
    }

    public void incrementCurrentTerm() {
        currentTerm++;
    }

    public int getCurrentLogIndex() {
        if (log.size() == 0) {
            return 0;
        } else {
            return log.size() - 1;
        }
    }

    public void appendLogEntry(LogEntry logEntry) {
        log.add(logEntry);
    }

    public void appendMessageClientQueue(Message m) {
        clientQueue.addLast(m);
    }
    
    private HashMap<String, Integer> getNextIndex() {
        HashMap<String, Integer> nextIndex = new HashMap<>();
        for(int i=0; i<serversProps.getHashMapProperties().size(); i++){
            nextIndex.put("srv"+i, getCurrentLogIndex());
        }
        return nextIndex;
    }

    public void execute(int clientId, Message value) {
        stateMachine.put(clientId, value);
    }
}
