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
public class Follower implements Runnable {

    private Server server;
    private ServerSocket serverSocket;

    public Follower(Server s, ServerSocket ss) {
        this.server = s;
        this.serverSocket = ss;
    }

    @Override
    public void run() {
        try {
            /*Follower faz commit de uma "blank no-operation" entry no início */
//            server.appendLogEntry(new LogEntry(server.getCurrentTerm(), server.getCurrentLogIndex()+1, "NO-OP", "noOperation", null, 0));
            while (true) {

                new Thread(new FollowerProcess(server, serverSocket.accept())).start();

            }

        } catch (IOException ex) {
            System.err.println(server.getServerID() + " - Erro no estabelecimento da ligação com o líder \n" + ex.getLocalizedMessage());

        }
    }
}
