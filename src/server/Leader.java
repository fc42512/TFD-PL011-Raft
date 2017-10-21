/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server;

import common.Message;
import java.io.IOException;
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
        server.appendLogEntry(new LogEntry(server.getCurrentTerm(), server.getCurrentLogIndex(), "NO-OP", ""));

        Message m, response;
        while (true) {
            if (!server.getClientQueue().isEmpty()) {
                System.out.println("Processar a resposta1...");

                /* Líder adiciona uma nova entrada no seu log */
                m = server.getClientQueue().remove();
                server.appendLogEntry(new LogEntry(server.getCurrentTerm(), server.getCurrentLogIndex(), m.getMessageType(), m.getContent()));
                System.out.println("Processar a resposta2...");

                response = new Message(m.getId(), m.getSource(), "RESPONSE", m.getContent() + " Sucesso - atribuído o ID " + server.getIDMESSAGE());
                server.execute(m.getSource(), response);//executa comando na máquina de estados
                server.incrementIDMessage();
                System.out.println("Notifica...");
                try {
                    m.getProcessClient().sendMessageToClient(response);//responde ao utilizador
                } catch (IOException ex) {
                    System.err.println("Erro na resposta ao cliente pelo líder \n" + ex.getLocalizedMessage());
                }
            }
        }

    }
}
