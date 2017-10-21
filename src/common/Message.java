/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package common;

import java.io.Serializable;
import server.ProcessClient;

/**
 *
 * @author João
 */
public class Message implements Serializable {

    private String id;
    private int source;
    private String messageType;
    private String content;

    public Message(String id, int source, String messageType, String content) {
        this.id = id;
        this.source = source;
        this.messageType = messageType;
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

    public String getContent() {
        return content;
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
