/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server;

import java.io.IOException;
import java.net.ServerSocket;

/**
 *
 * @author João
 */
public class Follower extends Thread {

    private Server server;
    private ServerSocket serverSocket;
    private boolean stopFollower;
    private ThreadGroup followerThreads;

    public Follower(Server s, ServerSocket ss) {
        this.server = s;
        this.serverSocket = ss;
        this.stopFollower = false;
        this.followerThreads = new ThreadGroup("followerThreads");
    }

    @Override
    public void run() {
        try {
            /*Follower faz commit de uma "blank no-operation" entry no início */
//            server.appendLogEntry(new LogEntry(server.getCurrentTerm(), server.getCurrentLogIndex()+1, "NO-OP", "noOperation", null, 0));
            while (!stopFollower) {
                
                FollowerProcess fp = new FollowerProcess(server, serverSocket.accept(), this);
                new Thread(followerThreads, fp).start();

            }
            serverSocket.close();
//            followerThreads.destroy();

        } catch (IOException ex) {
            System.err.println(server.getServerID() + " - Erro no estabelecimento da ligação com o líder \n" + ex.getLocalizedMessage());

        }
    }

    public void stopFollower() {
        this.stopFollower = true;
    }
    public void shutdownFollower(){
        this.stop();
    }
}
