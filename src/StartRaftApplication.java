
import client.Client;
import java.util.Scanner;
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
        
        /* Iniciar Servidores */
        System.out.println("Introduza o número de servidores: ");
        int numServers = sc.nextInt();
        for(int i=0; i < numServers; i++){
            new Thread(new Server()).start();
            
        }
        
        
        /* Iniciar Clientes */
        System.out.println("Introduza o número de clientes: ");
        int numClients = sc.nextInt();
        for(int i=0; i < numClients; i++){
            new Thread(new Client(i)).start();
            
        }
    }
    
}
