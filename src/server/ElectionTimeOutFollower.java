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
 * @author TFD-GRUPO11-17/18
 */
public class ElectionTimeOutFollower {

    private Server server;
    private Follower follower;
    private Timer electionTimer;
    private TimerTask electionTimerTask;

    public ElectionTimeOutFollower(Server server, Follower follower) {
        this.server = server;
        this.follower = follower;
    }

    public void run() {

        electionTimer = new Timer();
        electionTimerTask = new TimerTask() {
            @Override
            public void run() {
                
                System.out.println("Follower " + server.getServerID()+ " - Atingi o timeout!");
                follower.stopFollower();
                follower.activateStartCandidate();
                server.setState("CANDIDATE");// Follower passa para o estado de Candidato
                cancelElectionTimer();
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
        return rnd.nextInt(150)+ 150;
    }

}
