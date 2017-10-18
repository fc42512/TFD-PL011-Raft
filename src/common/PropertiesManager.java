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
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

public class PropertiesManager {

    private static final String CLIENT_TO_SERVER_PROPERTIES = "client_config.properties";
    private static final String SERVER__TO_SERVER_PROPERTIES = "server_config.properties";
    Map<String, String> hashMapProperties;

    public PropertiesManager(String type) {
        hashMapProperties = new HashMap<String, String>();
        if (type.equals("servidor")) {
            loadProps(SERVER__TO_SERVER_PROPERTIES);
        } else {
            loadProps(CLIENT_TO_SERVER_PROPERTIES);
        }
    }

    private void loadProps(String filename) {
        Properties props = new Properties();
        InputStream is = null;
        try {
            is = new FileInputStream(filename);
            props.load(is);
            is.close();
        } catch (FileNotFoundException ex) {
            props = null;
            System.err.println("O ficheiro de propriedades não existe \n" + ex.getLocalizedMessage());
        } catch (IOException ex) {
            props = null;
            System.err.println("Erro IO \n" + ex.getLocalizedMessage());
        }
        convertPropertiesToHashMap(props);
    }

    private void convertPropertiesToHashMap(Properties props) {
        for (Entry<Object, Object> x : props.entrySet()) {
            hashMapProperties.put((String) x.getKey(), (String) x.getValue());
        }
    }

    public String[] getServerAdress(String serverID) {
        return hashMapProperties.get(serverID).split(":");
    }

    public Map<String, String> getHashMapProperties() {
        return hashMapProperties;
    }
    
}
