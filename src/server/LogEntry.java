/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server;

import common.OperationType;
import java.io.Serializable;

/**
 *
 * @author TFD-GRUPO11-17/18
 */
public class LogEntry implements Serializable{
    
    private int term;
    private int index;
    private OperationType operationType;
    private String key;
    private String value;
    private String idMessage;
    private int source;
    private int majority;
    private boolean commited;
    private boolean sentToClient;

    public LogEntry(int term, int index, OperationType operationType, String key, String value, String idMessage, int source) {
        this.term = term;
        this.index = index;
        this.operationType = operationType;
        this.key = key;
        this.value = value;
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

    public OperationType getOperationType() {
        return operationType;
    }

    public void setOperationType(OperationType operationType) {
        this.operationType = operationType;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
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
