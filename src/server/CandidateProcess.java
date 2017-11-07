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
 * @author João
 */
public class CandidateProcess implements Runnable {

    private Server server;
    private Candidate candidate;
    private int voteCounter;
    private boolean restartCandidateProcess;
    private ElectionTimeOutCandidate electionTimeOut;
    private ArrayList<RequestVote> talkToOtherServers;

    public CandidateProcess(Server server, Candidate candidate) {
        this.server = server;
        this.candidate = candidate;
        this.voteCounter = 1;//vota nele próprio
        this.restartCandidateProcess = false;
        this.electionTimeOut = new ElectionTimeOutCandidate(server, this);
        this.talkToOtherServers = new ArrayList<>();
    }

    @Override
    public void run() {
        System.out.println("O " + server.getServerID() + " estabeleceu-se como candidato...");
        int numServers = server.getServersProps().getHashMapProperties().size() / 2;
        createTalkToOtherServers(createRequestVote());
        electionTimeOut.cancelElectionTimer();
        electionTimeOut.run();

        boolean finishedElection = false;
        while (!finishedElection && !restartCandidateProcess) {
            if (!server.getServerQueue().isEmpty() && !finishedElection) {
                if (!restartCandidateProcess) {
                    AppendEntry ae = server.getServerQueue().remove();
                    if (Objects.equals(ae.getType(), "REQUESTVOTE")) {
                        //Se votar a favor
                        if (ae.isSuccess()) {
                            voteCounter++;
                        }
                        removeTalkToOtherServers(ae.getLeaderId());

                    } else if (Objects.equals(ae.getType(), "HEARTBEAT") || Objects.equals(ae.getType(), "APPENDENTRY")) {
                        if (ae.getTerm() > server.getCurrentTerm()) {
                            electionTimeOut.cancelElectionTimer();
                            server.setState("FOLLOWER");// Candidate volta ao estado de Follower
                            restartCandidateProcess();
                            AppendEntry hr = new AppendEntry(server.getCurrentTerm(), server.getServerID(), 0, 0, null, server.getLastApplied(), false, null, "HEARTBEAT");
                            try {
                                sendAppendEntryToServer(hr, server.getCandidateSocket(ae.getLeaderId()));
                            } catch (IOException ex) {
                                System.err.println(server.getServerID() + " - Erro no estabelecimento da ligação com o servidor \n" + ex.getLocalizedMessage());
                            }
                        }
                    }
                }
                finishedElection = voteCounter > numServers;
                if (finishedElection) {
                    server.setState("LEADER");
                }
            }

        }
        electionTimeOut.cancelElectionTimer();
        shutdownTalkToOtherServers();
        candidate.stopCandidate();
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
        if (server.getCurrentLogIndex() == -1) {
            lastLogIndex = -1;
            lastLogTerm = 0;
        } else {
            lastLogIndex = server.getCurrentLogIndex();
            lastLogTerm = server.getLog().get(server.getCurrentLogIndex()).getTerm();
        }
        AppendEntry rv = new AppendEntry(server.getCurrentTerm(), server.getServerID(), lastLogIndex, lastLogTerm, null, 0, true, null, "REQUESTVOTE");
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

    public void restartCandidateProcess() {
        this.restartCandidateProcess = true;
    }

    public ArrayList<RequestVote> getTalkToOtherServers() {
        return talkToOtherServers;
    }

    public Candidate getCandidate() {
        return candidate;
    }

}
