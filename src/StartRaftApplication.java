
import client.Client;
import common.PropertiesManager;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import server.FileHandler;
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

    private static Map<String, Server> servidores;
    private static Map<Integer, Client> clientes;
    private static PropertiesManager serversProps;
    private static PropertiesManager clientsProps;

    public static void main(String[] args) {

        Scanner sc = new Scanner(System.in);
        servidores = new HashMap<>();
        clientes = new HashMap<>();
        serversProps = new PropertiesManager("servidor");
        clientsProps = new PropertiesManager("cliente");
        
        while (true) {
            System.out.println("Qual a operação que pretende executar?\n"
                    + "1 - Arrancar um servidor\n"
                    + "2 - Parar um servidor\n"
                    + "3 - Arrancar um cliente\n"
                    + "4 - Parar um cliente\n"
                    + "5 - Imprimir os log´s de todos os servidores\n"
                    + "6 - Terminar a aplicação");
            switch (sc.nextInt()) {
                case 1:
                    System.out.println("Introduza o ID do servidor: ");
                    startServer(sc.nextInt());
                    break;
                case 2:
                    System.out.println("Introduza o ID do servidor: ");
                    stopServer(sc.nextInt());
                    break;
//                case 3:
//                    System.out.println("Introduza o ID do cliente: ");
//                    startClient(sc.nextInt());
//                    break;
//                case 4:
//                    System.out.println("Introduza o ID do cliente: ");
//                    stopClient(sc.nextInt());
//                    break;
                case 5:
                    for (Map.Entry<String, Server> server : servidores.entrySet()) {
                        System.out.println(server.getValue().printLog() + "\n");
                    }
                    break;
                case 6:
                    System.exit(0);
                    break;
                default:
                    break;
            }
        }
    }

    /* Iniciar Servidor */
    public static void startServer(int id) {
        String serverID = "srv" + id;
        Server server = new Server(serverID, serversProps, clientsProps);
        new Thread(server).start();
        servidores.put(serverID, server);
    }

    /* Parar Servidor */
    public static void stopServer(int id) {
        servidores.get("srv" + id).stopServer();
        System.out.println("O servidor " + id + " foi desligado!\n");
    }

    /* Iniciar Clientes */
//    public static void startClient(int id) {
//        Client c = new Client(id, clientsProps);
//        new Thread(c).start();
//        clientes.put(id, c);
//    }

    /* Parar Cliente */
//    public static void stopClient(int id) {
//        clientes.get(id).stopClient();
//        System.out.println("O cliente " + id + " foi desligado!\n");
//    }
}
