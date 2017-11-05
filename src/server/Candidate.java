/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server;

import java.util.Objects;

/**
 *
 * @author João
 */
public class Candidate implements Runnable {

    private Server server;
    private int voteCounter;

    public Candidate(Server server) {
        this.server = server;
        this.voteCounter = 1;//vota nele próprio
    }

    @Override
    public void run() {
        System.out.println("O " + server.getServerID() + " estabeleceu-se como candidato...");
        createTalkToOtherServers(createRequestVote());
        int numServers = server.getServersProps().getHashMapProperties().size() / 2;

        boolean finishedElection = false;
        while (finishedElection) {
            if (!server.getServerQueue().isEmpty()) {
                AppendEntry ae = server.getServerQueue().remove();
                if (Objects.equals(ae.getType(), "REQUESTVOTE")) {
                    //Se votar a favor
                    if (ae.isSuccess()) {
                        voteCounter++;
                    }
                }
            }
            finishedElection = voteCounter > numServers;
        }

    }

    private void createTalkToOtherServers(AppendEntry rv) {
        String serverToTalk;
        for (int i = 0; i < server.getServersProps().getHashMapProperties().size(); i++) {
            serverToTalk = "srv" + i;
            if (!server.getServerID().equals(serverToTalk)) {
                new Thread(new RequestVote(server, Integer.parseInt(server.getServersProps().getServerAdress(serverToTalk)[1]), rv)).start();
            }
        }
    }

    private AppendEntry createRequestVote() {
        server.incrementCurrentTerm();
        int lastLogIndex = server.getCurrentLogIndex();
        int lastLogTerm = server.getLog().get(server.getCurrentLogIndex()).getTerm();
        AppendEntry rv = new AppendEntry(server.getCurrentTerm(), server.getServerID(), lastLogIndex, lastLogTerm, null, 0, true, null, "REQUESTVOTE");
        return rv;
    }
}
