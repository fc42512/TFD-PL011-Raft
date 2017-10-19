/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server;

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
        /*Líder faz commit de uma "blank no-operation" entry no início de cada termo */
        server.appendLogEntry(new LogEntry(server.getCurrentTerm(),server.getCurrentLogIndex(), "NO-OP", ""));
        
        
        
        
    }
    
}
