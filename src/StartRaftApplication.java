
import client.Client;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import server.Server;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


/**
 *
 * @author João
 */
public class StartRaftApplication {
    
    


    public static void main(String[] args) {
        
        Scanner sc = new Scanner(System.in);
        Properties prop = new Properties();
        try {
            prop.load(new FileInputStream("config.properties"));
        } 
        catch (FileNotFoundException ex) {
            System.err.println("O ficheiro de propriedades não existe \n" + ex.getLocalizedMessage());
        } 
        catch (IOException ex) {
            System.err.println("Erro IO \n" + ex.getLocalizedMessage());
        }
        
        /* Iniciar Servidores */
        System.out.println("Introduza o número de servidores: ");
        int numServers = sc.nextInt();
        for(int i=0; i < numServers; i++){
            String [] hostAdress = prop.getProperty(String.valueOf(i)).split(":");
            new Thread(new Server(Integer.parseInt(hostAdress[1]))).start();
            
        }
        
        
        /* Iniciar Clientes */
        System.out.println("Introduza o número de clientes: ");
        int numClients = sc.nextInt();
        for(int i=0; i < numClients; i++){
            new Thread(new Client(i)).start();
            
        }
    }
    
}
