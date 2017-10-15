/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package common;

import java.io.Serializable;

/**
 *
 * @author João
 */
public class Request implements Serializable {

    private String id;
    private String operationType;
    private String content;

    public Request(String id, String operationType, String content) {
        this.id = id;
        this.operationType = operationType;
        this.content = content;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getOperationType() {
        return operationType;
    }

    public void setOperationType(String operationType) {
        this.operationType = operationType;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Request) || obj == null) {
            return false;
        }
        Request r = (Request) obj;
        return  this.id.equals(r.getId());
    }

}
