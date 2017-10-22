/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server;

import java.io.Serializable;

/**
 *
 * @author João
 */
public class LogEntry implements Serializable{
    
    private int term;
    private int index;
    private String command;
    private String parameter;
    private String idMessage;
    private int source;
    private int majority;
    private boolean commited;
    private boolean sentToClient;

    public LogEntry(int term, int index, String command, String parameter, String idMessage, int source) {
        this.term = term;
        this.index = index;
        this.command = command;
        this.parameter = parameter;
        this.idMessage = idMessage;
        this.source = source;
        this.majority = 1;
        this.commited = false;
        this.sentToClient = false;
    }

    public int getTerm() {
        return term;
    }

    public void setTerm(int term) {
        this.term = term;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }
    
    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public String getParameter() {
        return parameter;
    }

    public void setParameter(String parameter) {
        this.parameter = parameter;
    }

    public String getIdMessage() {
        return idMessage;
    }

    public void setIdMessage(String idMessage) {
        this.idMessage = idMessage;
    }

    public int getSource() {
        return source;
    }

    public void setSource(int source) {
        this.source = source;
    }

    public int getMajority() {
        return majority;
    }

    public void incrementMajority() {
        this.majority++;
    }

    public boolean isCommited() {
        return commited;
    }

    public void setCommited(boolean commited) {
        this.commited = commited;
    }

    public boolean isSentToClient() {
        return sentToClient;
    }

    public void setSentToClient(boolean sentToClient) {
        this.sentToClient = sentToClient;
    }
    
}
