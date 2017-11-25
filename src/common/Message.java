/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package common;

import java.io.Serializable;

/**
 *
 * @author TFD-GRUPO11-17/18
 */
public class Message implements Serializable {

    private String id;
    private int source;
    private String messageType;
    private OperationType operationType;
    private String key;
    private String content;

    public Message(String id, int source, String messageType, OperationType operationType, String key, String content) {
        this.id = id;
        this.source = source;
        this.messageType = messageType;
        this.operationType = operationType;
        this.key = key;
        this.content = content;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getSource() {
        return source;
    }

    public void setSource(int source) {
        this.source = source;
    }

    public String getMessageType() {
        return messageType;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }

    public OperationType getOperationType() {
        return operationType;
    }

    public void setOperationType(OperationType operationType) {
        this.operationType = operationType;
    }
    
    public String getContent() {
        return content;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public void setContent(String content) {
        this.content = content;
    }  

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Message) || obj == null) {
            return false;
        }
        Message r = (Message) obj;
        return  this.id.equals(r.getId());
    }

}
