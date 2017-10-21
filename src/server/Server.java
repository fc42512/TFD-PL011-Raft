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
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
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

    private int currentTerm;
    private String votedFor; //candidateId que recebeu o voto no termo atual (null se não há)
    private ArrayList<LogEntry> log;
    private int commitIndex;//indice do último log entry commitado
    private int lastApplied;//indice do último log entry executado pela máquina de estados

    private Thread leaderThread;
    private LinkedBlockingQueue<Message> clientQueue;
    private LinkedBlockingQueue<AppendEntry> serverQueue;
    private HashMap<Integer, Message> stateMachine;
    private HashMap<Integer, ProcessClient> clientSockets;

    public Server(String id, PropertiesManager serversProps, PropertiesManager clientsProps) {
        this.serverID = id;
        this.leaderID = "srv0";
        this.serversProps = serversProps;
        this.clientsProps = clientsProps;
        System.out.println("O servidor " + serverID + " arrancou!");

        this.currentTerm = 0;
        this.votedFor = null;
        this.log = new ArrayList<LogEntry>();
        this.commitIndex = -1;
        this.lastApplied = 0;

        this.leaderThread = null;
        clientQueue = new LinkedBlockingQueue<>();
        serverQueue = new LinkedBlockingQueue<>();
        stateMachine = new HashMap<>();
        clientSockets = new HashMap<>();
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
                System.out.println("Verifica se é líder...");
                leaderThread = new Thread(new Leader(this, getNextIndex()));
                leaderThread.start();

//                String serverToTalk;
//                for (int i = 0; i < serversProps.getHashMapProperties().size(); i++) {
//                    serverToTalk = "srv" + i;
//                    if (!serverID.equals(serverToTalk)) {
//                        new Thread(new TalkToFollower(this, i, Integer.parseInt(serversProps.getServerAdress(serverToTalk)[1]))).start();
//                    }
//                }

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

    public void setLeaderID(String leaderID) {
        this.leaderID = leaderID;
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

    public void setCurrentTerm(int currentTerm) {
        this.currentTerm = currentTerm;
    }

    public int getCommitIndex() {
        return commitIndex;
    }

    public void setCommitIndex(int commitIndex) {
        this.commitIndex = commitIndex;
    }

    public Thread getLeaderThread() {
        return leaderThread;
    }

    public ArrayList<LogEntry> getLog() {
        return log;
    }
    
    public Message getStateMachineResult (int clientId) {
        return stateMachine.get(clientId);
    }

    public LinkedBlockingQueue<Message> getClientQueue() {
        return clientQueue;
    }

    public LinkedBlockingQueue<AppendEntry> getServerQueue() {
        return serverQueue;
    }

    public ProcessClient getClientSocket(int id) {
        return clientSockets.get(id);
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
    
    public void incrementCommitIndex() {
        commitIndex++;
    }

    public int getCurrentLogIndex() {
            return log.size()-1;
    }
    public void addSocket(int id, ProcessClient processClient) {
        clientSockets.put(id, processClient);
    }

    public void appendLogEntry(LogEntry logEntry) {
        log.add(logEntry);
    }

    public void appendMessageClientQueue(Message m) {
        clientQueue.add(m);
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
