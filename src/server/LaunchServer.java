/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server;

import common.PropertiesManager;
import java.util.Scanner;

/**
 *
 * @author TFD-GRUPO11-17/18
 */
public class LaunchServer {

    public static void main(String[] args) {

        PropertiesManager serversProps = new PropertiesManager("servidor");
        PropertiesManager clientsProps = new PropertiesManager("cliente");

        Scanner sc = new Scanner(System.in);
        System.out.println("Introduza o ID do servidor: ");

        String serverID = "srv" + sc.nextInt();
        Server server = new Server(serverID, serversProps, clientsProps);
        server.initiateServer();

        while (true) {
            System.out.println("Qual a operação que pretende executar?\n"
                    + "1 - Imprimir LOG do Servidor\n"
                    + "2 - Encerrar Servidor");
            switch (sc.nextInt()) {
                case 1:
                    System.out.println(server.printLog() + "\n");
                    break;
                case 2:
                    server.stopServer();
                    System.exit(0);
                    break;
                default:
                    break;
            }
        }
    }
}
