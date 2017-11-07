/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Objects;

/**
 *
 * @author João
 */
public class LeaderHandleCandidate implements Runnable {

    private Server server;
    private ServerSocket serverSocket;
    private boolean stopLeaderHandleCandidate;

    public LeaderHandleCandidate(Server server, ServerSocket serverSocket) {
        this.server = server;
        this.serverSocket = serverSocket;
        this.stopLeaderHandleCandidate = false;
    }

    @Override
    public void run() {
        try {
            while (!stopLeaderHandleCandidate) {

                Socket leaderHandleCandidateSocket = serverSocket.accept();

                if (!stopLeaderHandleCandidate) {
                    ObjectInputStream dis = new ObjectInputStream(leaderHandleCandidateSocket.getInputStream());
                    AppendEntry ae = (AppendEntry) dis.readObject();
                    processAppendEntry(ae, leaderHandleCandidateSocket);//executa o método que processa a AppendEntry de outro servidor
                    System.out.println("Recebida AppendEntry. Sou o " + server.getServerID());
                } else {
                    leaderHandleCandidateSocket.close();
                }
            }
//            serverSocket.close();

        } catch (IOException ex) {
            System.err.println(server.getServerID() + " - Erro no estabelecimento da ligação com o líder \n" + ex.getLocalizedMessage());

        } catch (ClassNotFoundException ex) {
            System.err.println("Erro na conversão da classe Request\n" + ex.getLocalizedMessage());
        }

    }

    private void processAppendEntry(AppendEntry ae, Socket leaderHandleCandidateSocket) {
        if (ae != null) {
            if (Objects.equals(ae.getType(), "REQUESTVOTE")) {
                server.addCandidateSockets(ae.getLeaderId(), leaderHandleCandidateSocket);
                server.getServerQueue().add(ae);
            }
        }
    }

    public void stopLeaderHandleCandidate() {
        this.stopLeaderHandleCandidate = true;
    }
}
