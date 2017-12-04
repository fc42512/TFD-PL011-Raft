/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.LinkedList;
import java.util.Map;
import java.util.Objects;

/**
 *
 * @author TFD-GRUPO11-17/18
 */
public class Follower implements Runnable {

    private Server server;
    private boolean stopFollower;
    private boolean startCandidate;
    private ElectionTimeOutFollower electionTimeOut;

    public Follower(Server server) {
        this.server = server;
        this.stopFollower = false;
        this.startCandidate = false;
        this.electionTimeOut = new ElectionTimeOutFollower(server, this);
    }

    @Override
    public void run() {
        server.setThreadFollower(this);
        server.resetThreadLeader();
        server.resetThreadCandidate();
        server.getServerQueue().clear();//limpa a fila de AppendEntry recebidas
        electionTimeOut.run();
        try {

            while (!stopFollower) {

                if (!server.getServerQueue().isEmpty() && !stopFollower) {
                    if (!stopFollower) {
                        AppendEntry ae = server.getServerQueue().remove();
                        electionTimeOut.cancelElectionTimer();
                        AppendEntry response = processAppendEntries(ae);//executa o método que processa as appendEntries
                        electionTimeOut.run();
                        System.out.println("Recebida msg no Follower");

                        sendMessageToLeader(response, server.getServersSockets(ae.getLeaderId()));
                        System.out.println("Enviada msg do Follower");

                        /* Realiza um snapshot da máquina de estados actual */
                        if (server.getLog().size() > 5) {
                            electionTimeOut.cancelElectionTimer();
                            server.takeSnapshot();
                            electionTimeOut.run();
                        }
                    }
                }
            }
            electionTimeOut.cancelElectionTimer();
            closeAllOpenSockets();
            if (startCandidate) {
                startCandidate();
            }

        } catch (IOException ex) {
            System.err.println(server.getServerID() + " - Erro no estabelecimento da ligação com o líder \n" + ex.getLocalizedMessage());
        }
    }

    private void sendMessageToLeader(AppendEntry ae, Socket s) throws IOException {
        BufferedOutputStream bos = new BufferedOutputStream(s.getOutputStream());
        ObjectOutputStream osw = new ObjectOutputStream(bos);
        osw.writeObject(ae);//Envia a mensagem
        osw.flush();
    }

    private AppendEntry processAppendEntries(Object obj) {
        AppendEntry response = null;
        if ((obj instanceof AppendEntry) && obj != null) {
            AppendEntry ae = (AppendEntry) obj;
            server.setLeaderID(ae.getLeaderId());//guarda o ID do líder

            /* Verifica se o AppendEntry é do tipo HeartBeat */
            if (Objects.equals(ae.getType(), "HEARTBEAT")) {
                System.out.println("Recebi heartbeat :)");
                if(stopFollower){
                    stopFollower = false;
                    startCandidate = false;
                }
                this.server.resetVotedFor();
                server.setCommitIndex(ae.getLeaderCommit());
                if (server.getLog().size() > 0 && ae.getLeaderCommit() > 0) {
                    if (server.getLastApplied() < server.getLog().getLast().getIndex() ) {
                        server.applyNewEntries();
                    }
                }
                return new AppendEntry(server.getCurrentTerm(), server.getServerID(), 0, 0, null, server.getLastApplied(), false, null, "HEARTBEAT");

                /* Verifica se o AppendEntry é do tipo RequestVote */
            } else if (Objects.equals(ae.getType(), "REQUESTVOTE")) {
                AppendEntry rv = server.receiverRequestVoteValidation(ae);//executa a validação de quem recebe um requestVote
//                stopFollower();
                return rv;

                /* Verifica se o AppendEntry é do tipo AppendEntry */
            } else if (Objects.equals(ae.getType(), "APPENDENTRY")) {
                /* Responde falso se term < currentTerm */
                if (ae.getTerm() < server.getCurrentTerm()) {
                    return new AppendEntry(server.getCurrentTerm(), server.getServerID(), 0, 0, null, server.getLastApplied(), false, ae.getMessage(), "APPENDENTRY");

                    /* Responde falso se term of prevLogIndex != prevLogTerm */
                } else if (!server.getLog().isEmpty() && ae.getPrevLogIndex() != -1) {
                    if (ae.getPrevLogIndex() > server.getLastApplied()) {
                        deleteConflictEntries(server.getLastApplied());
                        return new AppendEntry(server.getCurrentTerm(), server.getServerID(), 0, 0, null, server.getLastApplied(), false, ae.getMessage(), "APPENDENTRY");
                    } else if (server.getLog().getLast().getTerm() != ae.getPrevLogTerm()) {
                        deleteConflictEntries(server.getLastApplied());
                        return new AppendEntry(server.getCurrentTerm(), server.getServerID(), 0, 0, null, server.getLastApplied(), false, ae.getMessage(), "APPENDENTRY");
                    } else if (server.getLastApplied() == ae.getPrevLogIndex()) {
//                        deleteConflictEntries(ae.getPrevLogIndex());
                        addNewLogEntries(ae);
                        server.applyNewEntries();
                        return new AppendEntry(server.getCurrentTerm(), server.getServerID(), ae.getPrevLogIndex() + ae.getEntries().size(), ae.getPrevLogTerm(), null, server.getLog().getLast().getIndex(), true, ae.getMessage(), "APPENDENTRY");
                    } else if (ae.getPrevLogIndex() == -1) {
                        addNewLogEntries(ae);
                        server.applyNewEntries();
                        return new AppendEntry(server.getCurrentTerm(), server.getServerID(), ae.getPrevLogIndex() + ae.getEntries().size(), ae.getPrevLogTerm(), null, server.getLog().getLast().getIndex(), true, ae.getMessage(), "APPENDENTRY");

                    }
                } else if (ae.getPrevLogIndex() == -1) {
                    if (!server.getLog().isEmpty()) {
                        server.getLog().clear();
                    }
                    addNewLogEntries(ae);
//                        server.applyNewEntries();
                    return new AppendEntry(server.getCurrentTerm(), server.getServerID(), ae.getPrevLogIndex() + ae.getEntries().size(), ae.getPrevLogTerm(), null, server.getLog().getLast().getIndex(), true, ae.getMessage(), "APPENDENTRY");
                } else {
                    return new AppendEntry(server.getCurrentTerm(), server.getServerID(), 0, 0, null, -1, false, ae.getMessage(), "APPENDENTRY");
                }
            } else {
                server.setCurrentTerm(ae.getTerm());
                server.setLeaderID(ae.getLeaderId());
                server.setLastApplied(ae.getPrevLogIndex());

                String[] s = ae.getType().split(";");
                for (int i = 0; i < s.length; i = i + 2) {
                    server.getKeyValueStore().put(s[i], s[i + 1]);
                }
            }
        }
        return response;
    }

    private void addNewLogEntries(AppendEntry ae) {
        for (LogEntry le : ae.getEntries()) {
            le.setCommited(false);
            server.appendLogEntry(le);
        }
        server.setCurrentTerm(ae.getTerm());
        server.setCommitIndex(ae.getLeaderCommit());
    }

    private void deleteConflictEntries(int index) {
        LinkedList<LogEntry> newLog = new LinkedList<>();
        for (int i = 0; i < server.getLog().size(); i++) {
            if (server.getLog().get(i).getIndex() <= index) {
                newLog.add(server.getLog().get(i));
            }
        }
        server.setLog(newLog);

//        int maxIndex = server.getLog().size() - 1;
//        for (int i = maxIndex; i > index; i--) {
//            server.getLog().remove(i);
//        }
    }

    public void stopFollower() {
        this.stopFollower = true;
//        if (server.getProcessServer() != null) {
//            server.getProcessServer().stopProcessServer();
//        }
    }

    public void activateStartCandidate() {
        this.startCandidate = true;
    }

    private void startCandidate() {
        new Thread(new Candidate(server)).start();
    }

    private void closeAllOpenSockets() {
        for (Map.Entry<String, Socket> s : server.getHashMapServersSockets().entrySet()) {
            try {
                s.getValue().close();
            } catch (IOException ex) {
                System.out.println("Erro no fecho da ligação!");
            }
        }
    }
}
