/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server;

import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

/**
 *
 * @author João
 */
public class ElectionTimeOutCandidate {

    private Server server;
    private CandidateProcess candidateProcess;
    private Timer electionTimer;
    private TimerTask electionTimerTask;

    public ElectionTimeOutCandidate(Server server, CandidateProcess candidateProcess) {
        this.server = server;
        this.candidateProcess = candidateProcess;
    }

    public void run() {

        electionTimer = new Timer();
        electionTimerTask = new TimerTask() {
            @Override
            public void run() {

                System.out.println("Candidate " + server.getServerID() + " - Atingi o timeout!");
                server.setState("CANDIDATE");// CandidateProcess mantém-se no estado de Candidato
                candidateProcess.restartCandidateProcess();
                candidateProcess.shutdownTalkToOtherServers();
                cancelElectionTimer();
                new Thread(new CandidateProcess(server, candidateProcess.getCandidate())).start();
            }

        };
        // Executa a task escolhendo um período aleatório (150 a 300ms)
        electionTimer.schedule(electionTimerTask, getRandomTime());

    }

    // Termina o Timer e as Scheduled Tasks
    public void cancelElectionTimer() {
        if (this.electionTimer != null) {
            this.electionTimer.cancel();
            this.electionTimer.purge();
        }
        if (this.electionTimerTask != null) {
            this.electionTimerTask.cancel();
        }
    }

    /* Gera um número aleatório entre 150 e 299 */
    private int getRandomTime() {
        Random rnd = new Random();
        return rnd.nextInt(150) + 150;
    }

}
