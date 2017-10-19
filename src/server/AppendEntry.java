/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server;

import common.Message;
import java.util.ArrayList;

/**
 *
 * @author João
 */
public class AppendEntry extends Message {
    
    private int term;//termo do líder
    private String leaderId;
    private int prevLogIndex;//indice do log entry imediatamente anterior aos atuais
    private int prevLogTerm;//termo do prevLogIndex entry
    private ArrayList<LogEntry> entries;//guarda log entries (no heatbeat está vazio)
    private int leaderCommit;// commitIndex do líder
    private boolean success;
    
   
    public AppendEntry(int term, String leaderId, int prevLogIndex, int prevLogTerm, ArrayList<LogEntry> entries, int leaderCommit, boolean success, String id, int source, String messageType, String content) {
        super(id, source, messageType, content);
        this.term = term;
        this.leaderId = leaderId;
        this.prevLogIndex = prevLogIndex;
        this.prevLogTerm = prevLogTerm;
        this.entries = entries;
        this.leaderCommit = leaderCommit;
        this.success = success;
    }

    public int getTerm() {
        return term;
    }

    public String getLeaderId() {
        return leaderId;
    }

    public int getPrevLogIndex() {
        return prevLogIndex;
    }

    public int getPrevLogTerm() {
        return prevLogTerm;
    }

    public ArrayList<LogEntry> getEntries() {
        return entries;
    }

    public int getLeaderCommit() {
        return leaderCommit;
    }

    public boolean isSuccess() {
        return success;
    }
    
}
