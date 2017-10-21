/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server;

import common.Message;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author João
 */
public class Leader implements Runnable {

    private Server server;
    private HashMap<String, Integer> nextIndex;//indice do próximo log entry a enviar para cada servidor
    private int[] matchIndex;//indice do último log entry replicado em cada servidor

    public Leader(Server s, HashMap<String, Integer> nextIndex) {
        this.server = s;
        this.nextIndex = nextIndex;
    }

    @Override
    public void run() {
        System.out.println("Líder arrancou...");

        /*Líder faz commit de uma "blank no-operation" entry no início de cada termo */
//        server.appendLogEntry(new LogEntry(server.getCurrentTerm(), server.getCurrentLogIndex(), "NO-OP", ""));

        Message m, response;
        while (true) {
          int i = 0;  
//            System.out.println("Teste...");
            if (!server.getClientQueue().isEmpty()) {

                /* Líder adiciona uma nova entrada no seu log */
                m = server.getClientQueue().remove();
                server.appendLogEntry(new LogEntry(server.getCurrentTerm(), server.getCurrentLogIndex()+1, m.getMessageType(), m.getContent(), m.getId(), m.getSource()));
                System.out.println("Processar a resposta...");
                
                /* Líder envia o AppendEntries RPC */
                ArrayList<LogEntry> entries = new ArrayList<>();
                entries.add(server.getLog().get(server.getCurrentLogIndex()));
                sendAppendEntries(new AppendEntry(server.getCurrentTerm(), server.getServerID(), server.getCurrentLogIndex(), server.getLog().get(server.getCurrentLogIndex()).getTerm(),
                        entries, server.getCommitIndex(), true, m));

                /* Líder executa o comando na sua máquina de estados */
                server.applyNewEntries();//executa comando na máquina de estados
                response = server.getStateMachineResult(m.getSource());
                System.out.println("Executa na máquina de estados...");

                /* Líder envia a resposta ao cliente */
                try {
                    server.getClientSocket(m.getSource()).sendMessageToClient(response);//responde ao utilizador
                } catch (IOException ex) {
                    System.err.println("Erro na resposta ao cliente pelo líder \n" + ex.getLocalizedMessage());
                }
            }
        }

    }

    private void sendAppendEntries(AppendEntry m) {
        createTalkToFollower(m);
        System.out.println("Enviado para todos os followers...");
        int majority = 1;
        while (majority > (2 / 2)) {
            System.out.println("Aguarda maioria..." + server.getServerQueue().size());
            if (!server.getServerQueue().isEmpty()) {
                AppendEntry ae = server.getServerQueue().remove();
                if(ae.isSuccess()){
                    majority++;
                }
            }
        }
        server.incrementCommitIndex();//actualiza último indice comitado
        System.out.println("Temos maioria...");
    }
    
    private void createTalkToFollower(AppendEntry m) {
        String serverToTalk;
        for (int i = 0; i < server.getServersProps().getHashMapProperties().size(); i++) {
            serverToTalk = "srv" + i;
            if (!server.getServerID().equals(serverToTalk)) {
                new Thread(new TalkToFollower(server, Integer.parseInt(server.getServersProps().getServerAdress(serverToTalk)[1]), m)).start();
            }
        }
    }
}
