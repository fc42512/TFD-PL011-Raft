/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server;

import common.Message;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 *
 * @author TFD-GRUPO11-17/18
 */
public class Leader implements Runnable {

    private Server server;
    private Map<String, Integer> nextIndex;//indice do próximo log entry a enviar para cada servidor
    private Map<String, Integer> matchIndex;//indice do último log entry replicado em cada servidor
    private Map<String, TalkToFollower> followers;
    private boolean stopLeader;
    private HeartBeat heartbeat;

    public Leader(Server s, Map<String, Integer> nextIndex, Map<String, Integer> matchIndex) {
        this.server = s;
        this.nextIndex = nextIndex;
        this.matchIndex = matchIndex;
        this.followers = new HashMap<>();
        this.stopLeader = false;
    }

    @Override
    public void run() {
        System.out.println("Líder arrancou..." + server.getServerID());
        server.setThreadLeader(this);
        server.resetThreadFollower();
        server.resetThreadCandidate();
        server.getServerQueue().clear();//limpa a fila de AppendEntry recebidas
        createTalkToFollowers();
        heartbeat = new HeartBeat(this);
        new Thread(heartbeat).start();//arranca com a thread responsável por preparar os heartbeats para serem enviados

        /*Líder faz commit de uma "blank no-operation" entry no início de cada termo */
//        server.appendLogEntry(new LogEntry(server.getCurrentTerm(), server.getCurrentLogIndex() + 1, "NO-OP", "noOperation", null, 0));
        Message m;
        LogEntry le;
        int numFollowers = server.getServersProps().getHashMapProperties().size() / 2;
        while (!stopLeader) {

            if (!server.getClientQueue().isEmpty()) {
                m = server.getClientQueue().remove();

                /* Líder verifica se um dado pedido já foi executado na máquina de estados e envia para o cliente */
                if (server.getStateMachineResult(m.getId() + m.getSource()) != null) {
                    try {
                        server.getProcessClientSocket(m.getSource()).sendMessageToClient(server.getStateMachineResult(m.getId() + m.getSource()));//responde ao cliente
                    } catch (IOException ex) {
                        System.err.println("Erro na resposta ao cliente pelo líder \n" + ex.getLocalizedMessage());
                    }
                }

                /* Líder adiciona uma nova entrada no seu log */
                int index;
                if (server.getLog().isEmpty()) {
                    index = server.getLastApplied() + 1;
                } else {
                    index = server.getCurrentLogIndex() + 1;
                }
                server.appendLogEntry(new LogEntry(server.getCurrentTerm(), index, m.getOperationType(), m.getKey(), m.getContent(), m.getId(), m.getSource()));
                System.out.println("Processar a resposta...");

                /* Líder envia o AppendEntries RPC */
                ArrayList<LogEntry> entries = new ArrayList<>();
                entries.add(server.getLog().getLast());
                int prevLogIndex, prevLogTerm, commitIndex;
                if (server.getLog().isEmpty() || server.getLog().size() == 1) {
                    if (server.getLastApplied() > 0) {
                        prevLogIndex = server.getLastApplied();
                        prevLogTerm = server.getCurrentTerm();
                        commitIndex = server.getCommitIndex();
                    } else {
                        prevLogIndex = -1;
                        prevLogTerm = -1;
                        commitIndex = 0;
                    }
                } else {
                    prevLogIndex = server.getLog().get(server.getLog().size() - 2).getIndex();
                    prevLogTerm = server.getLog().get(server.getLog().size() - 2).getTerm();
                    commitIndex = server.getCommitIndex();
                }
                sendAppendEntries(new AppendEntry(server.getCurrentTerm(), server.getServerID(), prevLogIndex, prevLogTerm, entries, commitIndex, true, m, "APPENDENTRY"));

            } else if (!server.getServerQueue().isEmpty()) {
                AppendEntry ae = server.getServerQueue().remove();
                if (Objects.equals(ae.getType(), "REQUESTVOTE")) {
                    AppendEntry rv = server.receiverRequestVoteValidation(ae);//executa a validação de quem recebe um requestVote

                    if (rv.isSuccess()) {
                        stopLeader();//paro o líder, pois ele votou favorávelmente a um candidato
                        new Thread(new Follower(server)).start();//inicia-se como follower
                    } else {
                        try {
                            sendRequestVoteToCandidate(rv, server.getServersSockets(ae.getLeaderId()));
                        } catch (IOException ex) {
                            System.out.println("Falha no reenvio do requestVote pelo lider" + server.getServerID());
                        }
                    }
                } else if (ae.isSuccess()) {
                    int followerLastApplied = server.getLogEntryIndexInLog(ae.getLeaderCommit());
                    if (followerLastApplied != -1) {
                        le = server.getLog().get(followerLastApplied);
                        if (!le.isCommited() && (ae.getLeaderCommit() >= server.getCommitIndex())) {
                            le.incrementMajority();//incrementa para tentar fazer maioria numa dada log entry
                            if (le.getMajority() > numFollowers) {
                                server.setCommitIndex(ae.getLeaderCommit());//actualiza último indice comitado
                                System.out.println("Temos maioria...");

                                /* Líder executa o comando na sua máquina de estados */
                                System.out.println("Executa na máquina de estados...");
                                server.applyNewEntries();//executa comando na máquina de estados

                                matchIndex.put(ae.getLeaderId(), ae.getLeaderCommit());//actualiza o ultimo indice commitado pelo follower
                                nextIndex.put(ae.getLeaderId(), ae.getLeaderCommit() + 1);//actualiza o proximo indice a enviar para o follower

                                /* Líder resolve as inconsistências dos followers */
                                resolveConflictingEntries();

                                /* Realiza um snapshot da máquina de estados actual */
                                if (server.getLog().size() > 5) {
                                    server.takeSnapshot();
                                }
                            }
                        }
                    }
                } else {
                    matchIndex.put(ae.getLeaderId(), ae.getLeaderCommit());//actualiza o ultimo indice commitado pelo follower
                    nextIndex.put(ae.getLeaderId(), ae.getLeaderCommit() + 1);//actualiza o proximo indice a enviar para o follower
                }
            }
        }
    }

    private void createTalkToFollowers() {
        String serverToTalk;
        TalkToFollower ttf;
        ThreadGroup talkToFollowers = new ThreadGroup("talkToFollowers");
        for (int i = 0; i < server.getServersProps().getHashMapProperties().size(); i++) {
            serverToTalk = "srv" + i;
            if (!server.getServerID().equals(serverToTalk)) {
                ttf = new TalkToFollower(server, server.getServersProps().getServerAdress(serverToTalk));
                followers.put(serverToTalk, ttf);
                new Thread(talkToFollowers, ttf).start();
            }
        }
    }

    public void sendAppendEntries(AppendEntry ae) {
        for (Map.Entry<String, TalkToFollower> t : followers.entrySet()) {
            t.getValue().storeAppendEntryInQueue(ae);
        }
        System.out.println("Enviado para todos os followers...");
    }

    private void resolveConflictingEntries() {
        for (Map.Entry<String, Integer> follower : nextIndex.entrySet()) {
            if (server.getCommitIndex() >= follower.getValue()) {
                ArrayList<LogEntry> entries = new ArrayList<>();
                AppendEntry ae;
                if (server.getLogEntryIndexInLog(follower.getValue()) != -1) {
                    for (int i = server.getLogEntryIndexInLog(follower.getValue()); i <= server.getLogEntryIndexInLog(server.getCommitIndex()); i++) {
                        entries.add(server.getLog().get(i));
                    }
                    int prevLogIndex, prevLogTerm;
                    if (follower.getValue() == 0) {
                        prevLogIndex = -1;
                        prevLogTerm = -1;
                    } else {
                    prevLogIndex = follower.getValue() - 1;
//                    prevLogTerm = server.getLog().get(follower.getValue() - 1).getTerm();
//                        prevLogIndex = server.getLastApplied() - 1;
                        prevLogTerm = server.getCurrentTerm();
                    }
                    ae = new AppendEntry(server.getCurrentTerm(), server.getServerID(), prevLogIndex, prevLogTerm, entries, server.getCommitIndex(), true, null, "APPENDENTRY");
                }
                else {
                    ae = server.getFileHandler().readSnapshotFileToFollower();
                }
                followers.get(follower.getKey()).storeAppendEntryInQueue(ae);
            }
        }
    }

    private void sendRequestVoteToCandidate(AppendEntry rv, Socket s) throws IOException {
        BufferedOutputStream bos = new BufferedOutputStream(s.getOutputStream());
        ObjectOutputStream osw = new ObjectOutputStream(bos);
        osw.writeObject(rv);//Envia o appendentry
        osw.flush();
        osw.close();
        bos.close();
        s.close();//Fecha a ligação
    }

    private void stopAllTalkToFollowers() {
        for (Map.Entry<String, TalkToFollower> t : followers.entrySet()) {
            t.getValue().stopTalkToFollower();
        }
        System.out.println("Todos os TalkToFollowers estão parados...");
    }

    public Server getServer() {
        return this.server;
    }

    public void stopLeader() {
        this.stopLeader = true;
        heartbeat.stopHeartBeat();
        if (server.getProcessServer() != null) {
            server.getProcessServer().stopProcessServer();
        }
        stopAllTalkToFollowers();
    }
}
