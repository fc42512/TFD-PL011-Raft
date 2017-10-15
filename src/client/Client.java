/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package client;

import common.PropertiesManager;
import common.Request;
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
    private Request request;
    private Request response;
    
    public Client(int id, PropertiesManager props) {
        this.id = id;
        requestID = 0;
        response = null;
        System.out.println("O cliente " + id + " arrancou!");
        this.props = props;

    }

    @Override
    public void run() {
        Random rnd = new Random();
        
        
        int coeficient;
        while (true) {
            if(response == null){
                request = new Request("CL" + id + "-RQ" + requestID, "", "");
            }
            else{
               requestID++;
               request = new Request("CL" + id + "-RQ" + requestID, "", "");
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
    
    

    public void setResponse(Request response) {
        this.response = response;
        System.out.println(response.getId() + " " + response.getContent());
    }

    public PropertiesManager getProps() {
        return props;
    }
          

}
