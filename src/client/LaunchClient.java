/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package client;

import common.OperationType;
import common.PropertiesManager;
import java.util.Scanner;

/**
 *
 * @author TFD-GRUPO11-17/18
 */
public class LaunchClient {

    public static void main(String[] args) {

        PropertiesManager clientsProps = new PropertiesManager("cliente");

        Scanner sc = new Scanner(System.in);
        System.out.println("Introduza o ID do cliente: ");
        Client c = new Client(sc.nextInt(), clientsProps);

        while (true) {
            System.out.println("Qual a operação que pretende executar?\n"
                    + "1 - PUT\n"
                    + "2 - GET\n"
                    + "3 - LIST\n"
                    + "4 - DEL\n"
                    + "5 - CAS\n"
                    + "6 - Encerrar Cliente");

            switch (sc.nextInt()) {
                case 1:
                    System.out.println("Introduza um valor:");
                    c.sendRequest(OperationType.PUT, String.valueOf(sc.nextInt()));
                    break;
                case 2:
                    c.sendRequest(OperationType.GET, String.valueOf(0));
                    break;
                case 3:
                    c.sendRequest(OperationType.LIST, String.valueOf(0));
                    break;
                case 4:
                    c.sendRequest(OperationType.DEL, String.valueOf(0));
                    break;
                case 5:
                    System.out.println("Introduza um valor de comparação:");
                    String value = "" + sc.nextInt() + "|";
                    System.out.println("Introduza o valor de troca:");
                    c.sendRequest(OperationType.CAS, value + sc.nextInt());
                    break;
                case 6:
                    System.exit(0);
                    break;
                default:
                    break;
            }
        }
    }
}
