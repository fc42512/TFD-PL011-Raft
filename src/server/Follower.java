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
                        AppendEntry response = processAppendEntries(ae);//executa o método que processa ao appendEntries
                        electionTimeOut.run();
                        System.out.println("Recebida msg");

                        sendMessageToLeader(response, server.getServersSockets(ae.getLeaderId()));
                        System.out.println("Enviada msg");
                    }
                }
            }
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
                this.server.resetVotedFor();
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
                    if (ae.getPrevLogIndex() >= server.getLog().size()) {
//                      deleteConflictEntries(ae.getPrevLogIndex());
                        return new AppendEntry(server.getCurrentTerm(), server.getServerID(), 0, 0, null, server.getLastApplied(), false, ae.getMessage(), "APPENDENTRY");
                    } else if (server.getLog().get(ae.getPrevLogIndex()).getTerm() != ae.getPrevLogTerm()) {
                        deleteConflictEntries(ae.getPrevLogIndex());
                        return new AppendEntry(server.getCurrentTerm(), server.getServerID(), 0, 0, null, server.getLastApplied(), false, ae.getMessage(), "APPENDENTRY");
                    } else if (server.getLog().get(ae.getPrevLogIndex()).getIndex() == ae.getPrevLogIndex()) {
                        deleteConflictEntries(ae.getPrevLogIndex());
                        addNewLogEntries(ae);
                        server.applyNewEntries();
                        return new AppendEntry(server.getCurrentTerm(), server.getServerID(), ae.getPrevLogIndex() + ae.getEntries().size(), ae.getPrevLogTerm(), null, server.getLastApplied(), true, ae.getMessage(), "APPENDENTRY");
                    } else if (ae.getPrevLogIndex() == -1) {
                        addNewLogEntries(ae);
                        server.applyNewEntries();
                        return new AppendEntry(server.getCurrentTerm(), server.getServerID(), ae.getPrevLogIndex() + ae.getEntries().size(), ae.getPrevLogTerm(), null, server.getLastApplied(), true, ae.getMessage(), "APPENDENTRY");
                
                    }
                } else {
                    if (ae.getPrevLogIndex() == -1) {
                        addNewLogEntries(ae);
                        server.applyNewEntries();
                        return new AppendEntry(server.getCurrentTerm(), server.getServerID(), ae.getPrevLogIndex() + ae.getEntries().size(), ae.getPrevLogTerm(), null, server.getLastApplied(), true, ae.getMessage(), "APPENDENTRY");
                    } else {
                        return new AppendEntry(server.getCurrentTerm(), server.getServerID(), 0, 0, null, -1, false, ae.getMessage(), "APPENDENTRY");
                    }
                }
            }
        }
        return response;
    }

    private void addNewLogEntries(AppendEntry ae) {
        for (LogEntry le : ae.getEntries()) {
            server.appendLogEntry(le);
        }
        server.setCurrentTerm(ae.getTerm());
        server.setCommitIndex(ae.getLeaderCommit());
    }

    private void deleteConflictEntries(int index) {
        int maxIndex = server.getLog().size() - 1;
        for (int i = maxIndex; i > index; i--) {
            server.getLog().remove(i);
        }
    }

    public void stopFollower() {
        this.stopFollower = true;
        if (server.getProcessServer() != null) {
            server.getProcessServer().stopProcessServer();
        }
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
