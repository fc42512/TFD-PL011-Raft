package common;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class PropertiesManager {

    private static final String CLIENT_TO_SERVER_PROPERTIES = "client_config.properties";
    private static final String SERVER__TO_SERVER_PROPERTIES = "server_config.properties";
    private Properties props;
    
    public PropertiesManager (String type) {
        props = new Properties();
        if(type.equals("servidor")){
            loadProps(SERVER__TO_SERVER_PROPERTIES);
        }
        else{
            loadProps(CLIENT_TO_SERVER_PROPERTIES);
        }  
    }

    private void loadProps(String filename) {
        InputStream is = null;
        try {
            is = new FileInputStream(filename);
            props.load(is);
            is.close();
        } 
        catch (FileNotFoundException ex) {
            props = null;
            System.err.println("O ficheiro de propriedades não existe \n" + ex.getLocalizedMessage());
        } 
        catch (IOException ex) {
            props = null;
            System.err.println("Erro IO \n" + ex.getLocalizedMessage());
        }
    }
    
    public String [] getServerAdress (String serverID){
        String [] serverAdress = props.getProperty(serverID).split(":");
        return serverAdress;
    }
}
