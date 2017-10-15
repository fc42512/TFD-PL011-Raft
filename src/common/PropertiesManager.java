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

    private static final String SERVER_PROPERTIES = "config.properties";
    private Properties props;
    
    public PropertiesManager () {
        props = new Properties();
        props = loadProps(SERVER_PROPERTIES);
    }

    private Properties loadProps(String filename) {
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
        return props;
    }
    
    public String [] getServerAdress (String serverID){
        String [] serverAdress = props.getProperty(serverID).split(":");
        return serverAdress;
    }
}
