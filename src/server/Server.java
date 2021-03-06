/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server;

import common.KeyValueStore;
import common.Message;
import common.PropertiesManager;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.LinkedBlockingQueue;

/**
 *
 * @author TFD-GRUPO11-17/18
 */
public class Server implements Runnable {

    private String serverID;
    private boolean stopServer;
    private String leaderID;
    private PropertiesManager serversProps;
    private PropertiesManager clientsProps;
    private ServerSocket socketForClients;
    private int ID_MESSAGE = 0;
    private String state;
    private ListenOtherServers listenOtherServers;
    private ProcessServer processServer;
    private Leader threadLeader;
    private Candidate threadCandidate;
    private Follower threadFollower;
    private KeyValueStore keyValueStore;
    private FileHandler fileHandler;

    private int currentTerm;
    private String votedFor; //candidateId que recebeu o voto no termo atual (null se não há)
    private LinkedList<LogEntry> log;
    private int commitIndex;//indice do último log entry commitado
    private int lastApplied;//indice do último log entry executado pela máquina de estados
    private int lastIncludedTerm;

    private LinkedBlockingQueue<Message> clientQueue;
    private LinkedBlockingQueue<AppendEntry> serverQueue;
    private HashMap<String, Message> stateMachine;
    private HashMap<Integer, ProcessClient> clientSockets;
    private HashMap<String, Socket> serversSockets;

    public Server(String id, PropertiesManager serversProps, PropertiesManager clientsProps) {
        this.serverID = id;
        this.stopServer = false;
        this.leaderID = "srv0";
        this.serversProps = serversProps;
        this.clientsProps = clientsProps;
        this.threadLeader = null;
        this.threadCandidate = null;
        this.threadFollower = null;
        this.keyValueStore = new KeyValueStore();
        this.fileHandler = new FileHandler(this);
        System.out.println("O servidor " + serverID + " arrancou!");

        this.currentTerm = 0;
        this.votedFor = null;
        this.log = new LinkedList<LogEntry>();
        this.commitIndex = 0;
        this.lastApplied = 0;
        this.lastIncludedTerm = 0;

        clientQueue = new LinkedBlockingQueue<>();
        serverQueue = new LinkedBlockingQueue<>();
        stateMachine = new HashMap<>();
        clientSockets = new HashMap<>();
        serversSockets = new HashMap<>();

//        log = fileHandler.readLogFile();
//        for(LogEntry l :log){
//           System.out.println(l.getTerm() + " - " + l.getIndex() + " - " + l.getOperationType() + " - " + l.getKey() + " - " + l.getValue() + " - " + l.getIdMessage()
//            + " - " + l.getSource() + " - " + l.getMajority() + " - " + l.isCommited() + " - " + l.isSentToClient()); 
//        }
//        AppendEntry ae = fileHandler.readSnapshotFileToFollower();
//        System.out.println("last apllied:" + ae.getPrevLogIndex());
//        System.out.println("keyValueStore:" + ae.getType());
//        String [] s = ae.getType().split(";");
//        for(int i=0; i<s.length; i++){
//            System.out.println(s[i]);
//        }
    }

    @Override
    public void run() {

        installSnapshot();
        loadLog();

        /* Criar Socket para escutar os clientes */
        try {
            socketForClients = new ServerSocket(Integer.parseInt(clientsProps.getServerAdress(serverID)[1]), 50);
            socketForClients.setReuseAddress(true);

            /* Criar Socket para escutar os servidores */
            ServerSocket socketForServers = new ServerSocket(Integer.parseInt(serversProps.getServerAdress(serverID)[1]), 50);
            socketForServers.setReuseAddress(true);

            listenOtherServers = new ListenOtherServers(this, socketForServers);
            new Thread(listenOtherServers).start();

            state = "FOLLOWER";
            new Thread(new Follower(this)).start();

            while (!stopServer) {
                /* Processar os pedidos dos clientes */
                Socket clientSocket = socketForClients.accept();
                if (!stopServer) {
                    new Thread(new ProcessClient(this, clientSocket)).start();
                } else {
                    clientSocket.close();
                }

            }
        } catch (IOException ex) {
            System.err.println("O servidor " + serverID + " não consegue ativar a sua ligação \n" + ex.getLocalizedMessage());
        }
    }

    /**
     * *******************************************************************
     ****************** GETTER's e SETTER's*******************************
     * *******************************************************************
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

    public void setID_MESSAGE(int ID_MESSAGE) {
        this.ID_MESSAGE = ID_MESSAGE;
    }

    public int getIDMESSAGE() {
        return ID_MESSAGE;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
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

    public String getVotedFor() {
        return votedFor;
    }

    public void setVotedFor(String votedFor) {
        this.votedFor = votedFor;
    }

    public void resetVotedFor() {
        this.votedFor = null;
    }

    public int getCommitIndex() {
        return commitIndex;
    }

    public void setCommitIndex(int commitIndex) {
        this.commitIndex = commitIndex;
    }

    public void setLastApplied(int lastApplied) {
        this.lastApplied = lastApplied;
    }

    public int getLastApplied() {
        return lastApplied;
    }

    public int getLastIncludedTerm() {
        return lastIncludedTerm;
    }

    public void setLastIncludedTerm(int lastIncludedTerm) {
        this.lastIncludedTerm = lastIncludedTerm;
    }

    public LinkedList<LogEntry> getLog() {
        return log;
    }

    public void setLog(LinkedList<LogEntry> log) {
        this.log = log;
    }

    public HashMap<String, Message> getStateMachine() {
        return stateMachine;
    }

    public Message getStateMachineResult(String id) {
        return stateMachine.get(id);
    }

    public KeyValueStore getKeyValueStore() {
        return keyValueStore;
    }

    public FileHandler getFileHandler() {
        return fileHandler;
    }

    public void setFileHandler(FileHandler fileHandler) {
        this.fileHandler = fileHandler;
    }

    public LinkedBlockingQueue<Message> getClientQueue() {
        return clientQueue;
    }

    public LinkedBlockingQueue<AppendEntry> getServerQueue() {
        return serverQueue;
    }

    public void setServerQueue(LinkedBlockingQueue<AppendEntry> serverQueue) {
        this.serverQueue = serverQueue;
    }

    public ProcessClient getProcessClientSocket(int id) {
        return clientSockets.get(id);
    }

    public Socket getServersSockets(String id) {
        return serversSockets.get(id);
    }

    public HashMap<String, Socket> getHashMapServersSockets() {
        return this.serversSockets;
    }

    public void setThreadLeader(Leader threadLeader) {
        this.threadLeader = threadLeader;
    }

    public void setThreadCandidate(Candidate threadCandidate) {
        this.threadCandidate = threadCandidate;
    }

    public void setThreadFollower(Follower threadFollower) {
        this.threadFollower = threadFollower;
    }

    public void resetThreadLeader() {
        this.threadLeader = null;
    }

    public void resetThreadCandidate() {
        this.threadCandidate = null;
    }

    public void resetThreadFollower() {
        this.threadFollower = null;
    }

    public void setProcessServer(ProcessServer processServer) {
        this.processServer = processServer;
    }

    public ProcessServer getProcessServer() {
        return processServer;
    }

    /**
     * *******************************************************************
     ********************* MÉTODOS SERVIDOR ******************************
     * *******************************************************************
     */
    public void stopServer() {
        this.stopServer = true;
        this.listenOtherServers.stopListenOtherServers();

        if (this.threadLeader != null) {
            this.threadLeader.stopLeader();
        } else if (this.threadCandidate != null) {
            this.threadCandidate.stopCandidate();
        } else if (this.threadFollower != null) {
            this.threadFollower.stopFollower();
        }

        try {
            new Socket(socketForClients.getInetAddress(), socketForClients.getLocalPort()).close();
        } catch (IOException e) {

        }
    }

    public void incrementCurrentTerm() {
        currentTerm++;
    }

    public int getCurrentLogIndex() {
        return log.getLast().getIndex();
    }

    public int getLogEntryIndexInLog(int trueIndex) {
        int index = -1;
        for (int i = 0; i < log.size(); i++) {
            if (log.get(i).getIndex() == trueIndex) {
                index = i;
                break;
            }
        }
        return index;
    }

    public void addProcessClientSockets(int id, ProcessClient processClient) {
        clientSockets.put(id, processClient);
    }

    public void addServersSockets(String id, Socket serverSocket) {
        serversSockets.put(id, serverSocket);
    }

    public void appendLogEntry(LogEntry logEntry) {
        log.add(logEntry);
    }

    public void appendMessageClientQueue(Message m) {
        clientQueue.add(m);
    }

    public Map<String, Integer> getNextIndex() {
        Map<String, Integer> nextIndex = new HashMap<>();
        int index = log.size();
        for (int i = 0; i < serversProps.getHashMapProperties().size(); i++) {
            if (!Objects.equals("srv" + i, serverID)) {
                nextIndex.put("srv" + i, index);
            }
        }
        return nextIndex;
    }

    public void execute(LogEntry logEntry) {
        logEntry.setCommited(true);
        fileHandler.writeToFile(storeLogEntryInFile(logEntry), false);//escreve nova LogEntry commited no ficheiro de log (persistent storage)
        String executionResult = executeOperationOnKeyValueStore(logEntry);
        Message m = new Message(logEntry.getIdMessage(), logEntry.getSource(), "RESPONSE", logEntry.getOperationType(), logEntry.getKey(), executionResult + "\nSucesso - atribuído o ID " + getIDMESSAGE());
        stateMachine.put(logEntry.getIdMessage() + logEntry.getSource(), m);
        ID_MESSAGE++;

        /* Servidor envia a resposta ao cliente, caso seja o líder*/
        if (logEntry.getSource() != 0) {
            if (Objects.equals(state, "LEADER")) {
                try {
                    getProcessClientSocket(logEntry.getSource()).sendMessageToClient(m);//responde ao cliente
                    logEntry.setSentToClient(true);
                } catch (IOException ex) {
                    System.err.println("Erro na resposta ao cliente pelo líder \n" + ex.getLocalizedMessage());
                    logEntry.setSentToClient(false);
                }
            }
        }
    }

    public void applyNewEntries() {
        int aux;
        if (commitIndex > lastApplied) {
            aux = lastApplied + 1;
        } else {
            aux = lastApplied;
        }
        for (int i = aux; i <= commitIndex; i++) {
            if (!log.get(getLogEntryIndexInLog(i)).isCommited()) {
                execute(log.get(getLogEntryIndexInLog(i)));
            }
        }
        lastApplied = commitIndex;
    }

    private String storeLogEntryInFile(LogEntry logEntry) {
        String log = "" + logEntry.getTerm() + ";";
        log += logEntry.getIndex() + ";";
        log += logEntry.getOperationType() + ";";
        log += logEntry.getKey() + ";";
        log += logEntry.getValue() + ";";
        log += logEntry.getIdMessage() + ";";
        log += logEntry.getSource() + ";";
        log += logEntry.getMajority() + ";";
        log += logEntry.isCommited() + ";";
        log += logEntry.isSentToClient() + ";";
        return log;
    }

    private String executeOperationOnKeyValueStore(LogEntry logEntry) {
        String result = "";
        switch (logEntry.getOperationType()) {
            case PUT:
                keyValueStore.put(logEntry.getKey(), logEntry.getValue());
                result = "Novo valor introduzido com sucesso para a chave " + logEntry.getKey();
                break;
            case GET:
                result = keyValueStore.get(logEntry.getKey());
                break;
            case DEL:
                result = keyValueStore.delete(logEntry.getKey());
                break;
            case LIST:
                result = keyValueStore.list();
                break;
            case CAS:
                String[] stringSplit = logEntry.getValue().split("\\|");
                result = keyValueStore.compareAndSwap(logEntry.getKey(), stringSplit[0], stringSplit[1]);
                break;
        }
        return result;
    }

    public AppendEntry receiverRequestVoteValidation(AppendEntry ae) {
        AppendEntry rv = null;
        int lastLogIndex, lastLogTerm;
        if (log.isEmpty() || log.size() == 1) {
            if (lastApplied > 0) {
                lastLogIndex = lastApplied;
                lastLogTerm = lastIncludedTerm;
            } else {
                lastLogIndex = -1;
                lastLogTerm = -1;
            }
        } else {
            lastLogIndex = log.getLast().getIndex();
            lastLogTerm = log.getLast().getTerm();
//            lastLogIndex = log.get(log.size() - 2).getIndex();
//            lastLogTerm = log.get(log.size() - 2).getTerm();
        }

        if (ae.getTerm() < getCurrentTerm()) {
            rv = new AppendEntry(getCurrentTerm(), getServerID(), 0, 0, null, 0, false, null, "REQUESTVOTE");
        } else if (getVotedFor() == null || Objects.equals(getVotedFor(), ae.getLeaderId())) {
            if (ae.getPrevLogTerm() < lastLogTerm) {
                rv = new AppendEntry(getCurrentTerm(), getServerID(), 0, 0, null, 0, false, null, "REQUESTVOTE");
            } else if (lastLogTerm == ae.getPrevLogTerm()) {
                if (ae.getPrevLogIndex() < lastLogIndex) {
                    rv = new AppendEntry(getCurrentTerm(), getServerID(), 0, 0, null, 0, false, null, "REQUESTVOTE");
                } else {
                    rv = new AppendEntry(getCurrentTerm(), getServerID(), 0, 0, null, 0, true, null, "REQUESTVOTE");
                    setVotedFor(ae.getLeaderId());
                }
            } else {
                rv = new AppendEntry(getCurrentTerm(), getServerID(), 0, 0, null, 0, true, null, "REQUESTVOTE");
                setVotedFor(ae.getLeaderId());
            }
        } else {
            rv = new AppendEntry(getCurrentTerm(), getServerID(), 0, 0, null, 0, false, null, "REQUESTVOTE");
        }
        return rv;
    }

    public void takeSnapshot() {
        String snapshot = "" + lastApplied + ";";
        snapshot += getLastAppliedTerm() + ";";
        snapshot += getIDMESSAGE() + "\n";
        snapshot += keyValueStore.getStateMachineState();

        fileHandler.deleteSnapshotFile();
        fileHandler.writeToFile(snapshot, true);
        fileHandler.clearLogFile();
        lastIncludedTerm = log.get(getLogEntryIndexInLog(lastApplied)).getTerm();
        LinkedList<LogEntry> newLog = new LinkedList<>();
        for (int i = 0; i < log.size(); i++) {
            if (log.get(i).getIndex() > lastApplied) {
                newLog.add(log.get(i));
            }
        }
        log = newLog;
    }

    private void installSnapshot() {
        fileHandler.readSnapshotFile();
        currentTerm = lastIncludedTerm;
    }

    private void loadLog() {
        fileHandler.readLogFile();
        for (LogEntry l : log) {
            if (l.isCommited()) {
                commitIndex = l.getIndex();
                currentTerm = l.getTerm();
                String executionResult = executeOperationOnKeyValueStore(l);
                Message m = new Message(l.getIdMessage(), l.getSource(), "RESPONSE", l.getOperationType(), l.getKey(), executionResult + "\nSucesso - atribuído o ID " + getIDMESSAGE());
                stateMachine.put(l.getIdMessage() + l.getSource(), m);
                ID_MESSAGE++;
            } else {
                break;
            }
        }
        lastApplied = commitIndex;
    }

    private int getLastAppliedTerm() {
        int lastAppliedTerm = -1;
        for (LogEntry l : log) {
            if (l.getIndex() == lastApplied) {
                return l.getTerm();
            }
        }
        return lastAppliedTerm;
    }

    public String printLog() {
        String str = serverID + "->LOG: ";
        for (LogEntry l : log) {
            str += l.getTerm() + "-" + l.getIndex() + "-" + l.getIdMessage() + "/";
        }
        return str;
    }

}
