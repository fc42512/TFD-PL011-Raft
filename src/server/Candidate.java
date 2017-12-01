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
import java.util.ArrayList;
import java.util.Objects;

/**
 *
 * @author TFD-GRUPO11-17/18
 */
public class Candidate implements Runnable {

    private Server server;
    private int voteCounter;
    private boolean stopCandidate;
    private boolean restartCandidate;
    private boolean startLeaderOrFollower;
    private ElectionTimeOutCandidate electionTimeOut;
    private ArrayList<RequestVote> talkToOtherServers;

    public Candidate(Server server) {
        this.server = server;
        this.voteCounter = 1;//vota nele próprio
        this.server.setVotedFor(this.server.getServerID());
        this.stopCandidate = false;
        this.restartCandidate = false;
        this.startLeaderOrFollower = false;
        this.electionTimeOut = new ElectionTimeOutCandidate(server, this);
        this.talkToOtherServers = new ArrayList<>();
    }

    @Override
    public void run() {
        System.out.println("O " + server.getServerID() + " estabeleceu-se como candidato...");
        server.setThreadCandidate(this);
        server.resetThreadLeader();
        server.resetThreadFollower();
        int numServers = server.getServersProps().getHashMapProperties().size() / 2;
        server.getServerQueue().clear();//limpa a fila de AppendEntry recebidas
        createTalkToOtherServers(createRequestVote());
        electionTimeOut.cancelElectionTimer();
        electionTimeOut.run();

        boolean finishedElection = false;
        while (!finishedElection && !stopCandidate) {
            if (!server.getServerQueue().isEmpty() && !finishedElection && !stopCandidate) {
                if (!stopCandidate) {
                    AppendEntry ae = server.getServerQueue().remove();
                    if (Objects.equals(ae.getType(), "REQUESTVOTE")) {
                        //Se votar a favor
                        if (ae.isSuccess()) {
                            voteCounter++;
                            removeTalkToOtherServers(ae.getLeaderId());
                        }

                    } else if (Objects.equals(ae.getType(), "HEARTBEAT") || Objects.equals(ae.getType(), "APPENDENTRY") || Objects.equals(ae.getType(), "REQUESTVOTE")) {
                        if (ae.getTerm() > server.getCurrentTerm()) {
                            electionTimeOut.cancelElectionTimer();
                            server.setState("FOLLOWER");// Candidate volta ao estado de Follower
                            stopCandidate();
                            this.startLeaderOrFollower = true;
                            AppendEntry hr = new AppendEntry(server.getCurrentTerm(), server.getServerID(), 0, 0, null, server.getLastApplied(), false, null, "HEARTBEAT");
                            try {
                                sendAppendEntryToServer(hr, server.getServersSockets(ae.getLeaderId()));
                            } catch (IOException ex) {
                                System.err.println(server.getServerID() + " - Erro no estabelecimento da ligação com o servidor \n" + ex.getLocalizedMessage());
                            }
                        }
                    }
                }
                finishedElection = voteCounter > numServers;
                if (finishedElection) {
                    server.setState("LEADER");
                    this.startLeaderOrFollower = true;
                }
            }

        }
        electionTimeOut.cancelElectionTimer();
        shutdownTalkToOtherServers();
        server.resetVotedFor();
        if (startLeaderOrFollower) {
            startLeaderOrFollower();
        }
        if (restartCandidate) {
            new Thread(new Candidate(server)).start();
        }
    }

    private void createTalkToOtherServers(AppendEntry ae) {
        String serverToTalk;
        for (int i = 0; i < server.getServersProps().getHashMapProperties().size(); i++) {
            serverToTalk = "srv" + i;
            if (!server.getServerID().equals(serverToTalk)) {
                RequestVote rv = new RequestVote(server, Integer.parseInt(server.getServersProps().getServerAdress(serverToTalk)[1]), ae, serverToTalk);
                talkToOtherServers.add(rv);
                new Thread(rv).start();
            }
        }
    }

    private void removeTalkToOtherServers(String id) {
        if (!talkToOtherServers.isEmpty()) {
            for (int i = 0; i < talkToOtherServers.size(); i++) {
                if (Objects.equals(talkToOtherServers.get(i).getId(), id)) {
                    talkToOtherServers.remove(i);
                    break;
                }
            }
        }
    }

    private AppendEntry createRequestVote() {
        int lastLogIndex, lastLogTerm;
        server.incrementCurrentTerm();
        if (server.getLog().isEmpty() || server.getLog().size() == 1) {
            if (server.getLastApplied() > 0) {
                lastLogIndex = server.getLastApplied();
                lastLogTerm = server.getCurrentTerm();
            } else {
                lastLogIndex = -1;
                lastLogTerm = -1;
            }
        } else {
            lastLogIndex = server.getLog().get(server.getLog().size() - 2).getIndex();
            lastLogTerm = server.getLog().get(server.getLog().size() - 2).getTerm();
        }
        AppendEntry rv = new AppendEntry(server.getCurrentTerm(), server.getServerID(), lastLogIndex, lastLogTerm, null, 0, false, null, "REQUESTVOTE");
        return rv;
    }

    private void sendAppendEntryToServer(AppendEntry ae, Socket s) throws IOException {
        BufferedOutputStream bos = new BufferedOutputStream(s.getOutputStream());
        ObjectOutputStream osw = new ObjectOutputStream(bos);
        osw.writeObject(ae);//Envia o appendentry
        osw.flush();
        osw.close();
        bos.close();
        s.close();//Fecha a ligação
    }

    public void shutdownTalkToOtherServers() {
        for (RequestVote rv : getTalkToOtherServers()) {
            rv.cancelRequestVote();
        }
    }

    public void stopCandidate() {
        this.stopCandidate = true;
        if (server.getProcessServer() != null) {
            server.getProcessServer().stopProcessServer();
        }
    }

    public void restartCandidate() {
        stopCandidate();
        this.restartCandidate = true;
    }

    public ArrayList<RequestVote> getTalkToOtherServers() {
        return talkToOtherServers;
    }

    private void startLeaderOrFollower() {
        if (Objects.equals(server.getState(), "FOLLOWER")) {
            new Thread(new Follower(server)).start();

        } else if (Objects.equals(server.getState(), "LEADER")) {
            new Thread(new Leader(server, server.getNextIndex(), server.getNextIndex())).start();
        }
    }

}
