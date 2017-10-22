/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server;

import common.Message;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author João
 */
public class Leader implements Runnable {

    private Server server;
    private Map<String, Integer> nextIndex;//indice do próximo log entry a enviar para cada servidor
    private Map<String, Integer> matchIndex;//indice do último log entry replicado em cada servidor

    public Leader(Server s, Map<String, Integer> nextIndex, Map<String, Integer> matchIndex) {
        this.server = s;
        this.nextIndex = nextIndex;
        this.matchIndex = matchIndex;
    }

    @Override
    public void run() {
        System.out.println("Líder arrancou...");

        /*Líder faz commit de uma "blank no-operation" entry no início de cada termo */
//        server.appendLogEntry(new LogEntry(server.getCurrentTerm(), server.getCurrentLogIndex() + 1, "NO-OP", "noOperation", null, 0));

        Message m;
        LogEntry le;
        int numFollowers = server.getServersProps().getHashMapProperties().size() / 2;
        while (true) {

            if (!server.getClientQueue().isEmpty()) {
                m = server.getClientQueue().remove();

                /* Líder verifica se um dado pedido já foi executado na máquina de estados e envia para o cliente */
                if (server.getStateMachineResult(m.getId() + m.getSource()) != null) {
                    try {
                        server.getClientSocket(m.getSource()).sendMessageToClient(server.getStateMachineResult(m.getId() + m.getSource()));//responde ao cliente
                    } 
                    catch (IOException ex) {
                        System.err.println("Erro na resposta ao cliente pelo líder \n" + ex.getLocalizedMessage());
                    }
                }

                /* Líder adiciona uma nova entrada no seu log */
                server.appendLogEntry(new LogEntry(server.getCurrentTerm(), server.getCurrentLogIndex() + 1, m.getMessageType(), m.getContent(), m.getId(), m.getSource()));
                System.out.println("Processar a resposta...");

                /* Líder envia o AppendEntries RPC */
                ArrayList<LogEntry> entries = new ArrayList<>();
                entries.add(server.getLog().get(server.getCurrentLogIndex()));
                int prevLogIndex, prevLogTerm, commitIndex;
                if (server.getCurrentLogIndex() == 0) {
                    prevLogIndex = -1;
                    prevLogTerm = -1;
                    commitIndex = 0;
                } else {
                    prevLogIndex = server.getCurrentLogIndex() - 1;
                    prevLogTerm = server.getLog().get(server.getCurrentLogIndex() - 1).getTerm();
                    commitIndex = server.getCommitIndex();
                }
                sendAppendEntries(new AppendEntry(server.getCurrentTerm(), server.getServerID(), prevLogIndex, prevLogTerm, entries, commitIndex, true, m));
//                entries.clear();

            } 

            else if(!server.getServerQueue().isEmpty()) {
                AppendEntry ae = server.getServerQueue().remove();
                if (ae.isSuccess()) {
                    le = server.getLog().get(ae.getPrevLogIndex());
                    if (!le.isCommited() && (ae.getPrevLogIndex() >= server.getCommitIndex())) {
                        le.incrementMajority();//incremeta para tantar fazer maioria numa dada log entry
                        if (le.getMajority() > numFollowers) {
                            server.setCommitIndex(ae.getPrevLogIndex());//actualiza último indice comitado
                            System.out.println("Temos maioria...");

                            /* Líder executa o comando na sua máquina de estados */
                            System.out.println("Executa na máquina de estados...");
                            server.applyNewEntries();//executa comando na máquina de estados
                            
                            matchIndex.put(ae.getLeaderId(), ae.getLeaderCommit());//actualiza o ultimo indice commitado pelo follower
                            nextIndex.put(ae.getLeaderId(), ae.getLeaderCommit() + 1);//actualiza o proximo indice a enviar para o follower
                            
                            /* Líder resolve as inconsistências dos followers */
                            resolveConflictingEntries();

                        }
                    }

                } else {
                    matchIndex.put(ae.getLeaderId(), ae.getLeaderCommit());//actualiza o ultimo indice commitado pelo follower
                    nextIndex.put(ae.getLeaderId(), ae.getLeaderCommit() + 1);//actualiza o proximo indice a enviar para o follower
                }
            } 
        }
    }

    private void sendAppendEntries(AppendEntry ae) {
        String serverToTalk;
        for (int i = 0; i < server.getServersProps().getHashMapProperties().size(); i++) {
            serverToTalk = "srv" + i;
            if (!server.getServerID().equals(serverToTalk)) {
                new Thread(new TalkToFollower(server, Integer.parseInt(server.getServersProps().getServerAdress(serverToTalk)[1]), ae)).start();
            }
        }
        System.out.println("Enviado para todos os followers...");
    }

    private void resolveConflictingEntries() {
        for (Map.Entry<String, Integer> follower : nextIndex.entrySet()) {
            if (server.getCurrentLogIndex() >= follower.getValue()) {
                ArrayList<LogEntry> entries = new ArrayList<>();
                for (int i = follower.getValue(); i <= server.getCurrentLogIndex(); i++) {
                    entries.add(server.getLog().get(i));
                }
                int prevLogIndex, prevLogTerm;
                if (follower.getValue() == 0) {
                    prevLogIndex = -1;
                    prevLogTerm = -1;
                } else {
                    prevLogIndex = follower.getValue() - 1;
                    prevLogTerm = server.getLog().get(follower.getValue() - 1).getTerm();
                }
                AppendEntry ae = new AppendEntry(server.getCurrentTerm(), server.getServerID(), prevLogIndex, prevLogTerm, entries, server.getCommitIndex(), true, null);
                new Thread(new TalkToFollower(server, Integer.parseInt(server.getServersProps().getServerAdress(follower.getKey())[1]), ae)).start();
            }
//            while (matchIndex.get(follower.getKey()) < server.getCurrentLogIndex()) {
////            System.out.println("Aguarda maioria..." + server.getServerQueue().size());
//                if (!server.getServerQueue().isEmpty()) {
//                    AppendEntry ae = server.getServerQueue().remove();
//                    matchIndex.put(ae.getLeaderId(), ae.getLeaderCommit());//actualiza o ultimo indice commitado pelo follower
//                    nextIndex.put(ae.getLeaderId(), ae.getLeaderCommit() + 1);//actualiza o proximo indice a enviar para o follower
//                }
//            }
        }
    }
}
