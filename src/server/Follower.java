/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

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
            Socket liderSocket = serverSocket.accept();

            /* Processar os pedidos do líder */
            while (true) {

                ObjectInputStream dis = new ObjectInputStream(liderSocket.getInputStream());
                AppendEntry response = processAppendEntries(dis.readObject());//executa o método que processa ao appendEntries
                System.out.println("Recebida msg");

//                if (response != null) {
                sendMessageToLeader(response, liderSocket);
                System.out.println("Enviada msg");
//                }
            }

        } catch (IOException ex) {
            System.err.println(server.getServerID() + " - Erro no estabelecimento da ligação com o líder \n" + ex.getLocalizedMessage());
        } catch (ClassNotFoundException ex) {
            System.err.println("Erro na conversão da classe Request\n" + ex.getLocalizedMessage());
        }
    }

    private void sendMessageToLeader(AppendEntry ae, Socket s) throws IOException {
        BufferedOutputStream bos = new BufferedOutputStream(s.getOutputStream());
        ObjectOutputStream osw = new ObjectOutputStream(bos);
        osw.writeObject(ae);//Envia a mensagem
        osw.flush();
//        osw.close();
//        bos.close();
//        s.close();//Fecha a ligação

    }

    private AppendEntry processAppendEntries(Object obj) {
        AppendEntry response = null;
        if ((obj instanceof AppendEntry) && obj != null) {
            AppendEntry ae = (AppendEntry) obj;
            server.setLeaderID(ae.getLeaderId());//guarda o ID do líder

            /* Responde falso se term < currentTerm */
            if (ae.getTerm() < server.getCurrentTerm()) {
                return new AppendEntry(server.getCurrentTerm(), server.getServerID(), 0, 0, null, 0, false, ae.getMessage());
            } 
            /* Responde falso se term of prevLogIndex != prevLogTerm */ 
            else if (!server.getLog().isEmpty()) {
                if (server.getLog().get(ae.getPrevLogIndex()).getTerm() != ae.getPrevLogTerm()) {
                    return new AppendEntry(server.getCurrentTerm(), server.getServerID(), 0, 0, null, 0, false, ae.getMessage());
                } else if (server.getLog().get(ae.getPrevLogIndex()).getIndex() == ae.getPrevLogIndex()) {
                    addNewEntries(ae);
                    server.applyNewEntries();
                    return new AppendEntry(server.getCurrentTerm(), server.getServerID(), 0, 0, null, 0, true, ae.getMessage());
                } else {
                    deleteConflictEntries(ae.getPrevLogIndex());
                    return new AppendEntry(server.getCurrentTerm(), server.getServerID(), 0, 0, null, 0, false, ae.getMessage());
                }
            } else {
                addNewEntries(ae);
                server.applyNewEntries();
                return new AppendEntry(server.getCurrentTerm(), server.getServerID(), 0, 0, null, 0, true, ae.getMessage());
            }
        }

        return response;
    }

    private void addNewEntries(AppendEntry ae) {
        for (LogEntry l : ae.getEntries()) {
            server.appendLogEntry(l);
        }
        server.setCurrentTerm(ae.getTerm());
        server.setCommitIndex(ae.getLeaderCommit());
    }

    private void deleteConflictEntries(int index) {
        int maxIndex = server.getLog().size();
        for (int i = maxIndex; i >= index; i--) {
            server.getLog().remove(i);
        }
    }
    
    
}
