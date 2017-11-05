/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server;

import com.sun.org.apache.xerces.internal.utils.Objects;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author João
 */
public class Candidate implements Runnable {

    private Server server;
    private ServerSocket serverSocket;
    private boolean stopCandidate;

    public Candidate(Server server, ServerSocket serverSocket) {
        this.server = server;
        this.serverSocket = serverSocket;
        this.stopCandidate = false;
    }

    @Override
    public void run() {
        try {
            while (!stopCandidate) {

                Socket candidateSocket = serverSocket.accept();

                if (!stopCandidate) {
                    ObjectInputStream dis = new ObjectInputStream(candidateSocket.getInputStream());
                    AppendEntry ae = (AppendEntry) dis.readObject();
                    processAppendEntry(ae, candidateSocket);//executa o método que processa a AppendEntry de outro servidor
                    System.out.println("Recebida AppendEntry. Sou o " + server.getServerID());
                } else {
                    candidateSocket.close();
                }
            }
//            serverSocket.close();
            if(Objects.equals(server.getState(), "FOLLOWER")){
                new Thread(new Follower(server, serverSocket)).start();
                
            } else if(Objects.equals(server.getState(), "LEADER")){
                new Thread(new Leader(server, server.getNextIndex(), server.getNextIndex())).start();
            }
            
            

        } catch (IOException ex) {
            System.err.println(server.getServerID() + " - Erro no estabelecimento da ligação com o líder \n" + ex.getLocalizedMessage());

        } catch (ClassNotFoundException ex) {
            System.err.println("Erro na conversão da classe Request\n" + ex.getLocalizedMessage());
        }

    }

    private void processAppendEntry(AppendEntry ae, Socket candidateSocket) {
        if (ae != null) {
            server.addCandidateSockets(ae.getLeaderId(), candidateSocket);
            server.getServerQueue().add(ae);
        }
    }

    public void stopCandidate() {
        this.stopCandidate = true;
    }
}
