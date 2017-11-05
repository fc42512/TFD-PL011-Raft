/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 *
 * @author João
 */
public class Follower implements Runnable {

    private Server server;
    private ServerSocket serverSocket;
    private boolean stopFollower;

    public Follower(Server s, ServerSocket ss) {
        this.server = s;
        this.serverSocket = ss;
        this.stopFollower = false;
    }

    @Override
    public void run() {
        try {
            /*Follower faz commit de uma "blank no-operation" entry no início */
//            server.appendLogEntry(new LogEntry(server.getCurrentTerm(), server.getCurrentLogIndex()+1, "NO-OP", "noOperation", null, 0));
            while (!stopFollower) {

                Socket followerSocket = serverSocket.accept();
                if (!stopFollower) {
                    new Thread(new FollowerProcess(server, followerSocket, this)).start();
                } else {
                    followerSocket.close();
                }

            }
//            serverSocket.close();
            Candidate c = new Candidate(server, serverSocket);
            new Thread(new CandidateProcess(server, c)).start();
            new Thread(c).start();

        } catch (IOException ex) {
            System.err.println(server.getServerID() + " - Erro no estabelecimento da ligação com o líder \n" + ex.getLocalizedMessage());

        }
    }

    public void stopFollower() {
        this.stopFollower = true;
    }
}
