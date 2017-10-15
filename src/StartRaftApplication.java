
import client.Client;
import common.PropertiesManager;
import java.util.HashMap;
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

    private static HashMap<String, Thread> servidores;
    private static HashMap<Integer, Thread> clientes;
    private static PropertiesManager props;

    public static void main(String[] args) {

        Scanner sc = new Scanner(System.in);
        servidores = new HashMap<>();
        clientes = new HashMap<>();
        props = new PropertiesManager();

        while (true) {
            System.out.println("Qual a operação que pretende executar?\n"
                    + "1 - Arrancar um servidor\n"
                    + "2 - Parar um servidor\n"
                    + "3 - Arrancar um cliente\n"
                    + "4 - Parar um cliente");
            switch (sc.nextInt()) {
                case 1:
                    System.out.println("Introduza o ID do servidor: ");
                    startServer(sc.nextInt());
                    break;
                case 2:
                    System.out.println("Introduza o ID do servidor: ");
                    stopServer(sc.nextInt());
                    break;
                case 3:
                    System.out.println("Introduza o ID do cliente: ");
                    startClient(sc.nextInt());
                    break;
                case 4:
                    System.out.println("Introduza o ID do cliente: ");
                    stopClient(sc.nextInt());
                    break;
                default:
                    break;
            }
        }
    }

    /* Iniciar Servidor */
    public static void startServer(int id) {
        String serverID = "srv" + id;
        int serverPort = Integer.parseInt(props.getServerAdress(serverID)[1]);
        Thread serverThread = new Thread(new Server(serverID, serverPort));
        serverThread.start();
        servidores.put(serverID, serverThread);

    }
    
    /* Parar Servidor */
    public static void stopServer(int id){
        servidores.get("srv"+id).interrupt();
    }

    /* Iniciar Clientes */
    public static void startClient(int id) {
        Thread clientThread = new Thread(new Client(id));
        clientThread.start();
        clientes.put(id, clientThread);

    }
    
    /* Parar Cliente */
    public static void stopClient(int id){
        clientes.get(id).interrupt();
    }
}
