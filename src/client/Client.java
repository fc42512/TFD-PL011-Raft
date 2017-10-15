/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package client;

import common.PropertiesManager;
import common.Message;
import java.util.Properties;
import java.util.Random;

/**
 *
 * @author João
 */
public class Client implements Runnable {

    private int id;
    private PropertiesManager props;
    private int requestID;
    private String leaderID;
    private Message request;
    private Message response;
    
    public Client(int id, PropertiesManager props) {
        this.id = id;
        requestID = 0;
        response = null;
        leaderID = null;
        System.out.println("O cliente " + id + " arrancou!");
        this.props = props;

    }

    @Override
    public void run() {
        Random rnd = new Random();
        
        
        int coeficient;
        while (true) {
            if(response == null){
                request = new Message("CL" + id + "-RQ" + requestID, "", "");
            }
            else{
               requestID++;
               request = new Message("CL" + id + "-RQ" + requestID, "", "");
            }
            
            new Thread(new ClientRequest(this, request)).start();

            try {
                coeficient = rnd.nextInt(2)+1;
                Thread.sleep(coeficient * 2000);
            } 
            catch (InterruptedException ex) {
                
            }

        }

    }
    
    

    public void setResponse(Message response) {
        this.response = response;
        System.out.println(response.getId() + " " + response.getContent());
    }

    public PropertiesManager getProps() {
        return props;
    }

    public String getLeaderID() {
        return leaderID;
    }

    public void setLeaderID(String leaderID) {
        this.leaderID = leaderID;
    }
    
    

}
